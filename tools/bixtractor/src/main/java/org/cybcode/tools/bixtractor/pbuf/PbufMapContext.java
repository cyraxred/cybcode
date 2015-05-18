package org.cybcode.tools.bixtractor.pbuf;

import java.util.Map;

class PbufMapContext implements PbufContext
{
	private final int maxFieldId;
	private final Map<Integer, ? extends PbufFieldReceiver> receivers;

	public PbufMapContext(int maxFieldId, Map<Integer, ? extends PbufFieldReceiver> receivers)
	{
		this.maxFieldId = maxFieldId;
		this.receivers = receivers;
	}

	@Override public PbufFieldReceiver getFieldReceiver(int fieldId)
	{
		return receivers.get(fieldId);
	}

	@Override public int getMaxField()
	{
		return maxFieldId;
	}
}