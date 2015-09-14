package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorMonoInterim;

public class StiX_Last<T> extends StiXtractorMonoInterim<T, T>
{
	public StiX_Last(StiXtractor<? extends T> p0)
	{
		super(p0);
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return createXecutorStoreEach();
	}

	@Override public Class<? extends T> resultType()
	{
		return p0.getSourceXtractor().resultType();
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override protected T calculate(T p0)
	{
		return p0;
	}

	@Override public boolean isRepeatable()
	{
		return false;
	}
}