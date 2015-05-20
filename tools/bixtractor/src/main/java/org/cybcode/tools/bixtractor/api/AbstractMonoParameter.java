package org.cybcode.tools.bixtractor.api;

import org.cybcode.tools.bixtractor.core.Parameter;

public abstract class AbstractMonoParameter<P> extends Parameter<P>
{
	public AbstractMonoParameter(BiXtractor<? extends P> extractor)
	{
		super(extractor);
	}

	@SuppressWarnings("unchecked") public P get(XecutionContext context)
	{
		return (P) super.get(context); 
	}
}