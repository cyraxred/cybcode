package org.cybcode.tools.bixtractor.api;

import java.util.Collection;
import java.util.Iterator;

import org.cybcode.tools.bixtractor.core.PushParameter;

public abstract class AggregateParameter<P, A> extends MonoParameter<P> implements PushParameter
{
	public AggregateParameter(BiXtractor<? extends P> extractor)
	{
		super(extractor);
	}

	@SuppressWarnings("unchecked") public boolean pushValue(XecutionContext context, Object value)
	{
		A accumulator = getAccumulator(context);
		accumulator = aggregateValues(accumulator, (P) value);
		setAccumulator(context, accumulator);
		return isFinalValue(accumulator);
	}
	
	protected abstract boolean isFinalValue(A accumulator);
	protected abstract A aggregateValues(A accumulator, P value);

	@SuppressWarnings("unchecked") protected A getAccumulator(XecutionContext context)
	{
		return (A) context.getLocalValue();
	}
	
	protected void setAccumulator(XecutionContext context, A accumulator)
	{
		context.setLocalValue(accumulator);
	}
	
	protected boolean hasAccumulator(XecutionContext context)
	{
		return context.hasLocalValue();
	}
	
	private boolean hasFinalAccumulatorValue(XecutionContext context)
	{
		return isFinalValue(getAccumulator(context));
	}
	
	public static boolean hasAllValuesOrFinalValue(XecutionContext context, Collection<? extends AggregateParameter<?, ?>> params)
	{
		Iterator<? extends AggregateParameter<?, ?>> iter = params.iterator();
		
		AggregateParameter<?, ?> param = iter.next();
		if (!param.hasAccumulator(context)) return false;
		if (param.hasFinalAccumulatorValue(context)) return true;
		
		while (true) {
			if (param.isRepeatable() || !param.has(context)) return false;
			if (!iter.hasNext()) return true;
			param = iter.next();
		}
	}

	public static <A> A evaluate(XecutionContext context, Collection<? extends AggregateParameter<?, A>> params)
	{
		Iterator<? extends AggregateParameter<?, A>> iter = params.iterator();

		AggregateParameter<?, A> param = iter.next();
		A accumulator = param.getAccumulator(context);
		if (param.isFinalValue(accumulator)) return accumulator; 
		
		while (true) {
			if (!param.has(context)) return null;
			if (!iter.hasNext()) return accumulator;
			param = iter.next();
		}
	}
}