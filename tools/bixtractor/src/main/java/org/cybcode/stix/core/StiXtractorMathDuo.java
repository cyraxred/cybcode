package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXtractor;

public abstract class StiXtractorMathDuo extends StiXtractorDuo<Number, Number, Number>
{
	protected StiXtractorMathDuo(StiXtractor<? extends Number> p0, StiXtractor<? extends Number> p1)
	{
		super(p0, p1);
	}

	protected StiXtractorMathDuo(Parameter<Number> p0, Parameter<Number> p1)
	{
		super(p0, p1);
	}
	
	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return 10;
	}

	@Override public Class<Number> resultType()
	{
		return Number.class;
	}
	
	protected abstract long calculate(long p0, long p1) throws ArithmeticException;
	protected abstract double calculate(double p0, double p1) throws ArithmeticException;
	
	protected boolean isFloatPoint(Number p0, Number p1)
	{
		return !(p0 instanceof Long) || !(p1 instanceof Long); 
	}
	
	@Override protected Number calculate(Number p0, Number p1)
	{
		if (p0 == null || p1 == null) return null;
		try {
			if (isFloatPoint(p0, p1)) {
				double result = calculate(p0.doubleValue(), p1.doubleValue());
				if (Double.isNaN(result)) return null;
				return result;
			} else {
				return calculate(p0.longValue(), p1.longValue());
			}
		} catch (ArithmeticException e) {
			return null;
		}
	}
}
