package org.cybcode.stix.api;

public interface StiXParamContext
{
	Object getParamValue(int paramIndex);
	boolean hasParamFinalValue(int paramIndex);
}