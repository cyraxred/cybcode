package org.cybcode.stix.core.xource;

import org.cybcode.stix.api.StiXecutorCallback;

abstract class SingleFieldHandler<T> implements StiXourceByTags.FieldHandler<T>
{
	protected final StiXecutorCallback callback;

	protected SingleFieldHandler(StiXecutorCallback callback)
	{
		this.callback = callback;
	}
	
	public Object getFieldDetails()
	{
		return StiXourceByTags.getFieldDetails(callback.getFieldParameter()); 
	}
}