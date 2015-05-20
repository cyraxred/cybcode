package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;

public final class JointXtractor implements BiXtractor<Object>
{
	private static final JointXtractor INSTANCE = new JointXtractor();
	
	@SuppressWarnings("unchecked") public static <T> BiXtractor<T> getInstance()
	{
		return (BiXtractor<T>) INSTANCE;
	}
	
	private JointXtractor() {}
	
	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_JUNCTION;
	}

	@Override public Object evaluate(XecutionContext context)
	{
		return context.getRootValue();
	}

	@Override public void visit(XpressionRegistrator visitor) {}

	@Override public boolean isRepeatable()
	{
		return true;
	}
	
	@Override public String toString()
	{
		return "VAR()";
	}
}