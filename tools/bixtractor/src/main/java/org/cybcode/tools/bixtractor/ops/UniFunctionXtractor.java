package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.AbstractMonoParameter;
import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.MonoParameter;
import org.cybcode.tools.bixtractor.api.MonoPushParameter;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;

public abstract class UniFunctionXtractor<P0, R> implements BiXtractor<R>
{
	private final AbstractMonoParameter<P0> p0;

	protected UniFunctionXtractor(BiXtractor<? extends P0> p0, boolean usePush)
	{
		this.p0 = usePush ? new MonoPushParameter<P0>(p0) : new MonoParameter<P0>(p0); 
	}

	@Override public void visit(XpressionRegistrator registry)
	{
		registry.registerParameter(p0);
	}
	
	@Override public R evaluate(XecutionContext context)
	{
		return evaluate(p0.get(context));
	}

	@Override public boolean isRepeatable()
	{
		return p0.isRepeatable();
	}
	
	protected abstract R evaluate(P0 p0);
	
	@Override public String toString()
	{
		return XtractorFormatter.toString(this, p0);
	}
}