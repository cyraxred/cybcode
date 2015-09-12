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
	private OpLink[] pushOutlets; /* null or OpLink or List<OpLink> */
	private List<OpLink> outlets;

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
		result.pushOutlets = null;
		result.outlets = null;
		
		return result;
	}

	void compile(XpressionConfiguration config)
	{
		if (!config.isEnableEarlyCompletion()) {
			enablePush = false;
		}
		if (pushOutlets != null) throw new IllegalStateException();
		pushOutlets = filterPushOutlets(outlets, config);
		pushOutlets = validateOutlets(outlets, pushOutlets);
		outlets = null;
		Arrays.sort(pushOutlets, LINK_DISTANCE_ASC);
	}
	
	protected OpLink[] validateOutlets(List<OpLink> outlets, OpLink[] pushOutlets)
	{
		return pushOutlets;
	}

	private static final OpLink[] EMPTY_LINKS = new OpLink[0];
	
	private static OpLink[] filterPushOutlets(List<OpLink> outlets, XpressionConfiguration config)
	{
		if (outlets == null || outlets.isEmpty()) return EMPTY_LINKS;
		
		List<OpLink> filtered = new ArrayList<>(outlets.size());
		for (OpLink link : outlets) {
			if (!link.isPushLink(config)) continue;
			filtered.add(link);
		}

		if (filtered.isEmpty()) return EMPTY_LINKS;
		return filtered.toArray(new OpLink[filtered.size()]);
	}

	protected OpLink[] getPushOutlets()
	{
		if (pushOutlets == null) throw new IllegalStateException();
		return pushOutlets;
	}
	
	public OpLink addOutlet(OpNode outletsNode, Parameter<?> param)
	{
		OpLink result = OpLink.newInstance(this, outletsNode, param);
		if (outlets == null) {
			outlets = new ArrayList<>(2);
		}
		outlets.add(result);
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

		return pushToOutlets(context, result);
	}
	
	protected boolean pushToOutlets(InternalXecutionContext context, Object value)
	{
		for (OpLink link : pushOutlets) {
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
		
		return mappedSource.addOutlet(this, link.param);
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

	boolean hasOutlets()
	{
		return outlets != null && !outlets.isEmpty();
	}
	
	boolean isCompiled()
	{
		return pushOutlets != null;
	}
	
	protected abstract String getShortTypeString();
	
	@Override public String toString()
	{
		return toShortString() + "(" + XtractorFormatter.nameAndTokenOf(op) + ", " + 
			(pushOutlets == null ? outlets : Arrays.toString(pushOutlets));
	}
	
	public String toShortString()
	{
		return String.format("%s%02d", getShortTypeString(), nodeIndex);
	}

	public boolean isRepeated(boolean isRepeatableDownstream)
	{
		return isRepeatableDownstream;
	};
}
