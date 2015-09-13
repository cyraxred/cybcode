package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXpressionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xecutors.XecutorDuo;
import org.cybcode.stix.ops.StiX_Const;

public abstract class StiXtractorDuo<P0, P1, T> implements StiXtractor<T>
{
	private final static XecutorDuo XECUTOR = new XecutorDuo(0, 1) 
	{
		@Override protected boolean isPushToFinal(StiXecutorContext context, StiXtractor.Parameter<?> pushedParameter, Object pushedValue) 
		{
			return ((StiXtractorDuo<?, ?, ?>) context.getCurrentXtractor()).isPushToFinal(context, pushedParameter, pushedValue);
		};
	};
	
	protected final Parameter<P0> p0;
	protected final Parameter<P1> p1;

	public StiXtractorDuo(StiXtractor<? extends P0> p0, StiXtractor<? extends P1> p1)
	{
		this.p0 = new Parameter<P0>(p0);
		this.p1 = new Parameter<P1>(p1);
	}
	
	StiXtractorDuo(Parameter<P0> p0, Parameter<P1> p1)
	{
		this.p0 = p0;
		this.p1 = p1;
	}
	
	@Override public final boolean isRepeatable()
	{
		return false;
	}
	
	@Override public final T evaluate(StiXecutorContext context)
	{
		P0 pv0 = p0.getValue(context);
		P1 pv1 = p1.getValue(context);
		T result = calculate(pv0, pv1);
		return result;
	}

	protected abstract T calculate(P0 p0, P1 p1);

	protected boolean isPushToFinal(int parameterIndex, Object value) 
	{
		return false;
	};
	
	private boolean isPushToFinal(StiXecutorContext context, StiXtractor.Parameter<?> pushedParameter, Object pushedValue) 
	{
		int index = pushedParameter.getParamIndex();
		switch (index) {
			case 0:
			case 1:
				return isPushToFinal(index, pushedValue);
			default:
				throw new IllegalArgumentException();
		}
	}
	
	@Override public StiXecutor createXecutor(StiXpressionContext context)
	{
		return XECUTOR;
	}
	
	@Override public int paramCount()
	{
		return 2;
	}

	@Override public void visit(StiXtractor.Visitor visitor)
	{
		visitor.visitParameter(p0);
		visitor.visitParameter(p1);
	}

	@Override public StiXtractor<? extends T> curry(int parameterIndex, Object value)
	{
		switch (parameterIndex) {
			case 0: {
				@SuppressWarnings("unchecked") final P0 pv0 = (P0) value;
				
				if (isPushToFinal(parameterIndex, value)) {
					T result = calculate(pv0, null); 
					return StiX_Const.of(result);
				}
				return new CurriedP0<P0, P1, T>(this, pv0, p1);
			}
			case 1: {
				@SuppressWarnings("unchecked") final P1 pv1 = (P1) value;
				
				if (isPushToFinal(parameterIndex, value)) {
					T result = calculate(null, pv1); 
					return StiX_Const.of(result);
				}
				return new CurriedP1<P0, P1, T>(this, p0, pv1);
			}
				
			default:
				throw new IllegalArgumentException();
		}
	}
	
	private static abstract class CurriedDuo<V, P, O extends StiXtractor<?>, T> extends StiXtractorMono<P, T>
	{
		protected final CurryToken<O, V> token;
		
		CurriedDuo(O outer, Parameter<P> p1, V curryValue)
		{
			super(p1);
			this.token = new CurryToken<O, V>(outer, curryValue);
		}

		@Override public Object getOperationToken() { return token; }
		@Override public int getOperationComplexity() { return token.outer.getOperationComplexity(); }
		@SuppressWarnings("unchecked") @Override public Class<? extends T> resultType() { return (Class<? extends T>) token.outer.resultType(); }
	}
	
	private static class CurriedP0<P0, P1, T> extends CurriedDuo<P0, P1, StiXtractorDuo<P0, P1, T>, T>
	{
		CurriedP0(StiXtractorDuo<P0, P1, T> outer, P0 curryValue, StiXtractor.Parameter<P1> p1)
		{
			super(outer, p1, curryValue);
		}

		@Override protected T calculate(P1 p1)
		{
			return token.outer.calculate(token.value, p1);
		}
	}

	private static class CurriedP1<P0, P1, T> extends CurriedDuo<P1, P0, StiXtractorDuo<P0, P1, T>, T>
	{
		CurriedP1(StiXtractorDuo<P0, P1, T> outer, StiXtractor.Parameter<P0> p0, P1 curryValue)
		{
			super(outer, p0, curryValue);
		}

		@Override protected T calculate(P0 p0)
		{
			return token.outer.calculate(p0, token.value);
		}
	}
}
