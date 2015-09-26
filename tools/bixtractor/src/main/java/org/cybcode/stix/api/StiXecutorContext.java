package org.cybcode.stix.api;


public interface StiXecutorContext extends StiXParamContext
{
	StiXtractor<?> getCurrentXtractor();

	Object getInterimValue();
	boolean hasInterimValue();
}