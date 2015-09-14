package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorMathDuo;

public class StiX_Mul extends StiXtractorMathDuo implements StiXtractor.Commutative
{
	public StiX_Mul(StiXtractor<? extends Number> p0, StiXtractor<? extends Number> p1)
	{
		super(p0, p1);
	}

	@Override protected long calculate(long p0, long p1) throws ArithmeticException
	{
		return p0 * p1;
	}

	@Override protected double calculate(double p0, double p1) throws ArithmeticException
	{
		return p0 * p1;
	}
}