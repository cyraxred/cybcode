package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;

public enum VerbatimXtractor implements BiXtractor<Object>
{
	INSTANCE;
	
	@SuppressWarnings("unchecked") public static <T> BiXtractor<T> getInstance()
	{
		return (BiXtractor<T>) INSTANCE;
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_VERBATIM;
	}

	@Override public Object evaluate(XecutionContext context)
	{
		return context.getRootValue();
	}

	@Override public void visit(XpressionRegistrator visitor) {}
}