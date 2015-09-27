package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorCallback;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

import com.google.common.collect.ImmutableList;

class XecutorConstructionContext implements StiXecutorConstructionContext
{
	private StiXpressionNode node;
	private boolean callbacksClaimed;
	
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
		return DefaultXecutors.FRAME_START;
	}
	
	@Override public StiXecutor createFrameResultXecutor()
	{
		return DefaultXecutors.FRAME_RESULT;
	}

	public StiXpressionNode getNode()
	{
		return node;
	}

	public void setNode(StiXpressionNode node)
	{
		if (this.node != null) {
			validateProcessedNode();
//			validateFrame(true);
//			validateAndCloseFrame(node.getFrameOwnerIndex());
		}
		this.callbacksClaimed = false;
		this.node = node;
//		validateFrame(false);
	}

	private void validateProcessedNode()
	{
		if (!callbacksClaimed && !this.node.getCallbackTargets().isEmpty()) {
			throw new IllegalStateException("Callbacks are defined, but not claimed by xtractorIndex=" + node.getIndex());
		}
	}
	
//	private void validateAndCloseFrame(int index)
//	{
//		if (index >= frame.getStartIndex()) return;
//		
//		if (!possibleFrameEnd) {
//			throw new IllegalStateException("Inconsistent frame sequence, currentFrame=" + frame.getStartIndex() +
//					", nodeFrame=" + index + ", nodeIndex=" + (node.getIndex() + 1));
//		}
//		if (frame.getOuterFrame().getStartIndex() != index) {
//			throw new IllegalStateException("Inconsistent close frame sequence, currentFrame=" + frame.getStartIndex() +
//				", nodeFrame=" + index + ", nodeIndex=" + node.getIndex() + ", outerFrame=" + frame.getOuterFrame().getStartIndex());
//		}
//		
//		frame = frame.getOuterFrame();
//	}
//
//	private void validateFrame(boolean strict)
//	{
//		int index = node.getFrameOwnerIndex();
//		if (strict ? index == frame.getStartIndex() : index >= frame.getStartIndex()) return; 
//
//		throw new IllegalStateException("Inconsistent frame sequence, currentFrame=" + frame.getStartIndex() +
//			", nodeFrame=" + index + ", nodeIndex=" + node.getIndex());
//	}

	@Override public boolean hasSortedFields()
	{
		return false; //TODO configurable
	}
}