package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.ops.aggregators.MutableDouble;
import org.cybcode.stix.ops.aggregators.MutableLong;
import org.cybcode.stix.ops.aggregators.MutableNumber;

public abstract class StiXtractorMathAgg extends StiXtractorAggregate<Number, MutableNumber, Number>
{
	public StiXtractorMathAgg(StiXtractor<? extends Number> p0)
	{
		super(p0);
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 20);
	}
	
	@Override public Class<Number> resultType()
	{
		return Number.class;
	}

	protected abstract boolean isFinalStateValue(long accumulator);
	protected abstract boolean isFinalStateValue(double accumulator);
	
	@Override protected boolean isFinalStateValue(MutableNumber accumulator)
	{
		if (accumulator == null) return false;
		if (accumulator.isFloatingPoint()) return !accumulator.hasValue() || isFinalStateValue(accumulator.doubleValue());
		return isFinalStateValue(accumulator.longValue());
	}

	@Override protected MutableNumber aggregateFirstValue(Number p0)
	{
		if (p0 instanceof Long) return new MutableLong(p0.longValue());
		if (Double.isNaN(p0.doubleValue())) return null;
		return new MutableDouble(p0.doubleValue());
	}

	protected abstract MutableNumber aggregateNextValue(MutableNumber accumulator, long p0);
	protected abstract MutableNumber aggregateNextValue(MutableNumber accumulator, double p0);
	
	@Override protected MutableNumber aggregateNextValue(MutableNumber accumulator, Number p0)
	{
		if (accumulator == null) return aggregateFirstValue(p0);
		if (p0 instanceof Long) return aggregateNextValue(accumulator, p0.longValue());
		return aggregateNextValue(accumulator, p0.doubleValue());
	}

	protected Number calculate(long accumulator) 
	{
		return accumulator;
	}
	
	protected Number calculate(double accumulator)
	{
		return accumulator;
	}
	
	@Override protected Number calculate(MutableNumber accumulator)
	{
		if (accumulator == null || !accumulator.hasValue()) return null;
		if (accumulator.isFloatingPoint()) return calculate(accumulator.doubleValue());
		return calculate(accumulator.longValue());
	}
}
