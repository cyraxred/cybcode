package org.cybcode.stix.ops;

import java.util.Collection;

import org.cybcode.stix.api.OutputMode;
import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.tools.type.ClassUtil;

public final class StiX_Mux<T> implements StiXtractor<T>, StiXtractor.Commutative
{
	private final MuxParameter<?>[] params; //TODO ensure mandatory push
	private final boolean repeatable;

	private static class MuxParameter<T> extends PushParameter<T>
	{
		public MuxParameter(StiXtractor<? extends T> source) { super(source); }
		
		@SuppressWarnings("unchecked") @Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue)
		{
			if (!context.getCurrentXtractor().getOutputMode().isRepeatable()) {
				context.setFinalState();
			}
			return (T) pushedValue;
		}
	}
	
	private StiX_Mux(MuxParameter<?>... params)
	{
		this.params = params;
		this.repeatable = params.length > 1 || params[0].isRepeatable();
	}

	public StiX_Mux(StiXtractor<? extends T> p0)
	{
		this(new MuxParameter<T>(p0));
	}

	public StiX_Mux(StiXtractor<? extends T> p0, StiXtractor<? extends T> p1)
	{
		this(new MuxParameter<T>(p0), new MuxParameter<T>(p1));
	}

	public StiX_Mux(Collection<StiXtractor<? extends T>> paramList)
	{
		this(toArray(paramList));
	}

	private static MuxParameter<?>[] toArray(Collection<? extends StiXtractor<?>> paramList)
	{
		if (paramList.isEmpty()) throw new IllegalArgumentException();
		MuxParameter<?>[] result = new MuxParameter<?>[paramList.size()];
		int i = 0;
		for (StiXtractor<?> param : paramList) {
			result[i++] = new MuxParameter<>(param);
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

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 5);
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return null;
	}

	@Override public T apply(StiXecutorContext context)
	{
		return null;
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

	@Override public OutputMode getOutputMode()
	{
		return OutputMode.pushMode(repeatable);
	}
	
	@SuppressWarnings("unchecked") @Override public StiXtractor<T> curry(int parameterIndex, Object value)
	{
		if (parameterIndex < 0 || parameterIndex > paramCount()) throw new IllegalArgumentException();
		if (getOutputMode().isRepeatable() || paramCount() > 1) return null;
		return StiX_Const.of((T) value);
	}
}
