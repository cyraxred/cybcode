package org.cybcode.stix.api;

import java.util.List;

/**
 * 
 * @author Kirill "Red Cyrax" Ivkushkin <kirill@ivkushkin.name>
 *
 * @param <S> initial type to be parsed 
 * @param <C> internal context that carries configuration details (e.g. list of nested fields) after compilation
 * @param <D> description of a field
 * @param <T> internal field type
 */
public abstract class StiXource<S, C, D, T> implements StiXtractor<T>
{
	private static class SpecialXecutor<C> implements StiXecutor
	{
		private final Settings settings;
		private final C innerContext;
		
		SpecialXecutor(Settings settings, C innerContext)
		{
			this.settings = settings;
			this.innerContext = innerContext;
		}

		@SuppressWarnings("unchecked") @Override public Object evaluatePush(StiXecutorPushContext context, Parameter<?> pushedParameter, Object pushedValue)
		{
			Object parseValue = ((SpecialParameter<?>) pushedParameter).transform(pushedValue);
			Object valueForPush = ((StiXource<?, C, ?, Object>) context.getCurrentXtractor()).processNestedFields(context, innerContext, settings, parseValue);
			if (settings.hasPushTargets()) return valueForPush;
			return null;
		}

		@Override public Object evaluateFinal(StiXecutorPushContext context)
		{
			return context.getCurrentXtractor().apply(context);
		}
	}
	
	private static abstract class SpecialParameter<T> extends PushParameter<T>
	{
		private SpecialParameter(StiXtractor<? extends T> source, boolean callback) { super(source, callback); }
		protected abstract Object getOperationToken();
		protected abstract FieldParameter<?, T> asField();
		protected abstract T transform(Object value);
		
		@Override public Object evaluatePush(StiXecutorPushContext context, Object pushedValue)
		{
			throw new UnsupportedOperationException("Can't be called directly");
		}
	}

	private static final class FieldParameter<D, T> extends SpecialParameter<T>
	{
		private final D	fieldDetails;

		FieldParameter(StiXtractor<? extends T> source, D fieldDetails)
		{
			super(source, true);
			if (fieldDetails == null) throw new NullPointerException();
			this.fieldDetails = fieldDetails;
		}
		@Override protected Object getOperationToken() { return fieldDetails; }
		@Override protected FieldParameter<?, T> asField() { return this; };
		@SuppressWarnings("unchecked") @Override protected T transform(Object value) { return (T) value; };
	}

	private static final class TransformParameter<P, T> extends SpecialParameter<T>
	{
		private final StiXFunction<P, T> fn;

		@SuppressWarnings("unchecked") public TransformParameter(StiXtractor<? extends P> p0, StiXFunction<P, T> fn)
		{
			super((StiXtractor<? extends T>) (StiXtractor<?>) p0, false); //ugly hack
			if (fn == null) throw new NullPointerException();
			this.fn = fn;
		}
		
		public T getValue(StiXParamContext context)
		{
			return transform(context.getParamValue(getParamIndex()));
		}		

		@Override protected Object getOperationToken() { return fn.getOperationToken(); }
		@Override protected FieldParameter<?, T> asField() { return null; };
		@SuppressWarnings("unchecked") @Override protected T transform(Object value) { return fn.apply((P) value); };
	}
	
	protected static class Settings
	{
		private boolean hasPushTargets;
		private boolean hasSortedFields;

		public boolean hasPushTargets() { return hasPushTargets; }
		public boolean hasSortedFields() { return hasSortedFields; }
	}
	
	private final SpecialParameter<T> p0;
	
	public StiXource(StiXource<?, ?, D, T> p0, D fieldDetails)
	{
		this.p0 = new FieldParameter<>(p0, fieldDetails);
		//TODO
//		if (limitMode == ValueLimit.LAST) throw new UnsupportedOperationException("Xource doesn't support LAST limit for a field");
	}

	public StiXource(StiXtractor<? extends S> p0, StiXFunction<? super S, T> fn)
	{
		this.p0 = new TransformParameter<>(p0, fn);
//		if (limitMode == ValueLimit.LAST) throw new UnsupportedOperationException("Xource doesn't support LAST limit for a field");
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
		return TokenPair.of(null, ((SpecialParameter<?>) p0).getOperationToken());
	}

	@Override public final StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		Settings settings = new Settings();
		settings.hasPushTargets = context.hasPushTargets();
		settings.hasSortedFields = context.hasSortedFields();
		
		C innerContext = createFieldContainer(context.getXecutorCallbacks(), settings);
		return new SpecialXecutor<C>(settings, innerContext);
	}
	
	protected abstract C createFieldContainer(List<StiXecutorCallback> callbacks, Settings settings);
	
	protected abstract T processNestedFields(StiXecutorPushContext context, C container, Settings settings, T pushedValue);

	@Override public final T apply(StiXecutorContext context)
	{
		return null;
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
	
	@Override public int paramCount()
	{
		return 1;
	}
	
	@Override public void visit(Visitor visitor)
	{
		visitor.visitParameter(p0);
	}
}