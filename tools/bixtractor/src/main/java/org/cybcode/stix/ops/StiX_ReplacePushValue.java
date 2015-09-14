package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorMonoPush;

public class StiX_ReplacePushValue<T> extends StiXtractorMonoPush<Object, T>
{
	private final T value;

	public StiX_ReplacePushValue(StiXtractor<?> p0, T value)
	{
		super(p0);
		this.value = value;
	}
	
	@Override public Object getOperationToken()
	{
		return value;
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 1);
	}

	@Override protected T calculate(Object p0)
	{
		return value;
	}
	
	@SuppressWarnings("unchecked") @Override public Class<? extends T> resultType()
	{
		return (Class<? extends T>) value.getClass();
	}
}