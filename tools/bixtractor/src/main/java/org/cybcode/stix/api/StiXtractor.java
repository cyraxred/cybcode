package org.cybcode.stix.api;

import org.cybcode.stix.ops.StiX_Const;

import com.google.common.base.Function;

public interface StiXtractor<T> extends Function<StiXecutorContext, T>
{
	public interface Commutative {}
	
	StiXecutor createXecutor(StiXecutorConstructionContext context);
	@Override T apply(StiXecutorContext context);

	void visit(Visitor visitor);
	int paramCount();
	
	boolean isRepeatable();
	Class<? extends T> resultType();
	
	Object getOperationToken();
	int getOperationComplexity(StiXComplexityHelper helper);
	
	/**
	 * 
	 * @param parameterIndex
	 * @param value
	 * @return null when not supported, but only when parameterIndex is valid 
	 */
	StiXtractor<? extends T> curry(int parameterIndex, Object value);
	
	interface Visitor
	{
		void visitParameter(Parameter<?> param);	
	}

	class Parameter<P>
	{
		private int paramIndex = -1;
		private ParameterBehavior behavior;
		private final StiXtractor<? extends P> source;
		
		public Parameter(StiXtractor<? extends P> source)
		{
			this(source, ParameterBehavior.REGULAR);
		}
		
		public Parameter(StiXtractor<? extends P> source, boolean notify)
		{
			this(source, notify ? ParameterBehavior.NOTIFY_ON_FINAL : ParameterBehavior.REGULAR);
		}
		
		Parameter(StiXtractor<? extends P> source, ParameterBehavior behavior)
		{
			if (behavior == null || source == null) throw new NullPointerException();
			this.source = source;
			this.behavior = behavior;
		}
		
		public final StiXtractor<? extends P> getSourceXtractor()
		{
			return source;
		}
		
		public void disableNotify()
		{
			if (behavior.isMandatory()) throw new IllegalStateException("Push or callback parameters can't be disabled");
			behavior = ParameterBehavior.NEVER_NOTIFY;
		}

		@SuppressWarnings("unchecked") public P getValue(StiXParamContext context)
		{
			return (P) context.getParamValue(paramIndex);
		}
		
		public boolean hasFinalValue(StiXecutorContext context)
		{
			return context.hasParamFinalValue(paramIndex);
		}
		
		public void setParamIndex(int paramIndex)
		{
			if (paramIndex < 0) throw new IllegalArgumentException();
			if (this.paramIndex == paramIndex) return;
			if (this.paramIndex >= 0) throw new IllegalStateException();
			this.paramIndex = paramIndex;
		}
		
		public int getParamIndex()
		{
			if (this.paramIndex < 0) throw new IllegalStateException();
			return this.paramIndex;
		}
		
		public <T> StiXtractor<? extends T> tryCurryIfConst(StiXtractor<T> xtractor)
		{
			if (!(source instanceof StiX_Const)) return xtractor;
			Object value = ((StiX_Const<?>) source).getValue();
			
			StiXtractor<? extends T> result = xtractor.curry(paramIndex, value);
			if (result == null) return xtractor;
			
			return result;
		}

		public final ParameterBehavior getBehavior()
		{
			return behavior;
		}

		public boolean isRepeatable()
		{
			return false;
		}
		
		@Override public String toString()
		{
			return toNameString() + "=" + source;
		}
		
		public Object evaluatePush(StiXecutorPushContext context, Object pushedValue)
		{
			return null;
		}

		public String toNameString()
		{
			return "p" + (paramIndex < 0 ? "?" : "" + paramIndex);
		}
	}
	
	class PushParameter<P> extends Parameter<P>
	{
		private final boolean	isRepeatable;

		PushParameter(StiXtractor<? extends P> source, boolean callback)
		{
			super(source, callback ? ParameterBehavior.CALLBACK : ParameterBehavior.PUSH_ALL);
			this.isRepeatable = source.isRepeatable();
		}

		public PushParameter(StiXtractor<? extends P> source)
		{
			this(source, false);
		}

		@Override public <T> StiXtractor<? extends T> tryCurryIfConst(StiXtractor<T> xtractor)
		{
			return isRepeatable ? xtractor : super.tryCurryIfConst(xtractor);
		}
		
		@Override public boolean isRepeatable()
		{
			return isRepeatable;
		}
	}
}