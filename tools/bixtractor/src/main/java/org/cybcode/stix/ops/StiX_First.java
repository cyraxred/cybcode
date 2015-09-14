package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorMonoPush;
import org.cybcode.stix.core.xecutors.XecutorMono;

public class StiX_First<T> extends StiXtractorMonoPush<T, T>
{
	public StiX_First(StiXtractor<? extends T> p0)
	{
		super(p0); //subscribes to receive each value as soon as available ...
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return XecutorMono.getInstance(); //... but stops right after the first one
	}
	
	@Override public Class<? extends T> resultType()
	{
		return p0.getSourceXtractor().resultType();
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 1);
	}
	
	@Override public boolean isRepeatable()
	{
		return false;
	}

	@Override protected T calculate(T p0)
	{
		return p0;
	}
}