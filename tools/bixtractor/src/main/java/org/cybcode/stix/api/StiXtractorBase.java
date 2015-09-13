package org.cybcode.stix.api;

public interface StiXtractorBase
{
	Object getOperationToken();
	int getOperationComplexity();
	Class<?> resultType();
}