package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorDuo;

public class StiX_Eq extends StiXtractorDuo<Object, Object, Boolean> implements StiXtractor.Commutative
{
	public StiX_Eq(StiXtractor<?> p0, StiXtractor<?> p1)
	{
		super(p0, p1);
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 10);
	}

	@Override protected Boolean calculate(Object p0, Object p1)
	{
		if (p0 == null || p1 == null) return null;
		return p0.equals(p1);
	}

	@Override public Class<Boolean> resultType()
	{
		return Boolean.class;
	}
}