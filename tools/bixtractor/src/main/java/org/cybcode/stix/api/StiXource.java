package org.cybcode.stix.api;

import org.cybcode.stix.core.StiXtractorLimiter;

/**
 * 
 * @author Kirill "Red Cyrax" Ivkushkin <kirill@ivkushkin.name>
 *
 * @param <S> initial type to be parsed 
 * @param <C> internal context that carries configuration details (e.g. list of nested fields) after compilation
 * @param <D> description of a field
 * @param <T> internal field type
 */
public abstract class StiXource<S, C, D, T> extends StiXtractorLimiter<T, C, T>
{
	private static abstract class SpecialParameter<T> extends PushParameter<T>
	{
		private SpecialParameter(StiXtractor<? extends T> source) { super(source); }
		protected abstract Object getOperationToken();
	}

	private static final class FieldParameter<D, T> extends SpecialParameter<T>
	{
		private final D	fieldDetails;

		FieldParameter(StiXtractor<? extends T> source, D fieldDetails)
		{
			super(source);
			if (fieldDetails == null) throw new NullPointerException();
			this.fieldDetails = fieldDetails;
		}
		@Override protected Object getOperationToken() { return fieldDetails; }
		
		@Override public ParameterMode getMode()
		{
			return ParameterMode.CALLBACK;
		}
	}

	private static final class TransformParameter<P, T> extends SpecialParameter<T>
	{
		private final StiXFunction<P, T> fn;

		@SuppressWarnings("unchecked") public TransformParameter(StiXtractor<? extends P> p0, StiXFunction<P, T> fn)
		{
			super((StiXtractor<? extends T>) (StiXtractor<?>) p0); //ugly hack
			if (fn == null) throw new NullPointerException();
			this.fn = fn;
		}
		
		@SuppressWarnings("unchecked") public T getValue(StiXParamContext context)
		{
			P value = (P) context.getParamValue(getParamIndex());
			return fn.apply(value);
		}		

		@Override protected Object getOperationToken() { return fn.getOperationToken(); }
	}
	
	public StiXource(StiXource<?, ?, D, T> p0, D fieldDetails, StiXtractorLimiter.ValueLimit limitMode)
	{
		super(new FieldParameter<>(p0, fieldDetails), limitMode);
		if (limitMode == ValueLimit.LAST) throw new UnsupportedOperationException("Xource doesn't support LAST limit for a field");
	}

	public StiXource(StiXtractor<? extends S> p0, StiXFunction<? super S, T> fn, ValueLimit limitMode)
	{
		super(new TransformParameter<>(p0, fn), limitMode);
		if (limitMode == ValueLimit.LAST) throw new UnsupportedOperationException("Xource doesn't support LAST limit for a field");
	}
	
	@Override public final boolean isRepeatable()
	{
		return true; //source can ALWAYS produce repeated values
	}

	@Override public abstract Class<T> resultType();

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 10_000);
	}
	
	@Override public Object getOperationToken()
	{
		return TokenPair.of(super.getOperationToken(), ((SpecialParameter<?>) p0).getOperationToken());
	}

	@Override protected final T calculate(T p0)
	{
		return p0;
	}
	
	@SuppressWarnings("unchecked") @Override protected final StiXecutor processPush(StiXecutorContext context, C info, Parameter<?> pushedParameter, Object pushedValue)
	{
		if (pushedValue == null) return null;
		T valueForPush = processNestedFields(info, (T) pushedValue);
		context.setInterimValue(valueForPush);
		return getXecutor();
	}

	protected abstract T processNestedFields(C info, T pushedValue);

	@SuppressWarnings("unchecked") @Override public final T evaluate(StiXecutorContext context)
	{
		return (T) context.getInterimValue();
	}
	
	@Override public StiXtractor<? extends T> curry(int parameterIndex, Object value)
	{
		if (parameterIndex != 0) throw new IllegalArgumentException();
		return null;
	}

	public static Object getFieldDetails(Parameter<?> param)
	{
		if (!(param instanceof FieldParameter)) return null;
		return ((FieldParameter<?, ?>) param).fieldDetails;
	}
}
