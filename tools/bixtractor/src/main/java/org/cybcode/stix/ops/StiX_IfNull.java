package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorDuo;
import org.cybcode.tools.type.ClassUtil;

public class StiX_IfNull<T> extends StiXtractorDuo<T, T, T>
{
	public StiX_IfNull(StiXtractor<? extends T> pValue, StiXtractor<? extends T> pValueForNull)
	{
		super(pValue, true, pValueForNull, false);
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
	
	@Override protected T evaluatePushP0(StiXecutorPushContext context, T pushedValue)
	{
		if (pushedValue == null) return null;
		context.setFinalState();
		return pushedValue;
	}
	
	@Override protected T calculate(T p0, T p1)
	{
		if (p0 != null) return p0;
		return p1;
	}
}