package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorDuo;

public class StiX_IfNull<T> extends StiXtractorDuo<T, T, T>
{
	public StiX_IfNull(StiXtractor<? extends T> pValue, StiXtractor<? extends T> pValueForNull)
	{
		super(new NotifyParameter<>(pValue), new Parameter<>(pValueForNull));
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

	@Override public int getOperationComplexity()
	{
		return 10;
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