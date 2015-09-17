package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xecutors.XecutorFinal;

public class StiX_First<T> extends StiX_Last<T>
{
	public StiX_First(StiXtractor<? extends T> p0)
	{
		super(p0); //subscribes to receive each value as soon as available ...
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 10);
	}

	@Override protected StiXecutor processPush(StiXecutorContext context, Void info, Parameter<?> pushedParameter, Object pushedValue)
	{
		if (pushedValue == null) return null;

		context.setInterimValue(pushedValue);
		return XecutorFinal.getInstance();
	}
}