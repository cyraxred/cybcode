package org.cybcode.stix.core.xource;

import org.cybcode.stix.api.StiXecutorCallback;
import org.cybcode.stix.api.StiXecutorPushContext;

class StackedFieldHandler<T> extends SingleFieldHandler<T>
{
	private StackedFieldHandler<T> next;
	
	public StackedFieldHandler(StiXecutorCallback callback)
	{
		super(callback);
	}

	void merge(StackedFieldHandler<T> prev)
	{
		if (prev == null) return;
		if (this.next != null) throw new IllegalStateException();
		if (!this.getFieldDetails().equals(prev.getFieldDetails())) throw new IllegalArgumentException();
		this.next = prev;
	}
	
	@Override public void process(StiXecutorPushContext context, T value)
	{
		StackedFieldHandler<T> current = this;
		do {
			current.callback.push(context, value);
			current = current.next;
		} while (current != null);
	}
	
	@Override public boolean isMultiple()
	{
		return next != null;
	}
}