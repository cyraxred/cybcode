package org.cybcode.stix.core.xource;

import java.util.Map;

import org.cybcode.stix.core.xource.StiXNumberedFieldXource.FieldHandler;

class MappedFieldHandler implements StiXNumberedFieldXource.FieldContainer
{
	private final int maxFieldId;
	private final Map<Integer, ? extends StiXNumberedFieldXource.FieldHandler> receivers;

	public MappedFieldHandler(int maxFieldId, Map<Integer, ? extends StiXNumberedFieldXource.FieldHandler> receivers)
	{
		this.maxFieldId = maxFieldId;
		this.receivers = receivers;
	}

	@Override public int getMaxFieldId()
	{
		return maxFieldId;
	}
	
	@Override public FieldHandler findFieldHandler(int fieldId)
	{
		return receivers.get(fieldId);
	}
}