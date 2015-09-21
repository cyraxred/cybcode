package org.cybcode.stix.ops;

import org.cybcode.stix.api.OutputMode;
import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.AbstractXtractorMono;

public class StiX_Last<T> extends AbstractXtractorMono<T, T>
{
	public StiX_Last(StiXtractor<? extends T> p0)
	{
		super(new PushParameter<T>(p0) 
		{
			@Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue)
			{
				if (pushedValue == null) return null;
				context.setInterimValue(pushedValue);
				return null;
			}
		});
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return null;
	}

	@Override public Class<? extends T> resultType()
	{
		return p0.getSourceXtractor().resultType();
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public final OutputMode getOutputMode()
	{
		return OutputMode.REGULAR;
	}

	@SuppressWarnings("unchecked") @Override public T apply(StiXecutorContext context)
	{
		return calculate((T) context.getInterimValue());
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 10000);
	}

	@Override protected T calculate(T p0)
	{
		return p0;
	}
}