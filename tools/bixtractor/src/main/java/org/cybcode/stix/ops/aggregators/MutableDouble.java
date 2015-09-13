package org.cybcode.stix.ops.aggregators;

public class MutableDouble extends MutableNumber
{
	private double	value;

	public MutableDouble(double value)
	{
		this.value = value;
	}

	@Override public boolean isFloatingPoint()
	{
		return true;
	}

	@Override public boolean hasValue()
	{
		return !Double.isNaN(value);
	}

	@Override public double doubleValue()
	{
		return value;
	}

	@Override public long longValue()
	{
		return (long) value;
	}

	@Override public MutableDouble add(long p)
	{
		value += p;
		return this;
	}

	@Override public MutableDouble add(double p)
	{
		value += p;
		return this;
	}

	@Override public MutableDouble mul(long p)
	{
		value *= p;
		return this;
	}

	@Override public MutableDouble mul(double p)
	{
		value *= p;
		return this;
	}
}