package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXtractor.Parameter;

class XecutorContextRunnerStatelessNode extends XecutorContextRunnerFramedNode
{
	public XecutorContextRunnerStatelessNode(XecutorContextNode contextNode, RunnerFrame frame)
	{
		super(contextNode, frame);
	}

	@Override protected Object internalEvaluateFinal()
	{
		return getCurrentXtractor().apply(getXecutorContext());
	}
	
	@Override protected Object internalEvaluatePush(Parameter<?> targetParam, Object pushedValue)
	{
		return targetParam.evaluatePush(this, pushedValue);
	}
	
	@Override public StiXecutor getInitialState()
	{
		return null;
	}
	
	@Override public void setNextState(StiXecutor xecutor)
	{
		throw new IllegalStateException();
	}
}