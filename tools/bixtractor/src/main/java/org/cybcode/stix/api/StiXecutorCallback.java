package org.cybcode.stix.api;

public interface StiXecutorCallback
{
	StiXtractor.Parameter<?> getFieldParameter();
	void push(StiXecutorPushContext context, Object pushedValue);
}