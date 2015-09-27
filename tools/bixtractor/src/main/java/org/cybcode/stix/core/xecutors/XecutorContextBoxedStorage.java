package org.cybcode.stix.core.xecutors;

import java.util.Arrays;

import org.cybcode.stix.api.StiXecutor;

public final class XecutorContextBoxedStorage extends AbstractXecutorContextStorage
{
	private final Object[] values;

	public XecutorContextBoxedStorage(int nodeCount)
	{
		super(nodeCount);
		values = new Object[nodeCount];
	}

	@Override public void setResultValue(int index, Object value)
	{
		values[index] = value;
	}

//	@Override public void setResultValue(int index, long value)
//	{
//		values[index] = value;
//	}
//
//	@Override public void setResultValue(int index, double value)
//	{
//		values[index] = value;
//	}

	@Override public void setInterimValue(int index, Object value)
	{
		ensureNotFinal(index);
		values[index] = value;
	}

	@Override public Object getResultValue(int index)
	{
		ensureNotInterim(index);
		return values[index];
	}

//	@Override public long getResultValueLong(int index)
//	{
//		ensureNotInterim(index);
//		return ((Number) values[index]).longValue();
//	}
//
//	@Override public double getResultValueDouble(int index)
//	{
//		ensureNotInterim(index);
//		return ((Number) values[index]).doubleValue();
//	}
//
//	@Override public Class<?> getResultValueType(int index)
//	{
//		ensureNotInterim(index);
//		Object value = values[index];
//		if (value == null) return null;
//		return value.getClass();
//	}

	@Override public void clearRange(int startIndex, int endIndex, StiXecutor state)
	{
		super.clearRange(startIndex, endIndex, state);
		Arrays.fill(values, startIndex, endIndex, null);
	}

	@Override public void clear(int index, StiXecutor state)
	{
		super.clear(index, state);
		values[index] = null;
	}
	
	@Override public Object getInterimValue(int index)
	{
		ensureNotFinal(index);
		return values[index];
	}

	@Override public boolean hasInterimValue(int index)
	{
		if (isFinalState(index)) return false;
		return values[index] != null;
	}
	
	@Override protected void ensureNotInterim(int index)
	{
	}
}