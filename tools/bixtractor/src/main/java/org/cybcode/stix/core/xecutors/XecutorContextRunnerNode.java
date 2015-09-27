package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

abstract class XecutorContextRunnerNode implements StiXecutorPushContext
{
	protected final int index;
	private final XecutorContextNode contextNode;
	protected final AbstractXecutorContextStorage storage;

	public XecutorContextRunnerNode(XecutorContextNode contextNode)
	{
		this.contextNode = contextNode;
		this.index = contextNode.getIndex();
		this.storage = contextNode.getStorage();
	}

	public void validatePushFinalState() {}

	public boolean isValidFrameOfNextNode(int frameOwnerIndex)
	{
		return getXpressionNode().getFrameOwnerIndex() == frameOwnerIndex;
	}

	public abstract RunnerFrame getFrame();

	public StiXpressionNode getXpressionNode()
	{
		return contextNode.getXpressionNode();
	}

	public Object getFinalValue()
	{
		if (!isFinalState()) return null;
		return getPushOrFinalValue();
	}

	public Object getPushOrFinalValue()
	{
		return storage.getResultValue(index);
	}

	public boolean isFinalState()
	{
		return storage.isFinalState(index);
	}

	@Override public StiXecutorContext getXecutorContext()
	{
		return contextNode;
	}

	@Override public void setFinalState()
	{
		storage.setFinalState(index);
	}

	@Override public StiXtractor<?> getCurrentXtractor()
	{
		return contextNode.getCurrentXtractor();
	}

	@Override public Object getInterimValue()
	{
		return storage.getInterimValue(index);
	}

	@Override public void setInterimValue(Object value)
	{
		storage.setInterimValue(index, value);
	}

	@Override public boolean hasInterimValue()
	{
		return storage.hasInterimValue(index);
	}

	public abstract StiXecutor getInitialState();
	public abstract StiXpressionSequencer getSequencer();
	
	public void setFinalValue(Object value)
	{
		setFinalState();
		storage.setResultValue(index, value);
		if (value == null) return;
		getSequencer().addPostponeTargets(getXpressionNode().getNotifyTargets());
	}

	protected Object internalEvaluateFinal()
	{
		return getCurrentXtractor().apply(getXecutorContext());
	}
	
	protected Object internalEvaluatePush(Parameter<?> targetParam, Object pushedValue)
	{
		return targetParam.evaluatePush(this, pushedValue);
	}
	
	public Object evaluateFinalState()
	{
		Object finalValue = internalEvaluateFinal();
		setFinalValue(finalValue);
		return finalValue;
	}
	
	public Object evaluatePush(XecutorContextRunnerNode originNode, Parameter<?> targetParam, Object pushedValue)
	{
		Object finalValue = internalEvaluatePush(targetParam, pushedValue);
		
		if (isFinalState()) {
//			if (targetIndex + 1 == outerFrame.getEndIndex()) {
//				outerFrame.resetFrame(true);
//			}
			storage.setResultValue(index, finalValue);
			if (finalValue == null) return null;
			getSequencer().addPostponeTargets(getXpressionNode().getNotifyTargets());
		} else {
			if (finalValue == null) return null;
			storage.setResultValue(index, finalValue);
			getSequencer().addImmediateTargets(getXpressionNode().getPushTargets());
		}
		return finalValue;
	}
	
	public void evaluateDirectPush(List<PushTarget> targets, Object pushedValue)
	{
		throw new IllegalStateException();
	}

	public void evaluateDirectPush(PushTarget target, Object nestedSource)
	{
		throw new IllegalStateException();
	}
}