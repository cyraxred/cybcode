package org.cybcode.stix.api;

import java.util.List;

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
public abstract class StiXource<S, C, D, T> extends StiXtractorLimiter<T, TokenPair<C, StiXource.Settings>, T>
{
	private static abstract class SpecialParameter<T> extends PushParameter<T>
	{
		private SpecialParameter(StiXtractor<? extends T> source) { super(source); }
		protected abstract Object getOperationToken();
		protected abstract FieldParameter<?, T> asField();
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
		@Override protected FieldParameter<?, T> asField() { return this; };
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
		@Override protected FieldParameter<?, T> asField() { return null; };
	}
	
	protected static class Settings
	{
		private boolean hasPushTargets;
		private boolean hasSortedFields;

		public boolean hasPushTargets() { return hasPushTargets; }
		public boolean hasSortedFields() { return hasSortedFields; }
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
	
	@Override protected TokenPair<C, Settings> prepareContext(StiXecutorConstructionContext context)
	{
		Settings settings = new Settings();
		settings.hasPushTargets = context.hasPushTargets();
		settings.hasSortedFields = context.hasSortedFields();
		
		C fieldContainer = createFieldContainer(context.getXecutorCallbacks(), settings);
		return TokenPair.of(fieldContainer, settings);
	}
	
	protected abstract C createFieldContainer(List<StiXecutorCallback> callbacks, Settings settings);
	
	@SuppressWarnings("unchecked") @Override protected final StiXecutor processPush(StiXecutorContext context, TokenPair<C, Settings> container, 
		Parameter<?> pushedParameter, Object pushedValue)
	{
		Settings settings = container.getP1();
		T valueForPush = processNestedFields(context, container.getP0(), settings, (T) pushedValue);
		context.setInterimValue(settings.hasPushTargets() ? valueForPush : null);
		return getXecutor();
	}

	protected abstract T processNestedFields(StiXecutorContext context, C container, Settings settings, T pushedValue);

	@SuppressWarnings("unchecked") @Override public final T evaluate(StiXecutorContext context)
	{
		return (T) context.getInterimValue();
	}
	
	@Override public StiXtractor<? extends T> curry(int parameterIndex, Object value)
	{
		if (parameterIndex != 0) throw new IllegalArgumentException();
		return null;
	}
	
	public boolean isNested()
	{
		return ((SpecialParameter<?>) p0).asField() != null;
	}
	
	@SuppressWarnings("unchecked") public D getFieldDetails()
	{
		FieldParameter<?, ?> p = ((SpecialParameter<?>) p0).asField();
		if (p == null) return null;
		return (D) p.fieldDetails;
	}

	public static Object getFieldDetails(Parameter<?> param)
	{
		if (!(param instanceof FieldParameter)) return null;
		return ((FieldParameter<?, ?>) param).fieldDetails;
	}
}
