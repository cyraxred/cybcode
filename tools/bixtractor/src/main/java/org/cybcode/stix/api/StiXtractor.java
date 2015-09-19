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

	enum ParameterBehavior
	{
		REGULAR, 
		NOTIFY_ON_FINAL, NEVER_NOTIFY,
		PUSH_ALL { public boolean isMandatory() { return true; }}, 
		CALLBACK { public boolean isMandatory() { return true; }};
		
		public boolean isMandatory() { return false; }
	}
	
	class Parameter<P>
	{
		private int paramIndex = -1;
		private final StiXtractor<? extends P> source;
		
		public Parameter(StiXtractor<? extends P> source)
		{
			this.source = source;
		}
		
		public final StiXtractor<? extends P> getSourceXtractor()
		{
			return source;
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

		public boolean isRepeatable()
		{
			return false;
		}

		public final ParameterBehavior getBehavior()
		{
			return getParamBehavior();
		}
		
		ParameterBehavior getParamBehavior()
		{
			return ParameterBehavior.REGULAR;
		}
		
		@Override public String toString()
		{
			return toString(toNameString());
		}
		
		public Object evaluatePush(StiXecutorPushContext context, Object pushedValue)
		{
			return null;
		}

		String toNameString()
		{
			return "p" + (paramIndex < 0 ? "?" : "" + paramIndex);
		}

		public String toString(String paramName)
		{
			return paramName + (/*this instanceof PushParameter ? "<<=" :*/"=") + source;
		}
	}
	
	class PushParameter<P> extends Parameter<P>
	{
		private final boolean	isRepeatable;

		public PushParameter(StiXtractor<? extends P> source)
		{
			super(source);
			this.isRepeatable = source.isRepeatable();
		}

		@Override public <T> StiXtractor<? extends T> tryCurryIfConst(StiXtractor<T> xtractor)
		{
			return isRepeatable ? xtractor : super.tryCurryIfConst(xtractor);
		}
		
		@Override public boolean isRepeatable()
		{
			return isRepeatable;
		}

		@Override ParameterBehavior getParamBehavior()
		{
			return ParameterBehavior.PUSH_ALL;
		}
	}

	class NotifyParameter<P> extends Parameter<P>
	{
		public NotifyParameter(StiXtractor<? extends P> source)
		{
			super(source);
		}

		@Override ParameterBehavior getParamBehavior()
		{
			return ParameterBehavior.NOTIFY_ON_FINAL;
		}
	}

	class NeverNotifyParameter<P> extends Parameter<P>
	{
		public NeverNotifyParameter(StiXtractor<? extends P> source)
		{
			super(source);
		}

		@Override ParameterBehavior getParamBehavior()
		{
			return ParameterBehavior.NEVER_NOTIFY;
		}
	}
}