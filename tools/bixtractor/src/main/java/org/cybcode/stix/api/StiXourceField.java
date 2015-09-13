package org.cybcode.stix.api;

import org.cybcode.stix.core.xecutors.XecutorMono;
import org.cybcode.stix.core.xecutors.XecutorMonoPush;

public abstract class StiXourceField<P0, T> implements StiXtractor<T>
{
	public enum ValueMode { SINGLE, FIRST, LAST, MULTIPLE }
	
	private final Parameter<P0> p0;
	private final ValueMode mode;

	public static <P0, T> StiXourceField<P0, T> newInstance(StiXource<?, ?, ?, P0> p0, ValueMode mode, StiXFunction<P0, T> transform)
	{
		return new StiXourceFnField<P0, T>(p0, mode, transform);
	}
	
	public static <P0, T> StiXourceField<P0, T> newRepeatedValue(StiXource<?, ?, ?, P0> p0, StiXFunction<P0, T> transform)
	{
		return newInstance(p0, ValueMode.MULTIPLE, transform);
	}
	
	public static <P0, T> StiXourceField<P0, T> newSingleValue(StiXource<?, ?, ?, P0> p0, StiXFunction<P0, T> transform)
	{
		return newInstance(p0, ValueMode.SINGLE, transform);
	}
	
	public StiXourceField(StiXource<?, ?, ?, P0> p0, ValueMode mode)
	{
		this.p0 = mode.equals(ValueMode.FIRST) ? new Parameter<P0>(p0) : new PushParameter<P0>(p0);
		this.mode = mode;
	}

	@Override public void visit(StiXtractor.Visitor visitor)
	{
		visitor.visitParameter(p0);
	}
	
	@Override public Object getOperationToken()
	{
		return mode;
	}
	
	@Override public boolean isRepeatable()
	{
		return mode.equals(ValueMode.MULTIPLE);
	}

	@Override public StiXecutor createXecutor(StiXpressionContext context)
	{
		switch (mode) {
			case MULTIPLE: return XecutorMonoPush.getInstance();
			case FIRST: return XecutorMono.getInstance();
			case LAST: //TODO
			case SINGLE: //TODO
			default: 
				throw new UnsupportedOperationException(); 
		}
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
