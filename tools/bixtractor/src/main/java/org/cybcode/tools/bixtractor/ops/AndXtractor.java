package org.cybcode.tools.bixtractor.ops;

import java.util.Collection;

import org.cybcode.tools.bixtractor.api.BiXtractor;

public class AndXtractor extends MonoTypeAccumulateXtractor<Boolean>
{
	public AndXtractor(Collection<BiXtractor<Boolean>> params)
	{
		super(params);
	}

	@Override protected Boolean aggregateValues(Boolean accumulator, Boolean value)
	{
		return accumulator == null ? value : (accumulator && value);
	}

	@Override protected boolean isFinalValue(Boolean accumulator)
	{
		return !accumulator;
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