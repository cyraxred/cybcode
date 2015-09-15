package org.cybcode.stix.api;

public interface StiXecutorCallback
{
	StiXtractor.Parameter<?> getFieldParameter();
	void push(StiXecutorContext context, Object nestedSource);
}