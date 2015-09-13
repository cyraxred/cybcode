package org.cybcode.stix.core.xource;

import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXourceNestedXecutor;

class StackedFieldValueHandler extends SimpleFieldValueHandler
{
	private StackedFieldValueHandler next;
	
	public StackedFieldValueHandler(StiXourceNestedXecutor<Integer> receiver)
	{
		super(receiver);
	}

	void merge(StackedFieldValueHandler prev)
	{
		if (prev == null) return;
		if (this.next != null) throw new IllegalStateException();
		if (!this.receiver.getFieldDetails().equals(prev.receiver.getFieldDetails())) throw new IllegalArgumentException();
		this.next = prev;
	}
	
	@Override public void process(StiXecutorContext context, StiXNumberedFieldXource.FieldValue value)
	{
		StackedFieldValueHandler current = this;
		do {
			receiver.push(context, value);
			current = current.next;
		} while (current != null);
	}
	
	@Override public boolean isMultiple()
	{
		return next != null;
	}
}