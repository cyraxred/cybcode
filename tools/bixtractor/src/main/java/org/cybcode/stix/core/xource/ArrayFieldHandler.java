package org.cybcode.stix.core.xource;

import java.util.Map;

import org.cybcode.stix.core.xource.StiXNumberedFieldXource.FieldHandler;

class ArrayFieldHandler implements StiXNumberedFieldXource.FieldContainer
{
	private final int baseId;
	private final StiXNumberedFieldXource.FieldHandler[] receivers;

	public ArrayFieldHandler(int baseId, StiXNumberedFieldXource.FieldHandler[] receivers)
	{
		this.baseId = baseId;
		this.receivers = receivers;
	}

	public ArrayFieldHandler(int minFieldId, int maxFieldId, Map<Integer, ? extends StiXNumberedFieldXource.FieldHandler> receivers)
	{
		this.baseId = minFieldId;
		this.receivers = new StiXNumberedFieldXource.FieldHandler[maxFieldId - minFieldId + 1];
		for (Map.Entry<Integer, ? extends StiXNumberedFieldXource.FieldHandler> receiverEntry : receivers.entrySet()) {
			this.receivers[receiverEntry.getKey() - minFieldId] = receiverEntry.getValue();
		}
	}

	@Override public FieldHandler prepare(int pFieldId)
	{
		int fieldId = pFieldId - baseId;
		if (fieldId < 0 || fieldId > receivers.length) return null;
		return receivers[fieldId];
	}
	
	@Override public int getMaxFieldId()
	{
		return baseId + receivers.length - 1;
	}
}