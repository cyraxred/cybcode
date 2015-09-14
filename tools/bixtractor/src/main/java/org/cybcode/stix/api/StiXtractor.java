package org.cybcode.stix.api;

import org.cybcode.stix.ops.StiX_Const;

public interface StiXtractor<T>
{
	public interface Commutative {}
	
	StiXecutor createXecutor(StiXpressionContext context);
	
	T evaluate(StiXecutorContext context);
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

	enum ParameterPushMode
	{
		REGULAR, PUSH_ALL, NOTIFY_ON_FINAL, NEVER
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
		
		public boolean hasValue(StiXParamContext context)
		{
			return context.hasParamValue(paramIndex);
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
		
		public ParameterPushMode getPushMode()
		{
			return ParameterPushMode.REGULAR;
		}
		
		@Override public String toString()
		{
			return toString(toNameString());
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

		@Override public ParameterPushMode getPushMode()
		{
			return ParameterPushMode.PUSH_ALL;
		}
	}

	class NotifyParameter<P> extends Parameter<P>
	{
		public NotifyParameter(StiXtractor<? extends P> source)
		{
			super(source);
		}

		@Override public ParameterPushMode getPushMode()
		{
			return ParameterPushMode.NOTIFY_ON_FINAL;
		}
	}
}