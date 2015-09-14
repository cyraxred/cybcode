package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorDuo;

public class StiX_NullIf<T> extends StiXtractorDuo<T, T, T>
{
	public StiX_NullIf(StiXtractor<? extends T> pValue, StiXtractor<? extends T> pValueForNull)
	{
		super(pValue, pValueForNull);
	}

	@Override public Class<? extends T> resultType()
	{
		@SuppressWarnings("unchecked") Class<? extends T> result = (Class<? extends T>) 
			ClassUtil.findNearestSupertype(p0.getSourceXtractor().resultType(), p1.getSourceXtractor().resultType());
		return result;
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 10);
	}

	@Override protected T calculate(T p0, T p1)
	{
		if (p0 == null || p0.equals(p1)) return null;
		return p0;
	}
}