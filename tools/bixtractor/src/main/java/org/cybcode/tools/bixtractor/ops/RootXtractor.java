package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;

public class RootXtractor implements BiXtractor<Object>
{
	private static final RootXtractor INSTANCE = new RootXtractor();
	
	@SuppressWarnings("unchecked") public static <T> BiXtractor<T> getInstance()
	{
		return (BiXtractor<T>) INSTANCE;
	}
	
	private RootXtractor() {}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_VAR;
	}

	@Override public Object evaluate(XecutionContext context)
	{
		return context.getRootValue();
	}

	@Override public void visit(XpressionRegistrator visitor) {}

	@Override public String toString()
	{
		return "VAR()";
	}
}