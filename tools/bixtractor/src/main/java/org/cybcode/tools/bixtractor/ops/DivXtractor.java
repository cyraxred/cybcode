package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.BiXtractor;

public class DivXtractor extends BiFunctionXtractor<Number, Number, Number>
{
	public DivXtractor(BiXtractor<? extends Number> p0, BiXtractor<? extends Number> p1)
	{
		super(p0, p1);
	}

	@Override protected Number evaluate(Number p0, Number p1)
	{
		return p0.doubleValue() / p1.doubleValue();
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