package org.cybcode.tools.bixtractor.ops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cybcode.tools.bixtractor.api.AggregateParameter;
import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;

abstract class AccumulateXtractor<P, A, R> implements BiXtractor<R>
{
	private final List<AggregateParameter<P, A>> p0;

	protected AccumulateXtractor(Collection<? extends BiXtractor<? extends P>> params)
	{
		this.p0 = new ArrayList<>(params.size());
		for (BiXtractor<? extends P> param : params) {
			p0.add(new AggregateParameter<P, A>(param)
			{
				@Override protected boolean isFinalValue(A accumulator)
				{
					return accumulator == null || AccumulateXtractor.this.isFinalValue(accumulator);
				}
	
				@Override protected A aggregateValues(A accumulator, P value)
				{
					if (value == null) return null;
					return AccumulateXtractor.this.aggregateValues(accumulator, value);
				}
			});
		}
	}

	protected abstract A aggregateValues(A accumulator, P value);
	protected abstract boolean isFinalValue(A accumulator);

	@Override public void visit(XpressionRegistrator registry)
	{
		for (AggregateParameter<P, A> param : p0) {
			registry.registerParameter(param);
		}
	}
	
	@Override public R evaluate(XecutionContext context)
	{
		A accumulator = AggregateParameter.evaluate(context, p0);
		if (accumulator == null) return null;
		return evaluate(accumulator);
	}
	
	protected abstract R evaluate(A arg);

	@Override public String toString()
	{
		return XtractorFormatter.toString(this, p0);
	}
}