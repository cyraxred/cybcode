package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;

public abstract class StiXtractorMonoPush<P0, T> extends AbstractXtractorMono<P0, T>
{
	public StiXtractorMonoPush(StiXtractor<? extends P0> p0)
	{
		super(new PushParameter<P0>(p0) 
		{
			@SuppressWarnings("unchecked") @Override public Object evaluatePush(StiXecutorPushContext context, Object pushedValue)
			{
				if (!isRepeatable()) {
					context.setFinalState();
				}
				return ((StiXtractorMonoPush<P0, T>) context.getCurrentXtractor()).calculate((P0) pushedValue);
			}
		});
	}

	@Override public final T apply(StiXecutorContext context)
	{
		P0 pv0 = p0.getValue(context);
		T result = calculate(pv0);
		return result;
	}
	
	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return null;
	}
}
