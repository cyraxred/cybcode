package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorBoolDuo;

public class StiX_Or extends StiXtractorBoolDuo implements StiXtractor.Commutative
{
	public StiX_Or(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1)
	{
		super(p0, p1);
	}

	public StiX_Or(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1, boolean defaultParamValue)
	{
		super(p0, p1, defaultParamValue);
	}

	@Override protected boolean calculate(boolean p0, boolean p1)
	{
		return p0 || p1;
	}

	@Override protected boolean isPushToFinal(int parameterIndex, boolean value)
	{
		return value;
	}

	@Override protected StiXtractor<Boolean> curry(boolean value, StiXtractor<Boolean> otherParam)
	{
		if (value) return StiX_Const.of(true);
		return otherParam;
	}
}