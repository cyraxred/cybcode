package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

class XecutorStartFrame implements StiXecutor
{
	private final CtxFrame frame;
	
	public XecutorStartFrame(CtxFrame frame)
	{
		if (frame == null) throw new NullPointerException();
		this.frame = frame;
	}

	@Override public Object evaluatePush(StiXecutorPushContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		if (pushedParameter.getParamIndex() != 0) return new IllegalArgumentException(); //this is a derived dependency parameter, it must be disabled for push.
		
		context.setFinalState();
		((XecutorContext) context).enterFrame(frame);
		return pushedValue;
	}

	@Override public Object evaluateFinal(StiXecutorPushContext context)
	{
		((XecutorContext) context).skipFrame(frame);
		return null;
	}
}