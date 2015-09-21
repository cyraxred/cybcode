package org.cybcode.stix.core;

import org.cybcode.stix.api.OutputMode;
import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;

public abstract class StiXtractorDuo<P0, P1, T> implements StiXtractor<T>
{
	protected final Parameter<P0> p0;
	protected final Parameter<P1> p1;

	protected StiXtractorDuo(StiXtractor<? extends P0> p0, boolean p0_notify, StiXtractor<? extends P1> p1, boolean p1_notify)
	{
		this.p0 = new Parameter<P0>(p0, p0_notify) 
		{
			@SuppressWarnings("unchecked") @Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue) 
			{ 
				return evaluatePushP0(context, (P0) pushedValue); 
			}
		};
		this.p1 = new Parameter<P1>(p1, p1_notify) 
		{
			@SuppressWarnings("unchecked") @Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue) 
			{ 
				return evaluatePushP1(context, (P1) pushedValue); 
			}
		};
	}
	
	protected StiXtractorDuo(StiXtractor<? extends P0> p0, StiXtractor<? extends P1> p1)
	{
		this(p0, false, p1, false);
	}
	
	protected T evaluatePushP0(StiXecutorPushContext context, P0 pushedValue)
	{
		return null; //calculate(pushedValue, p1.getValue(context));
	}

	protected T evaluatePushP1(StiXecutorPushContext context, P1 pushedValue)
	{
		return null; //calculate(p0.getValue(context), pushedValue);
	}

	@Override public final OutputMode getOutputMode()
	{
		return OutputMode.REGULAR;
	}
	
	@Override public final T apply(StiXecutorContext context)
	{
		P0 pv0 = p0.getValue(context);
		P1 pv1 = p1.getValue(context);
		T result = calculate(pv0, pv1);
		return result;
	}

	protected abstract T calculate(P0 p0, P1 p1);

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return null;
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
		throw new UnsupportedOperationException();
//		switch (parameterIndex) {
//			case 0: {
//				@SuppressWarnings("unchecked") final P0 pv0 = (P0) value;
//				
//				if (isPushToFinal(parameterIndex, value)) {
//					T result = calculate(pv0, null); 
//					return StiX_Const.of(result);
//				}
//				return new CurriedP0<P0, P1, T>(this, pv0, p1);
//			}
//			case 1: {
//				@SuppressWarnings("unchecked") final P1 pv1 = (P1) value;
//				
//				if (isPushToFinal(parameterIndex, value)) {
//					T result = calculate(null, pv1); 
//					return StiX_Const.of(result);
//				}
//				return new CurriedP1<P0, P1, T>(this, p0, pv1);
//			}
//				
//			default:
//				throw new IllegalArgumentException();
//		}
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
		@Override public int getOperationComplexity(StiXComplexityHelper helper) { return token.outer.getOperationComplexity(helper); }
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
