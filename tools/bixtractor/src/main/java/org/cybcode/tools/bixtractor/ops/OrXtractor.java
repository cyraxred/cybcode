package org.cybcode.tools.bixtractor.ops;

import java.util.Collection;

import org.cybcode.tools.bixtractor.api.BiXtractor;

class OrXtractor extends AndXtractor
{
	public OrXtractor(Collection<BiXtractor<Boolean>> params)
	{
		super(params);
	}
	
	@Override protected Boolean aggregateValues(Boolean accumulator, Boolean value)
	{
		return accumulator == null ? value : (accumulator || value);
	}

	@Override protected boolean isFinalValue(Boolean accumulator)
	{
		return accumulator;
	}
}