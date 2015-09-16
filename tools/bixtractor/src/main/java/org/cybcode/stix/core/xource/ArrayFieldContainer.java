package org.cybcode.stix.core.xource;

import java.util.Map;

class ArrayFieldContainer<T> implements StiXourceByTags.FieldContainer<Integer, T>
{
	private final int baseId;
	private final StiXourceByTags.FieldHandler<T>[] handlers;
	private final Integer maxFieldTag;

	public ArrayFieldContainer(int baseId, StiXourceByTags.FieldHandler<T>[] handlers)
	{
		this.baseId = baseId;
		this.handlers = handlers;
		this.maxFieldTag = baseId + handlers.length - 1;
	}

	@SuppressWarnings("unchecked")
	public ArrayFieldContainer(int minFieldId, int maxFieldId, Map<Integer, ? extends StiXourceByTags.FieldHandler<T>> handlers)
	{
		this.baseId = minFieldId;
		this.maxFieldTag = maxFieldId;
		this.handlers = new StiXourceByIntTags.FieldHandler[maxFieldId - minFieldId + 1];
		for (Map.Entry<Integer, ? extends StiXourceByTags.FieldHandler<T>> receiverEntry : handlers.entrySet()) {
			this.handlers[receiverEntry.getKey() - minFieldId] = receiverEntry.getValue();
		}
	}

	@Override public StiXourceByTags.FieldHandler<T> findFieldHandler(Integer fieldId)
	{
		if (fieldId > maxFieldTag) return null;
		int index = fieldId - baseId;
		if (index < 0) return null;
		return handlers[index];
	}

	@Override public Integer getMaxFieldTag()
	{
		return maxFieldTag;
	}
}