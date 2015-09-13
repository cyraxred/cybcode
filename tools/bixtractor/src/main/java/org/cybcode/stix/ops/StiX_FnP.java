package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.FunctionWithComplexity;
import org.cybcode.stix.core.StiXtractorMonoPush;

import com.google.common.base.Function;

public class StiX_FnP<P0, T> extends StiXtractorMonoPush<P0, T>
{
	private Function<P0, T>	fn;
	private int	complexity;

	public StiX_FnP(StiXtractor<? extends P0> p0, Function<P0, T> fn, int complexity)
	{
		super(p0);
		if (fn == null) throw new NullPointerException();
		if (complexity <= 0) throw new IllegalArgumentException();
		this.fn = fn;
		this.complexity = complexity;
	}

	public StiX_FnP(StiXtractor<? extends P0> p0, FunctionWithComplexity<P0, T> fn)
	{
		this(p0, fn, fn.getOperationComplexity());
	}

	@Override public Object getOperationToken()
	{
		return fn;
	}

	@Override public int getOperationComplexity()
	{
		return complexity;
	}

	@Override protected T calculate(P0 p0)
	{
		return fn.apply(p0);
	}
}