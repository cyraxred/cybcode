package org.cybcode.stix.ops.aggregators;

public class MutableLong extends MutableNumber
{
	private long	value;

	public MutableLong(long value)
	{
		this.value = value;
	}

	@Override public boolean isFloatingPoint()
	{
		return false;
	}

	@Override public boolean hasValue()
	{
		return true;
	}
	
	@Override public double doubleValue()
	{
		return value;
	}

	@Override public long longValue()
	{
		return (long) value;
	}

	@Override public MutableLong add(long p)
	{
		value += p;
		return this;
	}

	@Override public MutableDouble add(double p)
	{
		return new MutableDouble(value).add(p);
	}

	@Override public MutableLong mul(long p)
	{
		value *= p;
		return this;
	}

	@Override public MutableDouble mul(double p)
	{
		return new MutableDouble(value).mul(p);
	}
}