package org.cybcode.stix.api;

import org.cybcode.stix.core.Multiplicity;
import org.cybcode.stix.core.StiXtractorLimiter;

public class StiXourceField<P0, T> extends StiXtractorLimiter<P0, T>
{
	public StiXourceField(StiXource<?, ?, ?, P0> p0, Multiplicity mode, StiXFunction<P0, ? extends T> fn)
	{
		super(p0, mode, fn);
	}
}