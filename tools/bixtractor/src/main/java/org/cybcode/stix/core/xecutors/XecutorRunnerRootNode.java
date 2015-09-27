package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXtractor.Parameter;

class XecutorRunnerRootNode extends XecutorRunnerFramedNode
{
	public XecutorRunnerRootNode(XecutorContextNode contextNode, XecutorRunnerFrame outermostFrame)
	{
		super(contextNode, outermostFrame, DefaultXecutors.FINAL);
	}

	@Override protected Object internalEvaluateFinal()
	{
		throw new IllegalStateException();
	}
	
	@Override protected Object internalEvaluatePush(Parameter<?> targetParam, Object pushedValue)
	{
		throw new IllegalStateException();
	}
}