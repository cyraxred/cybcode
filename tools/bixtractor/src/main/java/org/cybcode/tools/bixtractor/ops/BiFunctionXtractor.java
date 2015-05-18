package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.MonoParameter;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;

abstract class BiFunctionXtractor<P0, P1, R> implements BiXtractor<R>
{
	private final MonoParameter<? extends P0> p0;
	private final MonoParameter<? extends P1> p1;

	protected BiFunctionXtractor(BiXtractor<? extends P0> p0, BiXtractor<? extends P1> p1)
	{
		this.p0 = new MonoParameter<>(p0);
		this.p1 = new MonoParameter<>(p1);
	}

	@Override public void visit(XpressionRegistrator registry)
	{
		registry.registerParameter(p0);
		registry.registerParameter(p1);
	}
	
	@Override public R evaluate(XecutionContext context)
	{
		return evaluate(p0.get(context), p1.get(context));
	}
	
	protected abstract R evaluate(P0 p0, P1 p1);
}