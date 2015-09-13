package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXpressionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xecutors.XecutorFinal;

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

	@Override public int getOperationComplexity()
	{
		return 1;
	}

	@Override public StiXecutor createXecutor(StiXpressionContext context)
	{
		return XecutorFinal.getInstance();
	}
	
	@Override public T evaluate(StiXecutorContext context)
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