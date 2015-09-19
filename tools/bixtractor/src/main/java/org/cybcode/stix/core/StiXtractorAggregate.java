package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;

public abstract class StiXtractorAggregate<P0, A, T> implements StiXtractor<T>
{
	private final PushParameter<P0> p0;

	public StiXtractorAggregate(StiXtractor<? extends P0> p0)
	{
		this.p0 = new PushParameter<P0>(p0) 
		{
			@Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue)
			{
				@SuppressWarnings("unchecked") final P0 pv0 = (P0) pushedValue;
				A accumOut;
				if (context.hasInterimValue()) {
					@SuppressWarnings("unchecked") A accumIn = (A) context.getInterimValue();
					accumOut = aggregateNextValue(accumIn, pv0);
				} else {
					accumOut = aggregateFirstValue(pv0);
				}
				
				if (!isRepeatable() || isFinalStateValue(accumOut)) {
					context.setFinalState();
					return calculate(accumOut);
				} else {
					context.setInterimValue(accumOut);
					return null;
				}
			}
		};
	}
	
	@Override public T apply(StiXecutorContext context)
	{
		if (!context.hasInterimValue()) return null; //no values in, no values out
		
		@SuppressWarnings("unchecked") A accum = (A) context.getInterimValue();
		T result = calculate(accum);
		return result;
	}
	
	@Override public int paramCount()
	{
		return 1;
	}
	
	@Override public boolean isRepeatable()
	{
		return false;
	}

	protected abstract boolean isFinalStateValue(A accumulator);
	protected abstract A aggregateFirstValue(P0 p0);
	protected abstract A aggregateNextValue(A accumulator, P0 p0);
	protected abstract T calculate(A accumulator);

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return null;
	}
	
	@Override public StiXtractor<T> curry(int parameterIndex, Object value)
	{
		if (parameterIndex != 0) throw new IllegalArgumentException();
		return null;
	}

	@Override public void visit(StiXtractor.Visitor visitor)
	{
		visitor.visitParameter(p0);
	}

	/**
	 * @param pushedValue 
	 * @return true when the final state was reached
	 */
	protected boolean aggregateNextAndIsFinal(StiXecutorContext context, Object pushedValue)
	{
		@SuppressWarnings("unchecked") final P0 pv0 = (P0) pushedValue;
		A accumOut;
		if (context.hasInterimValue()) {
			@SuppressWarnings("unchecked") A accumIn = (A) context.getInterimValue();
			accumOut = aggregateNextValue(accumIn, pv0);
		} else {
			accumOut = aggregateFirstValue(pv0);
		}
		context.setInterimValue(accumOut);
		return !p0.isRepeatable() || isFinalStateValue(accumOut);
	}
}
