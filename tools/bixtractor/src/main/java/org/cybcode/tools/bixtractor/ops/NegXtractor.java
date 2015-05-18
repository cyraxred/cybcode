package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.BiXtractor;

public class NegXtractor extends UniFunctionXtractor<Number, Number>
{
	public NegXtractor(BiXtractor<? extends Number> p0)
	{
		super(p0, false);
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_OPERATION;
	}

	@Override protected Number evaluate(Number p0)
	{
		return - p0.doubleValue();
	}
}