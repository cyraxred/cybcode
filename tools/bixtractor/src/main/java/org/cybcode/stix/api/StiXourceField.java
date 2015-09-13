package org.cybcode.stix.api;

import org.cybcode.stix.core.xecutors.XecutorMono;
import org.cybcode.stix.core.xecutors.XecutorMonoPush;

import com.google.common.base.Function;

public abstract class StiXourceField<P0, T> implements StiXtractor<T>
{
	private final Parameter<P0> p0;

	public static <P0, T> StiXourceField<P0, T> newInstance(StiXource<?, ?, ?, P0> p0, boolean repeatable, final Function<P0, T> transform, final int complexity)
	{
		return new StiXourceTransformingField<P0, T>(p0, repeatable, transform, complexity);
	}
	
	public static <P0, T> StiXourceField<P0, T> newInstance(StiXource<?, ?, ?, P0> p0, boolean repeatable, final Function<P0, T> transform)
	{
		return newInstance(p0, repeatable, transform, COMPLEXITY_SOURCE_FIELD_CONVERSION);
	}
	
	public static <P0, T> StiXourceField<P0, T> newInstance(StiXource<?, ?, ?, P0> p0, final Function<P0, T> transform)
	{
		return newInstance(p0, false, transform);
	}
	
	public StiXourceField(StiXource<?, ?, ?, P0> p0, boolean allowRepeatedParamValue)
	{
		this.p0 = allowRepeatedParamValue ? new PushParameter<P0>(p0) : new Parameter<P0>(p0);
	}

	@Override public void visit(StiXtractor.Visitor visitor)
	{
		visitor.visitParameter(p0);
	}
	
	@Override public boolean isRepeatable()
	{
		return p0.isRepeatable();
	}

	@Override public StiXecutor createXecutor(StiXpressionContext context)
	{
		return isRepeatable() ? XecutorMonoPush.getInstance() : XecutorMono.getInstance();
	}

	@Override public T evaluate(StiXecutorContext context)
	{
		P0 pv0 = p0.getValue(context);
		if (pv0 == null) return null;
		return transform(pv0);
	}

	protected abstract T transform(P0 pv0);

	@Override public int paramCount()
	{
		return 1;
	}

	@Override public StiXtractor<T> curry(int parameterIndex, Object value)
	{
		if (parameterIndex != 0) throw new IllegalArgumentException();
		return null;
	}
}