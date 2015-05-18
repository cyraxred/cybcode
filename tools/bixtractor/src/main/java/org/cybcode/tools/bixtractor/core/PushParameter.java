package org.cybcode.tools.bixtractor.core;

import org.cybcode.tools.bixtractor.api.XecutionContext;

public interface PushParameter
{
	public boolean pushValue(XecutionContext context, Object value);
}