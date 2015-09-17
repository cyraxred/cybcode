package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorMathAgg;
import org.cybcode.tools.mutable.MutableNumber;

public class StiX_AddA extends StiXtractorMathAgg
{
	public StiX_AddA(StiXtractor<? extends Number> p0)
	{
		super(p0);
	}

	@Override protected boolean isFinalStateValue(long accumulator)
	{
		return false;
	}

	@Override protected boolean isFinalStateValue(double accumulator)
	{
		return false;
	}

	@Override protected MutableNumber aggregateNextValue(MutableNumber accumulator, long p0)
	{
		return accumulator.add(p0);
	}

	@Override protected MutableNumber aggregateNextValue(MutableNumber accumulator, double p0)
	{
		return accumulator.add(p0);
	}
}