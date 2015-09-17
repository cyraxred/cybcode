package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorAggregate;
import org.cybcode.tools.mutable.MutableLong;

public class StiX_Count extends StiXtractorAggregate<Object, MutableLong, Long>
{
	public StiX_Count(StiXtractor<? extends Object> p0)
	{
		super(p0);
	}

	@Override public Class<Long> resultType()
	{
		return Long.class;
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 100);
	}

	@Override protected boolean isFinalStateValue(MutableLong accumulator)
	{
		return false;
	}

	@Override protected MutableLong aggregateFirstValue(Object p0)
	{
		return new MutableLong(1);
	}

	@Override protected MutableLong aggregateNextValue(MutableLong accumulator, Object p0)
	{
		return accumulator.add(1L);
	}

	@Override protected Long calculate(MutableLong accumulator)
	{
		return accumulator.longValue();
	}
}
