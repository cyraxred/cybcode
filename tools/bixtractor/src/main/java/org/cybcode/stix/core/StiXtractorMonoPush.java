package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXtractor;

public abstract class StiXtractorMonoPush<P0, T> extends StiXtractorMono<P0, T>
{
	public StiXtractorMonoPush(StiXtractor<? extends P0> p0)
	{
		super(new PushParameter<>(p0));
	}
}