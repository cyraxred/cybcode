package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.api.StiXecutorCallback;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

class XecutorCallbackGroup implements StiXecutorCallback
{
	private final PushTarget[] targets;
	
	@SuppressWarnings("unchecked") public static XecutorCallbackGroup newInstance(List<StiXecutorCallback> targets)  
	{
		return new XecutorCallbackGroup((List<XecutorCallback>) (List<? extends StiXecutorCallback>) targets);
	}
	
	private XecutorCallbackGroup(List<XecutorCallback> targets) 
	{ 
		PushTarget[] targetArray = new PushTarget[targets.size()];
		for (int i = targetArray.length - 1; i >= 0; i--) {
			targetArray[i] = targets.get(i).target;
		}
		this.targets = targetArray;
	}
	
	@Override public void push(StiXecutorContext context, Object nestedSource)
	{
		((XecutorContext) context).evaluateDirectPush(targets, nestedSource);
	}

	@Override public Parameter<?> getFieldParameter()
	{
		throw new UnsupportedOperationException();
	}
}