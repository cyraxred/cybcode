package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;

public abstract class XecutorFail implements StiXecutor
{
	private XecutorFail() {}

	public static StiXecutor getInstance()
	{
		return DefaultXecutors.FAIL;
	}
}
