package org.cybcode.stix.api;

public enum ParameterBehavior
{
	REGULAR, 
	NOTIFY_ON_FINAL, NEVER_NOTIFY,
	PUSH_ALL { public boolean isMandatory() { return true; }}, 
	CALLBACK { public boolean isMandatory() { return true; }};
	
	public boolean isMandatory() { return false; }
}