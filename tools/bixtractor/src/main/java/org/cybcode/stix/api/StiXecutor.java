package org.cybcode.stix.api;

public interface StiXecutor
{
	StiXecutor push(StiXecutorContext context, StiXtractor.Parameter<?> pushedParameter, Object pushedValue);
	boolean canBeEvaluated();
}