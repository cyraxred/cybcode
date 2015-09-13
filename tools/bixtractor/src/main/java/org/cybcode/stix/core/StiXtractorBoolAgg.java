package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXtractor;

public abstract class StiXtractorBoolAgg extends StiXtractorAggregate<Boolean, Boolean, Boolean>
{
	public StiXtractorBoolAgg(StiXtractor<? extends Boolean> p0)
	{
		super(p0);
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return 5;
	}

	@Override protected Boolean aggregateFirstValue(Boolean p0)
	{
		return p0;
	}

	@Override protected Boolean calculate(Boolean accumulator)
	{
		return accumulator;
	}
}