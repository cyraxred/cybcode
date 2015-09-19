package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

class FrameStartXecutor implements StiXecutor
{
	public static final StiXecutor INSTANCE = new FrameStartXecutor();
	
	@Override public Object evaluatePush(StiXecutorPushContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		((StiXecutorDefaultContext) context).enterFrame();
		context.setFinalState();
		return pushedValue;
	}

	@Override public Object evaluateFinal(StiXecutorPushContext context)
	{
		((StiXecutorDefaultContext) context).enterFrame();
		context.setFinalState();
		return context.getCurrentXtractor().apply(context);
	}
}