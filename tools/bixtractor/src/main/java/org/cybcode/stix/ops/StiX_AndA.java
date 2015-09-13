package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorBoolAgg;

public class StiX_AndA extends StiXtractorBoolAgg
{
	public StiX_AndA(StiXtractor<? extends Boolean> p0)
	{
		super(p0);
	}

	@Override protected boolean isFinalStateValue(Boolean accumulator)
	{
		return Boolean.FALSE.equals(accumulator);
	}

	@Override protected Boolean aggregateNextValue(Boolean accumulator, Boolean p0)
	{
		return accumulator && p0;
	}
}