package org.cybcode.tools.bixtractor.api;

import org.cybcode.tools.bixtractor.core.PushParameter;

public abstract class AggregateParameter<P, A> extends MonoParameter<P> implements PushParameter
{
	public AggregateParameter(BiXtractor<? extends P> extractor)
	{
		super(extractor);
	}

	@SuppressWarnings("unchecked") public boolean pushValue(XecutionContext context, Object value)
	{
		A accumulator = (A) context.getLocalValue();
		accumulator = aggregateValues(accumulator, (P) value);
		context.setLocalValue(accumulator);
		return isFinalValue(accumulator);
	}
	
	protected abstract boolean isFinalValue(A accumulator);
	protected abstract A aggregateValues(A accumulator, P value);

	@SuppressWarnings("unchecked") public A getAccumulator(XecutionContext context)
	{
		return (A) context.getLocalValue();
	}
}