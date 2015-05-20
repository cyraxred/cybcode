package org.cybcode.tools.bixtractor.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.cybcode.tools.bixtractor.api.BiXourceContext;
import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionConfiguration;
import org.cybcode.tools.bixtractor.ops.XtractorFormatter;

abstract class OpNode implements ContextParameterMapper, Cloneable
{
	static final Comparator<OpNode> NODE_DISTANCE_ASC = new Comparator<OpNode>() 
	{
		@Override public int compare(OpNode o1, OpNode o2)
		{
			return o1.distanceFromRoot - o2.distanceFromRoot;
		}
	};
	
	static final Comparator<OpLink> LINK_DISTANCE_ASC = new Comparator<OpLink>() 
	{
		@Override public int compare(OpLink o1, OpLink o2)
		{
			return o1.receiverNode.distanceFromRoot - o2.receiverNode.distanceFromRoot;
		}
	};
	
	int nodeIndex;
	final BiXtractor<?> op;
	int distanceFromRoot;
	private boolean enablePush;
	private OpLink[] pushReceivers; /* null or OpLink or List<OpLink> */
	private List<OpLink> receivers;

	OpNode(BiXtractor<?> op)
	{
		this.op = op;
		this.distanceFromRoot = Integer.MAX_VALUE;
		this.enablePush = !(op instanceof BiXtractor.NoPush);
	}

	OpNode copy()
	{
		OpNode result;
		try {
			result = (OpNode) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		result.pushReceivers = null;
		result.receivers = null;
		
		return result;
	}

	void compile(XpressionConfiguration config)
	{
		if (!config.isEnableEarlyCompletion()) {
			enablePush = false;
		}
		if (pushReceivers != null) throw new IllegalStateException();
		pushReceivers = filterPushReceivers(receivers, config);
		pushReceivers = validateReceivers(receivers, pushReceivers);
		receivers = null;
		Arrays.sort(pushReceivers, LINK_DISTANCE_ASC);
	}
	
	protected OpLink[] validateReceivers(List<OpLink> receivers, OpLink[] pushReceivers)
	{
		return pushReceivers;
	}

	private static final OpLink[] EMPTY_LINKS = new OpLink[0];
	
	private static OpLink[] filterPushReceivers(List<OpLink> receivers, XpressionConfiguration config)
	{
		if (receivers == null || receivers.isEmpty()) return EMPTY_LINKS;
		
		List<OpLink> filtered = new ArrayList<>(receivers.size());
		for (OpLink link : receivers) {
			if (!link.isPushLink(config)) continue;
			filtered.add(link);
		}

		if (filtered.isEmpty()) return EMPTY_LINKS;
		return filtered.toArray(new OpLink[filtered.size()]);
	}

	protected OpLink[] getPushReceivers()
	{
		if (pushReceivers == null) throw new IllegalStateException();
		return pushReceivers;
	}
	
	public OpLink addValueReceiver(OpNode receiverNode, Parameter<?> param)
	{
		OpLink result = OpLink.newInstance(this, receiverNode, param);
		if (receivers == null) {
			receivers = new ArrayList<>(2);
		}
		receivers.add(result);
		return result;
	}
	
	public void disablePush()
	{
		enablePush = false;
	}

	public OpNodeToken createToken()
	{
		int[] params = null; //TODO
		return new OpNodeToken(op.getClass(), op.getOperationToken(), params);
	}

	public boolean evaluate(InternalXecutionContext context)
	{
		if (context.hasNodeResultValue(this)) return false; //avoid multiple evaluations by push
		
		context.setNode(this);
		Object result = op.evaluate(context);
		context.setNodeResultValue(this, result);

		return pushToReceivers(context, result);
	}
	
	protected boolean pushToReceivers(InternalXecutionContext context, Object value)
	{
		for (OpLink link : pushReceivers) {
			if (link.pushAndEvaluate(context, value)) return true;
		}
		return false;
	}
	
	/**
	 * Called when link push was successful
	 */
	protected boolean evaluateAfterPush(InternalXecutionContext context)
	{
		return evaluate(context);
	}

	protected abstract boolean pushValueByNode(InternalXecutionContext context, Parameter<?> param, Object value);
	
	protected abstract boolean canPushByNode(XpressionConfiguration config);

	boolean isPushByNodeEnabled(XpressionConfiguration config)
	{
		return isPushEnabled() && canPushByNode(config);
	}
	
	public boolean isPushEnabled()
	{
		return enablePush;
	}
	
	public abstract boolean hasParameters();

	abstract void mapParameters(OpNode node, List<OpNode> mappedNodes);

	protected OpLink mapParameter(OpLink link, List<OpNode> mappedNodes)
	{
		int sourceIndex = link.sourceNode.nodeIndex;
		OpNode mappedSource;
		try {
			mappedSource = mappedNodes.get(sourceIndex);
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Circular reference: node1=" + link.receiverNode + ", node2=" + link.sourceNode);
		}
		
		return mappedSource.addValueReceiver(this, link.param);
	}
	
	abstract void setParameter(OpLink opLink);
	abstract void setParameters(OpLink[] opLinks);

	InternalXecutionContext prepareContext(XecutionContext context)
	{
		InternalXecutionContext result = (InternalXecutionContext) context; 
		result.setNode(this);
		return result;
	}
	
	abstract BiXourceContext getSourceContext();

	boolean hasReceivers()
	{
		return receivers != null && !receivers.isEmpty();
	}
	
	boolean isCompiled()
	{
		return pushReceivers != null;
	}
	
	
	protected abstract String getShortTypeString();
	
	@Override public String toString()
	{
		Object opToken = op.getOperationToken();
		return toShortString() + "(" + XtractorFormatter.nameOf(op) + (opToken == null ? "" : ":" + opToken) + ", " + 
			(pushReceivers == null ? receivers : Arrays.toString(pushReceivers));
	}
	
	public String toShortString()
	{
		return String.format("%s%02d", getShortTypeString(), nodeIndex);
	};
}
