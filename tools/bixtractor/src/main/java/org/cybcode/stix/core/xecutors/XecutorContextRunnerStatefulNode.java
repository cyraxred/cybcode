package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXtractor.Parameter;

class XecutorContextRunnerStatefulNode extends XecutorContextRunnerFramedNode
{
	protected final StiXecutor initialState;
	
	public XecutorContextRunnerStatefulNode(XecutorContextNode contextNode, RunnerFrame frame, StiXecutor initialState)
	{
		super(contextNode, frame);
		if (initialState == null) throw new NullPointerException();
		this.initialState = initialState;
	}
	
	protected StiXecutor getXecutor()
	{
		StiXecutor state = storage.getState(index);
		if (state != null) return state;
		return initialState;
	}
	
	@Override protected Object internalEvaluateFinal()
	{
		return getXecutor().evaluateFinal(this);
	}
	
	@Override protected Object internalEvaluatePush(Parameter<?> targetParam, Object pushedValue)
	{
		return getXecutor().evaluatePush(this, targetParam, pushedValue);
	}

	@Override public StiXecutor getInitialState()
	{
		return initialState;
	}

	@Override public void setNextState(StiXecutor xecutor)
	{
		storage.setState(index, xecutor);
	}
}