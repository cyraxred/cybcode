package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXtractor.Parameter;

class XecutorContextRunnerRootNode extends XecutorContextRunnerStatelessNode
{
	public XecutorContextRunnerRootNode(XecutorContextNode contextNode, RunnerFrame outermostFrame)
	{
		super(contextNode, outermostFrame);
	}

	@Override protected Object internalEvaluateFinal()
	{
		throw new IllegalStateException();
	}
	
	@Override protected Object internalEvaluatePush(Parameter<?> targetParam, Object pushedValue)
	{
		throw new IllegalStateException();
	}
	
	@Override public StiXecutor getInitialState()
	{
		return DefaultXecutors.FINAL;
	}
}