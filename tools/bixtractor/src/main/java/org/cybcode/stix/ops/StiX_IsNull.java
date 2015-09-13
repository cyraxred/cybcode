package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorMono;

public class StiX_IsNull extends StiXtractorMono<Object, Boolean>
{
	public StiX_IsNull(StiXtractor<? extends Object> p0)
	{
		super(p0);
	}

	@Override public Class<? extends Boolean> resultType()
	{
		return Boolean.class;
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return 10;
	}

	@Override protected Boolean calculate(Object p0)
	{
		return p0 == null;
	}
}