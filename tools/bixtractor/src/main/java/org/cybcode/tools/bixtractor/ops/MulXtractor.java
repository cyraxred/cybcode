package org.cybcode.tools.bixtractor.ops;

import java.util.Collection;

import org.cybcode.tools.bixtractor.api.BiXtractor;

public class MulXtractor extends MonoTypeAccumulateXtractor<Number>
{
	public MulXtractor(Collection<? extends BiXtractor<? extends Number>> params)
	{
		super(params);
	}

	@Override protected Number aggregateValues(Number accumulator, Number value)
	{
		return accumulator == null ? value : (accumulator.doubleValue() * value.doubleValue());
	}

	@Override protected boolean isFinalValue(Number accumulator)
	{
		double v = accumulator.doubleValue();
		return v == 0d || Double.isNaN(v);
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_OPERATION;
	}
}