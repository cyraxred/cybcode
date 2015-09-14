package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorMonoPush;

public class StiX_FnP<P0, T> extends StiXtractorMonoPush<P0, T>
{
	private StiXFunction<P0, T>	fn;

	public StiX_FnP(StiXtractor<? extends P0> p0, StiXFunction<P0, T> fn)
	{
		super(p0);
		if (fn == null) throw new NullPointerException();
		this.fn = fn;
	}

	@Override public Object getOperationToken()
	{
		return fn.getOperationToken();
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(fn, fn.getOperationComplexity());
	}

	@Override protected T calculate(P0 p0)
	{
		return fn.apply(p0);
	}

	@Override public Class<? extends T> resultType()
	{
		return fn.resultType();
	}
}