package org.cybcode.tools.bixtractor.pbuf;

class PbufSingletonContext implements PbufContext
{
	private final int fieldId;
	private final PbufFieldReceiver receiver;

	public PbufSingletonContext(int fieldId, PbufFieldReceiver receiver)
	{
		this.fieldId = fieldId;
		this.receiver = receiver;
	}

	@Override public PbufFieldReceiver getFieldReceiver(int fieldId)
	{
		if (fieldId != this.fieldId) return null;
		return receiver;
	}
	
	@Override public int getMaxField()
	{
		return fieldId;
	}
}