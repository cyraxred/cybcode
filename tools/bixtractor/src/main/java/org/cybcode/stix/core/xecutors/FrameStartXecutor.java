package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

class FrameStartXecutor implements StiXecutor
{
	public static final StiXecutor INSTANCE = new FrameStartXecutor();
	
	@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		((StiXecutorDefaultContext) context).enterFrame();
		return DefaultXecutors.FINAL;
	}

	@Override public boolean isPushOrFinal() { return false; }
}