package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

enum DefaultXecutors implements StiXecutor 
{
	FINAL, PUSH_FINAL;

	@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		throw new IllegalStateException();
	}
	
	@Override public boolean isPushOrFinal()
	{
		return true;
	}
}