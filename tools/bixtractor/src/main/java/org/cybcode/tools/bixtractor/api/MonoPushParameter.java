package org.cybcode.tools.bixtractor.api;

import org.cybcode.tools.bixtractor.core.PushParameter;

public class MonoPushParameter<P> extends AbstractMonoParameter<P> implements PushParameter
{
	public MonoPushParameter(BiXtractor<? extends P> extractor)
	{
		super(extractor);
	}

	@Override public boolean pushValue(XecutionContext context, Object value)
	{
		return true;
	}
}