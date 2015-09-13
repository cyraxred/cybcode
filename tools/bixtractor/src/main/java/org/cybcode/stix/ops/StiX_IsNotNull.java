package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;

public class StiX_IsNotNull extends StiX_IsNull
{
	public StiX_IsNotNull(StiXtractor<? extends Object> p0)
	{
		super(p0);
	}

	@Override protected Boolean calculate(Object p0)
	{
		return p0 != null;
	}
}