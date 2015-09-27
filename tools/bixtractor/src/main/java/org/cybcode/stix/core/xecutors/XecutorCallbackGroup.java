package org.cybcode.stix.core.xecutors;

import java.util.Arrays;
import java.util.List;

import org.cybcode.stix.api.StiXecutorCallback;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXpressionNode.PushTarget;
import org.cybcode.stix.api.StiXtractor.Parameter;

class XecutorCallbackGroup implements StiXecutorCallback
{
	private final List<PushTarget> targets;
	
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
		this.targets = Arrays.asList(targetArray);
	}
	
	@Override public void push(StiXecutorPushContext context, Object pushedValue)
	{
		((XecutorRunnerNode) context).evaluateDirectPush(targets, pushedValue);
	}

	@Override public Parameter<?> getFieldParameter()
	{
		throw new UnsupportedOperationException();
	}
}