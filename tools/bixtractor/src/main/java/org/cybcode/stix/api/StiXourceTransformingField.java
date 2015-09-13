package org.cybcode.stix.api;

import com.google.common.base.Function;

public class StiXourceTransformingField<P0, T> extends StiXourceField<P0, T>
{
	private final Function<P0, T>	transform;
	private final int complexity;

	public StiXourceTransformingField(StiXource<?, ?, ?, P0> p0, boolean repeatable, Function<P0, T> transform, int complexity)
	{
		super(p0, repeatable);
		this.transform = transform;
		this.complexity = complexity;
	}

	@Override public Object getOperationToken()
	{
		return transform;
	}

	@Override public int getOperationComplexity()
	{
		return complexity;
	}

	@Override protected T transform(P0 pv0)
	{
		return transform.apply(pv0);
	}
}