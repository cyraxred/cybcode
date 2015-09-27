package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXpressionNode;
import org.cybcode.stix.api.StiXpressionSequencer;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXpressionNode.PushTarget;
import org.cybcode.stix.api.StiXtractor.Parameter;

abstract class XecutorRunnerNode implements StiXecutorPushContext
{
	private final StiXecutor initialState;
	protected final int index;
	private final XecutorContextNode contextNode;
	protected final AbstractXecutorContextStorage storage;

	public XecutorRunnerNode(XecutorContextNode contextNode, StiXecutor initialState)
	{
		this.contextNode = contextNode;
		this.initialState = initialState;
		this.index = contextNode.getIndex();
		this.storage = contextNode.getStorage();
	}

	protected StiXecutor getXecutor()
	{
		StiXecutor state = storage.getState(index);
		if (state != null) return state;
		return initialState;
	}
	
	protected Object internalEvaluateFinal()
	{
		StiXecutor xecutor = getXecutor();
		if (xecutor != null) {
			return xecutor.evaluateFinal(this);
		}
		return getCurrentXtractor().apply(getXecutorContext());
	}
	
	protected Object internalEvaluatePush(Parameter<?> targetParam, Object pushedValue)
	{
		StiXecutor xecutor = getXecutor();
		if (xecutor != null) {
			return xecutor.evaluatePush(this, targetParam, pushedValue);
		}
		return targetParam.evaluatePush(this, pushedValue);
	}

	public StiXecutor getInitialState()
	{
		return initialState;
	}
	
	public void validatePushFinalState() {}

	public boolean isValidFrameOfNextNode(int frameOwnerIndex)
	{
		return getXpressionNode().getFrameOwnerIndex() == frameOwnerIndex;
	}

	public abstract XecutorRunnerFrame getFrame();

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

	public abstract StiXpressionSequencer getSequencer();
	
	public void setFinalValue(Object value)
	{
		setFinalState();
		storage.setResultValue(index, value);
		if (value == null) return;
		getSequencer().addPostponeTargets(getXpressionNode().getNotifyTargets());
	}
	
	@Override public void setNextState(StiXecutor xecutor)
	{
		storage.setState(index, xecutor);
	}
	
	public Object evaluateFinalState()
	{
		Object finalValue = internalEvaluateFinal();
		setFinalValue(finalValue);
		return finalValue;
	}
	
	public Object evaluatePush(XecutorRunnerNode originNode, Parameter<?> targetParam, Object pushedValue)
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