package org.cybcode.stix.ops;

import org.cybcode.stix.api.OutputMode;
import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.AbstractXtractor;
import org.cybcode.stix.core.AbstractXtractorMono;
import org.cybcode.stix.core.xecutors.XecutorFail;

public final class StiX_Subroutine<S, T> extends AbstractXtractor<T>
{
	private final Parameter<S> p0; //EntryPoint parameter MUST come FIRST
	private final Parameter<T> p1;
	
	public StiX_Subroutine(StiXtractor<? extends S> pSource, StiXtractor<? extends T> pResult)
	{
		p0 = new Parameter<>(new EntryPoint(pSource)); 
		p0.disableNotify(); //this parameter is used through p1, but must be present for proper dependency management
		p1 = new Parameter<T>(pResult, true) 
		{
			@Override public Object evaluatePush(StiXecutorPushContext context, Object pushedValue)
			{
				context.setNextState(XecutorFail.getInstance());
				return pushedValue;
			}
		};
		if (p1.isRepeatable()) throw new IllegalArgumentException("Can only return a single value per invocation");
	}
	
	@Override public OutputMode getOutputMode()
	{
		return OutputMode.pushMode(p0.isRepeatable()); //well, inside we are regular (set-once), but the subr gots reset, resulting in multiple 
	}
	
	@Override public Object getOperationToken()
	{
		return null;
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
		return null;
//		throw new UnsupportedOperationException();
//		return isRepeatable() ? XECUTOR_PUSH : XECUTOR;
	}

	@Override public T apply(StiXecutorContext context)
	{
		return null;
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
	
	public class EntryPoint extends AbstractXtractorMono<S, S> implements Commutative
	{
		private EntryPoint(StiXtractor<? extends S> p0)
		{
			super(new PushParameter<S>(p0));
		}
		
		@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
		{
			return context.createFrameStartXecutor();
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
			return helper.getComplexityOf(this, 1);
		}

		@Override protected S calculate(S p0)
		{
			return p0;
		}

		public StiXtractor<T> getSubroutine()
		{
			return StiX_Subroutine.this;
		}

		@Override public OutputMode getOutputMode()
		{
			return OutputMode.REGULAR;
		}

		@Override public S apply(StiXecutorContext context)
		{
			throw new UnsupportedOperationException();
		}
	}
}