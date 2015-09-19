package org.cybcode.stix.api;

public interface StiXecutorContext extends StiXParamContext
{
	Object getInterimValue();
	void setInterimValue(Object value);
	boolean hasInterimValue();

	StiXtractor<?> getCurrentXtractor();

	boolean hasResultValue();
}