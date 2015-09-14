package org.cybcode.stix.api;

import org.cybcode.stix.core.StiXtractorMonoInterim;

public abstract class StiXourceField<P0, T> extends StiXtractorMonoInterim<P0, T>
{
	public enum ValueMode { SINGLE, FIRST, LAST, MULTIPLE }
	
	private final ValueMode mode;

	public static <P0, T> StiXourceField<P0, T> newInstance(StiXource<?, ?, ?, P0> p0, ValueMode mode, StiXFunction<P0, T> transform)
	{
		return new StiXourceFnField<P0, T>(p0, mode, transform);
	}
	
	public static <P0, T> StiXourceField<P0, T> newRepeatedValue(StiXource<?, ?, ?, P0> p0, StiXFunction<P0, T> transform)
	{
		return newInstance(p0, ValueMode.MULTIPLE, transform);
	}
	
	public static <P0, T> StiXourceField<P0, T> newSingleValue(StiXource<?, ?, ?, P0> p0, StiXFunction<P0, T> transform)
	{
		return newInstance(p0, ValueMode.SINGLE, transform);
	}
	
	public static <P0, T> StiXourceField<P0, T> newFirstValue(StiXource<?, ?, ?, P0> p0, StiXFunction<P0, T> transform)
	{
		return newInstance(p0, ValueMode.FIRST, transform);
	}
	
	public static <P0, T> StiXourceField<P0, T> newLastValue(StiXource<?, ?, ?, P0> p0, StiXFunction<P0, T> transform)
	{
		return newInstance(p0, ValueMode.LAST, transform);
	}
	
	public StiXourceField(StiXource<?, ?, ?, P0> p0, ValueMode mode)
	{
		super(p0);
		if (mode == null) throw new NullPointerException();
		this.mode = mode;
	}

	@Override public Object getOperationToken()
	{
		return mode;
	}
	
	@Override public boolean isRepeatable()
	{
		return mode == ValueMode.MULTIPLE && p0.isRepeatable();
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		switch (mode) {
			case MULTIPLE: return createXecutorPushEach();
			case FIRST: return createXecutorStoreFirst();
			case LAST: return createXecutorStoreEach();
			case SINGLE: return createXecutorStoreFirstOnly();
			default: 
				throw new UnsupportedOperationException(); 
		}
	}
}
