package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;

public abstract class XecutorFinal implements StiXecutor 
{
	private XecutorFinal() {}
	
	public static StiXecutor getInstance()
	{
		return DefaultXecutors.FINAL;
	}
}
