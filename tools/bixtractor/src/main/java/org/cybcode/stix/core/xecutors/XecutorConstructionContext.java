package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorCallback;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

import com.google.common.collect.ImmutableList;

class XecutorConstructionContext implements StiXecutorConstructionContext
{
	private final int maxNodeIndex;
	private StiXpressionNode node;
	private boolean callbacksClaimed;
	
	public XecutorConstructionContext(int maxNodeIndex)
	{
		this.maxNodeIndex = maxNodeIndex;
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

	@Override public StiXecutor createFrameXecutor()
	{
		int index = node.getFrameResultIndex(); //checks validity of the claim to use frame
		if (index < 0 || index > maxNodeIndex) throw new IllegalStateException("Node #" + node.getIndex() + "has returned a wrong frameResultIndex=" + index);
		return FrameStartXecutor.INSTANCE;
	}

	public StiXpressionNode getNode()
	{
		return node;
	}

	public void setNode(StiXpressionNode node)
	{
		if (this.node != null && !(callbacksClaimed || this.node.getCallbackTargets().isEmpty())) {
			throw new IllegalStateException("Callbacks are defined, but not claimed by xtractorIndex=" + node.getIndex());
		}
		callbacksClaimed = false;
		this.node = node;
	}

	@Override public boolean hasSortedFields()
	{
		return false; //TODO configurable
	}
}