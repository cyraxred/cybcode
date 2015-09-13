package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorMathAgg;
import org.cybcode.stix.ops.aggregators.MutableNumber;

public class StiX_MulA extends StiXtractorMathAgg
{
	public StiX_MulA(StiXtractor<? extends Number> p0)
	{
		super(p0);
	}

	@Override protected boolean isFinalStateValue(long accumulator)
	{
		return accumulator == 0;
	}

	@Override protected boolean isFinalStateValue(double accumulator)
	{
		return accumulator == 0.0d;
	}

	@Override protected MutableNumber aggregateNextValue(MutableNumber accumulator, long p0)
	{
		return accumulator.mul(p0);
	}

	@Override protected MutableNumber aggregateNextValue(MutableNumber accumulator, double p0)
	{
		return accumulator.mul(p0);
	}
}