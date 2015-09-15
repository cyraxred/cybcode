package org.cybcode.stix.core.xource;

import org.cybcode.stix.api.StiXecutorCallback;

abstract class SimpleFieldValueHandler implements StiXNumberedFieldXource.FieldHandler
{
	protected final StiXecutorCallback<Integer> receiver;

	public SimpleFieldValueHandler(StiXecutorCallback<Integer> receiver)
	{
		this.receiver = receiver;
	}
}