package org.cybcode.stix.api;

public interface StiXecutor
{
	public static final Object KEEP_LAST_VALUE = new Object();
	
	Object evaluatePush(StiXecutorPushContext context, StiXtractor.Parameter<?> pushedParameter, Object pushedValue);
	Object evaluateFinal(StiXecutorPushContext context);
}