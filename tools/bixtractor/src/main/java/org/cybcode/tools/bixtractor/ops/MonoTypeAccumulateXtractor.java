package org.cybcode.tools.bixtractor.ops;

import java.util.Collection;

import org.cybcode.tools.bixtractor.api.BiXtractor;

abstract class MonoTypeAccumulateXtractor<T> extends AccumulateXtractor<T, T, T>
{
	public MonoTypeAccumulateXtractor(Collection<? extends BiXtractor<? extends T>> params)
	{
		super(params);
	}

	@Override protected T evaluate(T arg)
	{
		return arg;
	}
}