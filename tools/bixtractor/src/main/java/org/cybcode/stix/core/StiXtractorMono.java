package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xecutors.XecutorMono;
import org.cybcode.stix.core.xecutors.XecutorMonoPush;

public abstract class StiXtractorMono<P0, T> extends AbstractXtractorMono<P0, T>
{
	public StiXtractorMono(StiXtractor<? extends P0> p0)
	{
		super(new NotifyParameter<P0>(p0));
	}

	StiXtractorMono(Parameter<P0> p0)
	{
		super(p0);
	}
	
	@Override public final T evaluate(StiXecutorContext context)
	{
		P0 pv0 = p0.getValue(context);
		T result = calculate(pv0);
		return result;
	}
	
	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return isRepeatable() ? XecutorMonoPush.getInstance() : XecutorMono.getInstance();
	}
}
