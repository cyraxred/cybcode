package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutorCallback;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXpressionNode.PushTarget;
import org.cybcode.stix.api.StiXtractor.Parameter;

class XecutorCallback implements StiXecutorCallback
{
	final PushTarget target;
	
	public XecutorCallback(PushTarget target) 
	{ 
		this.target = target; 
	}
	
	@Override public void push(StiXecutorPushContext context, Object nestedSource)
	{
		((XecutorRunnerNode) context).evaluateDirectPush(target, nestedSource);
	}

	@Override public Parameter<?> getFieldParameter()
	{
		return target.getXtractorParam();
	}
}