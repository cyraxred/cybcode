package org.cybcode.stix.core.xource;

import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorCallback;
import org.cybcode.stix.core.xource.StiXNumberedFieldXource.FieldHandler;
import org.cybcode.stix.core.xource.StiXNumberedFieldXource.FieldValue;

class SingletonFieldHandler extends SimpleFieldValueHandler implements StiXNumberedFieldXource.FieldContainer 
{
	public SingletonFieldHandler(StiXecutorCallback<Integer> receiver)
	{
		super(receiver);
	}

	@Override public int getMaxFieldId()
	{
		return receiver.getFieldDetails();
	}

	@Override public FieldHandler findFieldHandler(int fieldId)
	{
		if (fieldId != receiver.getFieldDetails().intValue()) return null;
		return this;
	}

	@Override public boolean isMultiple()
	{
		return false;
	}

	@Override public void process(StiXecutorContext context, FieldValue value)
	{
		receiver.push(context, value);
	}
}