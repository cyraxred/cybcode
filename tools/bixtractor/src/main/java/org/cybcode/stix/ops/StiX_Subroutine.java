package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorMonoPush;
import org.cybcode.stix.core.xecutors.XecutorMono;
import org.cybcode.stix.core.xecutors.XecutorMonoPush;

public final class StiX_Subroutine<S, T> implements StiXtractor<T>
{
	private static final StiXecutor XECUTOR = new XecutorMono(1);
	private static final StiXecutor XECUTOR_PUSH = new XecutorMonoPush(1);
	
	private final PushParameter<S> p0;
	private final Parameter<T> p1;
	
	public StiX_Subroutine(StiXtractor<? extends S> pSource, StiXtractor<? extends T> pResult)
	{
		p0 = new PushParameter<>(new EntryPoint(pSource));
		p1 = new Parameter<>(pResult);
	}
	
	@Override public boolean isRepeatable()
	{
		return p0.isRepeatable();
	}

	@Override public Object getOperationToken()
	{
		return this; //subroutines can't be deduplicated
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return 1;
	}
	
	@Override public Class<? extends T> resultType()
	{
		return p1.getSourceXtractor().resultType();
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return isRepeatable() ? XECUTOR_PUSH : XECUTOR;
	}

	@Override public T evaluate(StiXecutorContext context)
	{
		return p1.getValue(context);
	}

	@Override public void visit(StiXtractor.Visitor visitor)
	{
		visitor.visitParameter(p0);
		visitor.visitParameter(p1);
	}

	@Override public int paramCount()
	{
		return 2;
	}

	@SuppressWarnings("unchecked") @Override public StiXtractor<T> curry(int parameterIndex, Object value)
	{
		switch (parameterIndex) {
			case 0: 
				return null;
			case 1:
				return new StiX_ReplacePushValue<T>(p0.getSourceXtractor(), (T) value);
			default:
				throw new IllegalArgumentException();
		}
	}
	
	class EntryPoint extends StiXtractorMonoPush<S, S>
	{
		private EntryPoint(StiXtractor<? extends S> p0)
		{
			super(p0);
		}
		
		@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
		{
			return context.createFrameXecutor();
		}

		@Override public Class<? extends S> resultType()
		{
			return p0.getSourceXtractor().resultType();
		}
		
		@Override public Object getOperationToken()
		{
			return StiX_Subroutine.this.getOperationToken();
		}

		@Override public int getOperationComplexity(StiXComplexityHelper helper)
		{
			return 1;
		}

		@Override protected S calculate(S p0)
		{
			return p0;
		}

		public StiXtractor<T> getSubroutine()
		{
			return StiX_Subroutine.this;
		}
	}
}