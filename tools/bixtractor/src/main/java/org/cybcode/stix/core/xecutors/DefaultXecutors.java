package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

enum DefaultXecutors implements StiXecutor 
{
	FINAL,
	
	FAIL {
		@Override public Object evaluatePush(StiXecutorPushContext context, Parameter<?> pushedParameter, Object pushedValue)
		{
			throw new XecutorFailException();
		}
	},
	
	FRAME_START,
	FRAME_STARTED,
	FRAME_RESULT,
	;

	@Override public Object evaluatePush(StiXecutorPushContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		throw new IllegalStateException();
	}
	
	@Override public Object evaluateFinal(StiXecutorPushContext context)
	{
		return context.getCurrentXtractor().apply(context.getXecutorContext());
	}
}