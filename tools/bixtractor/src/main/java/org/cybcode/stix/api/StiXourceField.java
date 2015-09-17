package org.cybcode.stix.api;

import org.cybcode.stix.core.StiXtractorLimiter;

public abstract class StiXourceField<P0, T> extends StiXtractorLimiter<P0, Void, T>
{
	public static <P0, T> StiXourceField<P0, T> newInstance(StiXource<?, ?, ?, P0> p0, ValueLimit mode, StiXFunction<P0, T> transform)
	{
		return new StiXourceFnField<P0, T>(p0, mode, transform);
	}
	
	public static <P0, T> StiXourceField<P0, T> newRepeatedValue(StiXource<?, ?, ?, P0> p0, StiXFunction<P0, T> transform)
	{
		return newInstance(p0, ValueLimit.ALL, transform);
	}
	
	public static <P0, T> StiXourceField<P0, T> newSingleValue(StiXource<?, ?, ?, P0> p0, StiXFunction<P0, T> transform)
	{
		return newInstance(p0, ValueLimit.SINGLE, transform);
	}
	
	public static <P0, T> StiXourceField<P0, T> newFirstValue(StiXource<?, ?, ?, P0> p0, StiXFunction<P0, T> transform)
	{
		return newInstance(p0, ValueLimit.FIRST, transform);
	}
	
	public static <P0, T> StiXourceField<P0, T> newLastValue(StiXource<?, ?, ?, P0> p0, StiXFunction<P0, T> transform)
	{
		return newInstance(p0, ValueLimit.LAST, transform);
	}
	
	public StiXourceField(StiXource<?, ?, ?, P0> p0, ValueLimit mode)
	{
		super(p0, mode);
	}

	@SuppressWarnings("unchecked") @Override public T evaluate(StiXecutorContext context)
	{
		return calculate((P0) context.getInterimValue());
	}

	@Override protected final Void prepareContext(StiXecutorConstructionContext context)
	{
		return null;
	}
	
	@Override protected StiXecutor processPush(StiXecutorContext context, Void info, Parameter<?> pushedParameter, Object pushedValue)
	{
		if (pushedValue == null) return null;
		context.setInterimValue(pushedValue);
		return getXecutor();
	}
}
