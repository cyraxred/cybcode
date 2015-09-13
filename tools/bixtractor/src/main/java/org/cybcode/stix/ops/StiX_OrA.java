package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorBoolAgg;

public class StiX_OrA extends StiXtractorBoolAgg
{
	public StiX_OrA(StiXtractor<? extends Boolean> p0)
	{
		super(p0);
	}

	@Override protected boolean isFinalStateValue(Boolean accumulator)
	{
		return Boolean.TRUE.equals(accumulator);
	}

	@Override protected Boolean aggregateNextValue(Boolean accumulator, Boolean p0)
	{
		return accumulator || p0;
	}
}