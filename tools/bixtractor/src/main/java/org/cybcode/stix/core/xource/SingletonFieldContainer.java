package org.cybcode.stix.core.xource;

class SingletonFieldContainer<F, T> implements StiXourceByTags.FieldContainer<F, T> 
{
	private final SingleFieldHandler<T> handler;
	private final F fieldTag;

	@SuppressWarnings("unchecked") public SingletonFieldContainer(SingleFieldHandler<T> handler)
	{
		this.handler = handler;
		this.fieldTag = (F) handler.getFieldDetails(); 
	}

	@Override public StiXourceByTags.FieldHandler<T> findFieldHandler(F fieldId)
	{
		if (this.fieldTag.equals(fieldId)) return handler;
		return null;
	}

	@Override public F getMaxFieldTag()
	{
		return fieldTag; 
	}
}