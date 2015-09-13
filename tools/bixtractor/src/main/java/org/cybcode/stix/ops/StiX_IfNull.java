package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;

public class StiX_IfNull<T> extends StiX_NullIf<T>
{
	public StiX_IfNull(StiXtractor<? extends T> pValue, StiXtractor<? extends T> pValueForNull)
	{
		super(pValue, pValueForNull);
	}

	@Override protected boolean isPushToFinal(int parameterIndex, Object value)
	{
		return parameterIndex == 0 && value != null;
	}
	
	@Override protected T calculate(T p0, T p1)
	{
		if (p0 != null) return p0;
		return p1;
	}
}