package org.cybcode.stix.core.xecutors;

import java.util.Arrays;

import org.cybcode.stix.api.StiXecutor;

public abstract class AbstractXecutorContextStorage
{
	private final StiXecutor[] states;

	public AbstractXecutorContextStorage(int nodeCount)
	{
		states = new StiXecutor[nodeCount];
	}
	
	public abstract void setResultValue(int index, Object value);
//	public abstract void setResultValue(int index, long value);
//	public abstract void setResultValue(int index, double value);

	public abstract void setInterimValue(int index, Object value);

	public void setState(int index, StiXecutor state)
	{
		if (state == null) throw new NullPointerException();
		if (isFinalState(index)) throw new IllegalStateException();
		states[index] = state;
	}

	public void setFinalState(int index)
	{
		setState(index, DefaultXecutors.FINAL);
	}
	
	public abstract Object getResultValue(int index);
//	public abstract long getResultValueLong(int index);
//	public abstract double getResultValueDouble(int index);
//	public abstract Class<?> getResultValueType(int index);
	
	public abstract Object getInterimValue(int index);

	public StiXecutor getState(int index)
	{
		return states[index];
	}
	
	public boolean isFinalState(int index)
	{
		return states[index] == DefaultXecutors.FINAL;
	}
	
	protected void ensureNotInterim(int index)
	{
		if (hasInterimValue(index)) throw new IllegalStateException();
	}
	
	protected void ensureNotFinal(int index)
	{
		if (isFinalState(index)) throw new IllegalStateException();
	}

	protected void ensureFinal(int index)
	{
		if (!isFinalState(index)) throw new IllegalStateException();
	}

	public abstract boolean hasInterimValue(int index);

	public void clearRange(int startIndex, int endIndex, StiXecutor state)
	{
		Arrays.fill(states, startIndex, endIndex, state);
	}

	public void clear(int index, StiXecutor state)
	{
		states[index] = state;
	}
}