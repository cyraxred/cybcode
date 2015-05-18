package org.cybcode.tools.bixtractor.api;

import org.cybcode.tools.bixtractor.core.Parameter;

public class MonoParameter<P> extends Parameter<P>
{
	public MonoParameter(BiXtractor<? extends P> extractor)
	{
		super(extractor);
	}

	@SuppressWarnings("unchecked") public P get(XecutionContext context)
	{
		return (P) super.get(context); 
	}
}