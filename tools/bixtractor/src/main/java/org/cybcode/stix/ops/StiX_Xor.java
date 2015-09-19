package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorBoolDuo;

public class StiX_Xor extends StiXtractorBoolDuo implements StiXtractor.Commutative
{
	public StiX_Xor(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1)
	{
		super(p0, p1);
	}

	@Override protected boolean calculate(boolean p0, boolean p1)
	{
		return p0 ^ p1;
	}

	@Override protected Boolean calculatePartial(boolean p)
	{
		return null;
	}

	@Override protected StiXtractor<Boolean> curry(boolean value, StiXtractor<Boolean> otherParam)
	{
		if (value) return otherParam;
		return new StiX_Fn<>(otherParam, StiX_Fns.NOT);
	}
}