package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

class XecutorStartFrame implements StiXecutor
{
	private final CtxFrame frame;
	private final StiXecutor startedState; 
	
	public XecutorStartFrame(CtxFrame frame)
	{
		if (frame == null) throw new NullPointerException();
		this.frame = frame;
		startedState = new XecutorStartedFrame(frame);
	}

	@Override public Object evaluatePush(StiXecutorPushContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		if (pushedValue == null) return null;
		context.setNextState(startedState);
		return pushedValue;
	}

	@Override public Object evaluateFinal(StiXecutorPushContext context)
	{
		((XecutorContextRunner) context).skipFrame(frame);
		return null;
	}
}

class XecutorStartedFrame implements StiXecutor
{
	private final CtxFrame frame;
	
	public XecutorStartedFrame(CtxFrame frame)
	{
		if (frame == null) throw new NullPointerException();
		this.frame = frame;
	}

	@Override public Object evaluatePush(StiXecutorPushContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		throw new IllegalStateException();
	}

	@Override public Object evaluateFinal(StiXecutorPushContext context)
	{
		((XecutorContextRunner) context).enterFrame(frame);
		return KEEP_LAST_VALUE;
	}
}