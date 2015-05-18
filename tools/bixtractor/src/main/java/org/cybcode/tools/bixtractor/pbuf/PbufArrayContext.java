package org.cybcode.tools.bixtractor.pbuf;

import java.util.Map;


class PbufArrayContext implements PbufContext
{
	private final int baseId;
	private final PbufFieldReceiver[] receivers;

	public PbufArrayContext(int baseId, PbufFieldReceiver[] receivers)
	{
		this.baseId = baseId;
		this.receivers = receivers;
	}

	public PbufArrayContext(int minFieldId, int maxFieldId, Map<Integer, ? extends PbufFieldReceiver> receivers)
	{
		this.baseId = minFieldId;
		this.receivers = new PbufFieldReceiver[maxFieldId - minFieldId + 1];
		for (Map.Entry<Integer, ? extends PbufFieldReceiver> receiverEntry : receivers.entrySet()) {
			this.receivers[receiverEntry.getKey() - minFieldId] = receiverEntry.getValue();
		}
	}
	
	@Override public PbufFieldReceiver getFieldReceiver(int fieldId)
	{
		fieldId -= baseId;
		if (fieldId < 0 || fieldId > receivers.length) return null;
		return receivers[fieldId];
	}
	
	@Override public int getMaxField()
	{
		return baseId + receivers.length - 1;
	}
}