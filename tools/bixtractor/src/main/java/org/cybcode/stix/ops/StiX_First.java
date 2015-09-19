package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.AbstractXtractorMono;

public class StiX_First<T> extends AbstractXtractorMono<T, T>
{
	public StiX_First(StiXtractor<? extends T> p0)
	{
		super(new PushParameter<T>(p0) 
		{
			@SuppressWarnings("unchecked") @Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue)
			{
				if (pushedValue == null) return null;
				context.setFinalState();
				return ((StiX_First<T>) context.getCurrentXtractor()).calculate((T) pushedValue);
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

	@Override public boolean isRepeatable()
	{
		return false;
	}

	@Override public T apply(StiXecutorContext context)
	{
		return calculate(p0.getValue(context));
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