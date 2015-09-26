package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

class XecutorResolveFrame implements StiXecutor
{
	private final CtxFrame.ResultMarker marker;
	
	public XecutorResolveFrame(CtxFrame.ResultMarker frame)
	{
		if (frame == null) throw new NullPointerException();
		this.marker = frame;
	}

	@Override public Object evaluatePush(StiXecutorPushContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		context.setFinalState();
		((XecutorContextRunner) context).resolveFrame(marker, pushedParameter.isRepeatable());
		return pushedValue;
	}

	@Override public Object evaluateFinal(StiXecutorPushContext context)
	{
		((XecutorContextRunner) context).resolveFrame(marker, false);
		return null;
	}
}