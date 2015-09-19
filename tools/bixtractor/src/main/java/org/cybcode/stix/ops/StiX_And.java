package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorBoolDuo;

public class StiX_And extends StiXtractorBoolDuo implements StiXtractor.Commutative
{
	public StiX_And(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1)
	{
		super(p0, p1);
	}

	public StiX_And(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1, boolean defaultParamValue)
	{
		super(p0, p1, defaultParamValue);
	}

	@Override protected boolean calculate(boolean p0, boolean p1)
	{
		return p0 && p1;
	}
	
	@Override protected Boolean calculatePartial(boolean p)
	{
		if (!p) return Boolean.FALSE;
		return null;
	}
	
	@Override protected StiXtractor<Boolean> curry(boolean value, StiXtractor<Boolean> otherParam)
	{
		if (value) return otherParam;
		return StiX_Const.of(false);
	}
}