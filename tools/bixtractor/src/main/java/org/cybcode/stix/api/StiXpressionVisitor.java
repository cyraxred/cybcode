package org.cybcode.stix.api;

public interface StiXpressionVisitor
{
	void visitParameter(StiXtractor.Parameter<?> param);	
}