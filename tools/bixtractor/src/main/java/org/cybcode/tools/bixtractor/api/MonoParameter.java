package org.cybcode.tools.bixtractor.api;


public class MonoParameter<P> extends AbstractMonoParameter<P>
{
	public MonoParameter(BiXtractor<? extends P> extractor)
	{
		super(extractor);
	}
}