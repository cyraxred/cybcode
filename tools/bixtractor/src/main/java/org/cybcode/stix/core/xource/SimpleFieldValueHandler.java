package org.cybcode.stix.core.xource;

import org.cybcode.stix.api.StiXourceNestedXecutor;

abstract class SimpleFieldValueHandler implements StiXNumberedFieldXource.FieldHandler
{
	protected final StiXourceNestedXecutor<Integer> receiver;

	public SimpleFieldValueHandler(StiXourceNestedXecutor<Integer> receiver)
	{
		this.receiver = receiver;
	}
}