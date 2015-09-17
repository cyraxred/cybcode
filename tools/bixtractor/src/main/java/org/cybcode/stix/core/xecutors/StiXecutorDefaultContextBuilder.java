package org.cybcode.stix.core.xecutors;

import java.util.ArrayList;
import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContextBuilder;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

import com.google.common.collect.ImmutableList;

public class StiXecutorDefaultContextBuilder implements StiXecutorContextBuilder
{
	private final ArrayList<NodeDetails> nodeDetails;
	private final ArrayList<Node> contextNodes;
	private boolean regularAsNotify;
	private final static int[] EMPTY_ARGS = new int[0];

	public StiXecutorDefaultContextBuilder()
	{
		nodeDetails = new ArrayList<>();
		contextNodes = new ArrayList<>();
	}

	@Override public void setCapacities(int nodesCount, int paramsCount, int linksCount)
	{
		if (nodesCount > 0) {
			nodeDetails.ensureCapacity(nodesCount);
			contextNodes.ensureCapacity(nodesCount);
		}
	}
	
	private static int[] getParamIndexes(NodeDetails node)
	{
		int paramCount = node.getParamCount();
		if (paramCount == 0) return EMPTY_ARGS;
		int[] params = new int[paramCount];
		for (int i = paramCount - 1; i >= 0; i--) {
			params[i] = node.getParamXtractorIndex(i);
		}
		return params;
	}

	private void getNodeTargets(NodeDetails node, List<PushTarget> callbackTargets, List<PushTarget> pushTargets, List<PushTarget> notifyTargets)
	{
		for (int i = node.getTargetCount() - 1; i >= 0; i--) {
			PushTarget pushTarget = createPushTarget(node, i);
			switch (pushTarget.getXtractorParam().getKind()) {
				case NEVER: 
					break;
				case PUSH_ALL: 
					pushTargets.add(pushTarget); 
					break;
				case CALLBACK:
					callbackTargets.add(pushTarget);
					break;
				case REGULAR: 
					if (!regularAsNotify) break;
				case NOTIFY_ON_FINAL: 
					notifyTargets.add(pushTarget); 
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	private static NodePushTarget createPushTarget(NodeDetails node, int targetIndex)
	{
		Parameter<?> targetParam = node.getTargetParameter(targetIndex);
		int targetXtractorIndex = node.getTargetXtractor(targetIndex).getXtractorIndex();
		return new NodePushTarget(targetParam, targetXtractorIndex, node.getXtractorIndex());
	}

	
	@Override public void addNode(NodeDetails node)
	{
		int index = node.getXtractorIndex();
		if (index != contextNodes.size()) throw new IllegalArgumentException();
		
		int[] params = getParamIndexes(node);

		Node contextNode = new Node(node.getXtractorIndex(), node.getXtractor(), params);
		contextNodes.add(contextNode);
		nodeDetails.add(node);
	}

	private void getNodeTargets(NodeDetails node, Node contextNode)
	{
		List<PushTarget> callbackTargets;
		List<PushTarget> pushTargets;
		List<PushTarget> notifyTargets;
		
		int targetCount = node.getTargetCount();
		switch (targetCount) {
			case 0:
				pushTargets = ImmutableList.of();
				notifyTargets = ImmutableList.of();
				callbackTargets = ImmutableList.of();
				break;
			default: {
				callbackTargets = new ArrayList<>(targetCount);
				pushTargets = new ArrayList<>(targetCount);
				notifyTargets = new ArrayList<>(targetCount);
				
				getNodeTargets(node, callbackTargets, pushTargets, notifyTargets);
				
				callbackTargets = ImmutableList.copyOf(callbackTargets);
				pushTargets = ImmutableList.copyOf(pushTargets);
				notifyTargets = ImmutableList.copyOf(notifyTargets);
			}
		}
		contextNode.setTargets(callbackTargets, pushTargets, notifyTargets);
	}
	
	@Override public void getNodeTargets()
	{
		for (int i = contextNodes.size() - 1; i >= 0; i--) {
			getNodeTargets(nodeDetails.get(i), contextNodes.get(i));
		}
	}

	@Override public StiXecutorDefaultContext build()
	{
		StiXpressionNode[] initialState = contextNodes.toArray(new StiXpressionNode[contextNodes.size()]);
		return new StiXecutorDefaultContext(initialState);
	}
	
	private static class NodePushTarget implements PushTarget
	{
		private final Parameter<?> param;
		private final int targetIndex;
		private final int nodeIndex;

		public NodePushTarget(Parameter<?> param, int targetIndex, int nodeIndex)
		{
			if (param == null) throw new NullPointerException();
			if (targetIndex <= 0 || nodeIndex < 0) throw new IllegalArgumentException();
			this.param = param;
			this.targetIndex = targetIndex;
			this.nodeIndex = nodeIndex;
		}

		@Override public Parameter<?> getXtractorParam()
		{
			return param;
		}

		@Override public int getXtractorIndex()
		{
			return targetIndex;
		}

		@Override public int getValueIndex()
		{
			return nodeIndex;
		}
	}
	
	private static class Node implements StiXpressionNode
	{
		private final int	xtractorIndex;
		private final StiXtractor<?>	xtractor;
		private final int[] params;
		private List<PushTarget> pushTargets;
		private List<PushTarget> notifyTargets;
		private List<PushTarget> callbackTargets;

		Node(int xtractorIndex, StiXtractor<?> xtractor, int[] params)
		{
			this.xtractorIndex = xtractorIndex;
			this.xtractor = xtractor;
			this.params = params;
		}

		private void setTargets(List<PushTarget> callbackTargets, List<PushTarget> pushTargets, List<PushTarget> notifyTargets)
		{
			if (callbackTargets == null || pushTargets == null || notifyTargets == null) throw new NullPointerException();
			if (this.pushTargets != null) throw new IllegalStateException();
			this.callbackTargets = callbackTargets;
			this.pushTargets = pushTargets;
			this.notifyTargets = notifyTargets;
		}

		@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
		{
			return xtractor.createXecutor(context);
		}

		@Override public StiXtractor<?> getXtractor()
		{
			return xtractor;
		}

		@Override public int getIndex()
		{
			return xtractorIndex;
		}

		@Override public int mapParamIndex(int paramIndex)
		{
			return params[paramIndex];
		}

		@Override public List<PushTarget> getCallbackTargets()
		{
			return callbackTargets;
		}
		@Override public List<PushTarget> getPushTargets()
		{
			return pushTargets;
		}

		@Override public List<PushTarget> getNotifyTargets()
		{
			return notifyTargets;
		}

		@Override public int getFrameResultIndex()
		{
			throw new IllegalStateException();
		}
	}

	public boolean isRegularAsNotify()
	{
		return regularAsNotify;
	}

	public void setRegularAsNotify(boolean regularAsNotify)
	{
		this.regularAsNotify = regularAsNotify;
	}
}
