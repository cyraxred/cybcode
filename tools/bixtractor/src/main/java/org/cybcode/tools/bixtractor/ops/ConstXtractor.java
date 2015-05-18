package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;

public class ConstXtractor<T> implements BiXtractor<T>
{
	private final T value;

	public ConstXtractor(T value)
	{
		this.value = value;
	}

	@Override public Object getOperationToken()
	{
		return value;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_CONSTANT;
	}

	@Override public T evaluate(XecutionContext context)
	{
		return value;
	}

	@Override public void visit(XpressionRegistrator visitor)
	{
	}
}