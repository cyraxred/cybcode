package org.cybcode.tools.bixtractor.pbuf;

import org.cybcode.tools.bixtractor.api.BiXourceContext;

interface PbufContext extends BiXourceContext
{
	PbufFieldReceiver getFieldReceiver(int fieldId);
	int getMaxField();
}