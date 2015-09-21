package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorCallback;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

class XecutorCallback implements StiXecutorCallback
{
	final PushTarget target;
	
	public XecutorCallback(PushTarget target) 
	{ 
		this.target = target; 
	}
	
	@Override public void push(StiXecutorContext context, Object nestedSource)
	{
		((XecutorContext) context).evaluateDirectPush(target, nestedSource);
	}

	@Override public Parameter<?> getFieldParameter()
	{
		return target.getXtractorParam();
	}
}