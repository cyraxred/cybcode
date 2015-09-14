package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXpressionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xecutors.XecutorTrio;
import org.cybcode.stix.ops.StiX_Const;

public abstract class StiXtractorTrio<P0, P1, P2, T> implements StiXtractor<T>
{
	private final static XecutorTrio XECUTOR = new XecutorTrio() 
	{
		@Override protected boolean isPushToFinal(StiXecutorContext context, StiXtractor.Parameter<?> pushedParameter, Object pushedValue) 
		{
			return ((StiXtractorTrio<?, ?, ?, ?>) context.getCurrentXtractor()).isPushToFinal(context, pushedParameter, pushedValue);
		}

		@Override public boolean isPushOrFinal()
		{
			return false;
		};
	};
	
	private final Parameter<P0> p0;
	private final Parameter<P1> p1;
	private final Parameter<P2> p2;

	public StiXtractorTrio(StiXtractor<? extends P0> p0, StiXtractor<? extends P1> p1, StiXtractor<? extends P2> p2)
	{
		this.p0 = new Parameter<P0>(p0);
		this.p1 = new Parameter<P1>(p1);
		this.p2 = new Parameter<P2>(p2);
	}
	
	@Override public T evaluate(StiXecutorContext context)
	{
		P0 pv0 = p0.getValue(context);
		P1 pv1 = p1.getValue(context);
		P2 pv2 = p2.getValue(context);
		T result = calculate(pv0, pv1, pv2);
		return result;
	}
	
	@Override public final boolean isRepeatable()
	{
		return false;
	}
	
	protected abstract T calculate(P0 p0, P1 p1, P2 p2);

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
			case 2:
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
		return 3;
	}
	
	@Override public void visit(StiXtractor.Visitor visitor)
	{
		visitor.visitParameter(p0);
		visitor.visitParameter(p1);
		visitor.visitParameter(p2);
	}

	@Override public StiXtractor<? extends T> curry(int parameterIndex, Object value)
	{
		switch (parameterIndex) {
			case 0: {
				@SuppressWarnings("unchecked") final P0 pv0 = (P0) value;
				
				if (isPushToFinal(parameterIndex, value)) {
					T result = calculate(pv0, null, null); 
					return StiX_Const.of(result);
				}
				return new CurriedP0<P0, P1, P2, T>(this, pv0, p1, p2);
			}
			case 1: {
				@SuppressWarnings("unchecked") final P1 pv1 = (P1) value;
				
				if (isPushToFinal(parameterIndex, value)) {
					T result = calculate(null, pv1, null); 
					return StiX_Const.of(result);
				}
				return new CurriedP1<P0, P1, P2, T>(this, p0, pv1, p2);
			}
			case 2: {
				@SuppressWarnings("unchecked") final P2 pv2 = (P2) value;
				
				if (isPushToFinal(parameterIndex, value)) {
					T result = calculate(null, null, pv2); 
					return StiX_Const.of(result);
				}
				return new CurriedP2<P0, P1, P2, T>(this, p0, p1, pv2);
			}
				
			default:
				throw new IllegalArgumentException();
		}
	}

	private static abstract class CurriedTrio<V, P0, P1, O extends StiXtractor<?>, T> extends StiXtractorDuo<P0, P1, T>
	{
		protected final CurryToken<O, V> token;
		
		CurriedTrio(O outer, Parameter<P0> p0, Parameter<P1> p1, V curryValue)
		{
			super(p0, p1);
			this.token = new CurryToken<O, V>(outer, curryValue);
		}

		@Override public Object getOperationToken() { return token; }
		@Override public int getOperationComplexity(StiXComplexityHelper helper) { return token.outer.getOperationComplexity(helper); }
		@SuppressWarnings("unchecked") @Override public Class<T> resultType() { return (Class<T>) token.outer.resultType(); }
	}
	
	
	private static class CurriedP0<P0, P1, P2, T> extends CurriedTrio<P0, P1, P2, StiXtractorTrio<P0, P1, P2, T>, T>
	{
		CurriedP0(StiXtractorTrio<P0, P1, P2, T> outer, P0 curryValue, StiXtractor.Parameter<P1> p1, StiXtractor.Parameter<P2> p2)
		{
			super(outer, p1, p2, curryValue);
		}

		@Override protected T calculate(P1 p1, P2 p2)
		{
			return token.outer.calculate(token.value, p1, p2);
		}
		
		@Override protected boolean isPushToFinal(int parameterIndex, Object value)
		{
			return token.outer.isPushToFinal(parameterIndex + 1, value);
		}
	}

	private static class CurriedP1<P0, P1, P2, T> extends CurriedTrio<P1, P0, P2, StiXtractorTrio<P0, P1, P2, T>, T>
	{
		CurriedP1(StiXtractorTrio<P0, P1, P2, T> outer, StiXtractor.Parameter<P0> p0, P1 curryValue, StiXtractor.Parameter<P2> p2)
		{
			super(outer, p0, p2, curryValue);
		}

		@Override protected T calculate(P0 p0, P2 p2)
		{
			return token.outer.calculate(p0, token.value, p2);
		}
		
		@Override protected boolean isPushToFinal(int parameterIndex, Object value)
		{
			if (parameterIndex == 0) return token.outer.isPushToFinal(0, value);
			return token.outer.isPushToFinal(parameterIndex + 1, value);
		}
	}

	private static class CurriedP2<P0, P1, P2, T> extends CurriedTrio<P2, P0, P1, StiXtractorTrio<P0, P1, P2, T>, T>
	{
		CurriedP2(StiXtractorTrio<P0, P1, P2, T> outer, StiXtractor.Parameter<P0> p0, StiXtractor.Parameter<P1> p1, P2 curryValue)
		{
			super(outer, p0, p1, curryValue);
		}

		@Override protected T calculate(P0 p0, P1 p1)
		{
			return token.outer.calculate(p0, p1, token.value);
		}
		
		@Override protected boolean isPushToFinal(int parameterIndex, Object value)
		{
			return token.outer.isPushToFinal(parameterIndex, value);
		}
	}
}
