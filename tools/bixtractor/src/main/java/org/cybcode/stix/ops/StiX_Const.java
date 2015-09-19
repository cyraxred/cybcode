package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;

public final class StiX_Const<T> implements StiXtractor<T>
{
	private final T value;
	
	public static <T> StiX_Const<T> of(T value)
	{
		return new StiX_Const<T>(value);
	}
	
	private StiX_Const(T value)
	{
		this.value = value;
	}

	@Override public boolean isRepeatable()
	{
		return false;
	}
	
	@Override public Object getOperationToken()
	{
		return value;
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 1);
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return null;
	}
	
	@Override public T apply(StiXecutorContext context)
	{
		return value;
	}

	@Override public int paramCount()
	{
		return 0;
	}
	
	@Override public void visit(StiXtractor.Visitor visitor) {}

	@Override public StiXtractor<T> curry(int parameterIndex, Object value)
	{
		throw new IllegalArgumentException();
	}
	
	public T getValue()
	{
		return value;
	}

	@SuppressWarnings("unchecked") @Override public Class<? extends T> resultType()
	{
		return (Class<? extends T>) value.getClass();
	}
}