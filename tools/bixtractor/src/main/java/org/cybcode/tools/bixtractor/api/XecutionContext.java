package org.cybcode.tools.bixtractor.api;


public interface XecutionContext 
{
	Object getRootValue();
	
	Object getParamValue(int paramIndex);
	boolean hasParamValue(int paramIndex);
	
	Object getLocalValue();
	void setLocalValue(Object value);
	boolean hasLocalValue();
	
	BiXourceContext getSourceContext();

}