package org.cybcode.tools.bixtractor.api;


public interface XecutionContext 
{
	Object getRootValue();
	
	Object getParamValue(int paramIndex);
	
	Object getLocalValue();
	void setLocalValue(Object value);
	
	BiXourceContext getSourceContext();
}