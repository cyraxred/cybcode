package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorCallback;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

import com.google.common.collect.ImmutableList;

class XecutorConstructionContext implements StiXecutorConstructionContext
{
//	private final int nodeCount;
	private CtxFrame frame;
	private StiXpressionNode node;
	private boolean callbacksClaimed;
	private boolean possibleFrameEnd;
	
	public XecutorConstructionContext(int nodeCount, CtxFrame outerFrame)
	{
//		this.nodeCount = nodeCount;
		this.frame = outerFrame;
	}

	@Override public List<StiXecutorCallback> getXecutorCallbacks()
	{
		List<PushTarget> targets = node.getCallbackTargets();
		callbacksClaimed = true;

		if (targets.isEmpty()) return ImmutableList.of();
		
		StiXecutorCallback[] result = new StiXecutorCallback[targets.size()];
		for (int i = result.length - 1; i >= 0; i--) {
			result[i] = new XecutorCallback(targets.get(i));
		}
		
		return ImmutableList.copyOf(result);
	}
	
	@Override public StiXecutorCallback createCallbackGroup(List<StiXecutorCallback> callbacks)
	{
		if (callbacks.isEmpty()) throw new IllegalArgumentException();
		return XecutorCallbackGroup.newInstance(callbacks);
	}
	
	@Override public boolean hasPushTargets()
	{
		return !node.getPushTargets().isEmpty();
	}

	@Override public StiXecutor createFrameStartXecutor()
	{
		frame = new CtxFrame(node.getIndex(), frame);
		return new XecutorStartFrame(frame);
	}
	
	@Override public StiXecutor createFrameResultXecutor()
	{
		possibleFrameEnd = true;
		CtxFrame.ResultMarker frameResult = frame.registerFrameResult(node.getIndex());
		return new XecutorResolveFrame(frameResult);
	}

	public StiXpressionNode getNode()
	{
		return node;
	}

	public void setNode(StiXpressionNode node)
	{
		if (this.node != null) {
			if (!callbacksClaimed && !this.node.getCallbackTargets().isEmpty()) {
				throw new IllegalStateException("Callbacks are defined, but not claimed by xtractorIndex=" + node.getIndex());
			}
		}
		this.callbacksClaimed = false;
		this.node = node;
		validateFrame();
		this.possibleFrameEnd = false;
	}
	
	private void validateFrame()
	{
		int index = node.getFrameOwnerIndex();
		
		if (index == frame.getStartIndex()) return; 
			
		if (possibleFrameEnd) {
			if (frame.getOuterFrame().getStartIndex() == index) return;
		}

		throw new IllegalStateException("Inconsistent frame sequence, currentFrame=" + frame.getStartIndex() +
			", nodeFrame=" + index + ", nodeIndex=" + node.getIndex());
	}

	@Override public boolean hasSortedFields()
	{
		return false; //TODO configurable
	}
}