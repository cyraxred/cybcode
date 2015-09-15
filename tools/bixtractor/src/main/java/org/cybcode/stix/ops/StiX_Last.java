package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorMonoIntercept;
import org.cybcode.stix.core.xecutors.XecutorFinal;

public class StiX_Last<T> extends StiXtractorMonoIntercept<T, Void, T>
{
	public StiX_Last(StiXtractor<? extends T> p0)
	{
		super(new PushParameter<>(p0));
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return createXecutor(null, false);
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

	@SuppressWarnings("unchecked") @Override public T evaluate(StiXecutorContext context)
	{
		return calculate((T) context.getInterimValue());
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 10);
	}

	@Override protected T calculate(T p0)
	{
		return p0;
	}

	@Override protected StiXecutor processPush(StiXecutorContext context, Void info, Parameter<?> pushedParameter, Object pushedValue)
	{
		if (pushedValue != null) {
			context.setInterimValue(pushedValue);
		}
		return XecutorFinal.getInstance();
	}

}