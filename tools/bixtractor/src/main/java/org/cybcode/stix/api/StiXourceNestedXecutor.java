package org.cybcode.stix.api;

public interface StiXourceNestedXecutor<T>
{
	T getFieldDetails();
	void push(StiXecutorContext context, Object nestedSource);
}