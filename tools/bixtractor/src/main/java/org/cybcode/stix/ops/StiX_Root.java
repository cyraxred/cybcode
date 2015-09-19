package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;

public final class StiX_Root<T> implements StiXtractor<T>
{
	public static StiX_Root<Object> INSTANCE = new StiX_Root<Object>();
	
	@SuppressWarnings("unchecked") public static <T> StiX_Root<T> getInstance()
	{
		return (StiX_Root<T>) INSTANCE;
	}
	
	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return 1;
	}
	
	@Override public boolean isRepeatable()
	{
		return false;
	}
	
	@Override public Class<? extends T> resultType()
	{
		return null; //TODO depends on context, think about it - will impact multiplex operation
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return null;
	}

	@Override public T apply(StiXecutorContext context) 
	{
		throw new UnsupportedOperationException();
	}
	
	@Override public void visit(StiXtractor.Visitor visitor) {}

	@Override public int paramCount()
	{
		return 0;
	}

	@Override public StiXtractor<T> curry(int parameterIndex, Object value)
	{
		throw new IllegalArgumentException();
	}
}
