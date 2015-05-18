package org.cybcode.tools.bixtractor.pbuf;

import org.cybcode.tools.bixtractor.api.XecutionContext;

interface PbufFieldReceiver
{
	boolean pushFieldValue(XecutionContext context, PbufFieldValue value);
}