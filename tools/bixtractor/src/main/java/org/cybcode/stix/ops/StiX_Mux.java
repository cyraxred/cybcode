package org.cybcode.stix.ops;

import java.util.Collection;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXpressionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xecutors.XecutorFinal;

public final class StiX_Mux<T> implements StiXtractor<T>
{
	private final PushParameter<?>[] params; //TODO ensure mandatory push
	private final boolean repeatable;

	private StiX_Mux(PushParameter<?>... params)
	{
		this.params = params;
		this.repeatable = params.length > 1 || params[0].isRepeatable();
	}

	public StiX_Mux(StiXtractor<? extends T> p0)
	{
		this(new PushParameter<T>(p0));
	}

	public StiX_Mux(StiXtractor<? extends T> p0, StiXtractor<? extends T> p1)
	{
		this(new PushParameter<T>(p0), new PushParameter<T>(p1));
	}

	public StiX_Mux(Collection<StiXtractor<? extends T>> paramList)
	{
		this(toArray(paramList));
	}

	private static PushParameter<?>[] toArray(Collection<? extends StiXtractor<?>> paramList)
	{
		if (paramList.isEmpty()) throw new IllegalArgumentException();
		PushParameter<?>[] result = new PushParameter<?>[paramList.size()];
		int i = 0;
		for (StiXtractor<?> param : paramList) {
			result[i++] = new PushParameter<>(param);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked") @Override public Class<? extends T> resultType()
	{
		Class<?> result = params[0].getSourceXtractor().resultType();
		switch (params.length) {
			case 1: break;
			case 2: result = ClassUtil.findNearestSupertype(result, params[0].getSourceXtractor().resultType()); break;
			default: {
				Class<?>[] types = new Class<?>[params.length - 1];
				for (int i = params.length - 1; i > 0; i--) {
					types[i - 1] = params[i].getSourceXtractor().resultType();
				}
				result = ClassUtil.findNearestSupertype(result, types);
			}
		}
		
		return (Class<? extends T>) result;
	}
	
	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return 1;
	}

	@Override public StiXecutor createXecutor(StiXpressionContext context)
	{
		return Xecutor.INSTANCE;
	}

	@SuppressWarnings("unchecked") @Override public T evaluate(StiXecutorContext context)
	{
		return (T) context.getAndResetInterimValue();
	}

	@Override public void visit(Visitor visitor)
	{
		for (Parameter<?> p : params) {
			visitor.visitParameter(p);
		}
	}

	@Override public int paramCount()
	{
		return params.length;
	}

	@Override public boolean isRepeatable()
	{
		return repeatable;
	}

	@SuppressWarnings("unchecked") @Override public StiXtractor<T> curry(int parameterIndex, Object value)
	{
		if (parameterIndex < 0 || parameterIndex > paramCount()) throw new IllegalArgumentException();
		if (isRepeatable() || paramCount() > 1) return null;
		return StiX_Const.of((T) value);
	}
	
	private enum Xecutor implements StiXecutor
	{
		INSTANCE;

		@Override public StiXecutor push(StiXecutorContext context, StiXtractor.Parameter<?> pushedParameter, Object pushedValue)
		{
			StiX_Mux<?> xtractor = (StiX_Mux<?>) context.getCurrentXtractor();
			if (pushedParameter.getParamIndex() >= xtractor.paramCount()) {
				throw new IllegalArgumentException("Parameter of unexpected index");
			}
			context.setInterimValue(pushedValue);
			return xtractor.isRepeatable() ? this : XecutorFinal.getInstance();
		}

		@Override public boolean canBeEvaluated()
		{
			return true;
		}
	}
}
