package org.cybcode.stix.core.xource;

import java.util.Map;

class MappedFieldContainer<F, T> implements StiXourceByTags.FieldContainer<F, T>
{
	private final F maxFieldTag;
	private final Map<F, ? extends StiXourceByTags.FieldHandler<T>> handlers;

	public MappedFieldContainer(F maxFieldTag, Map<F, ? extends StiXourceByTags.FieldHandler<T>> handlers)
	{
		this.maxFieldTag = maxFieldTag;
		this.handlers = handlers;
	}

	public F getMaxFieldTag()
	{
		return maxFieldTag;
	}

	@Override public StiXourceByTags.FieldHandler<T> findFieldHandler(F fieldId)
	{
		return handlers.get(fieldId);
	}
}