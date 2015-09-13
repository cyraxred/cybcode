package org.cybcode.stix.ops.aggregators;

public abstract class MutableNumber
{
	public abstract boolean isFloatingPoint();
	public abstract boolean hasValue();
	public abstract double doubleValue();
	public abstract long longValue();
	public abstract MutableNumber add(long p);
	public abstract MutableNumber add(double p);
	public abstract MutableNumber mul(long p);
	public abstract MutableNumber mul(double p);
}

