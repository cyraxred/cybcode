package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xecutors.AbstractXecutor;
import org.cybcode.stix.core.xecutors.XecutorFail;
import org.cybcode.stix.core.xecutors.XecutorFinal;

public abstract class StiXtractorMonoInterim<P0, T> extends AbstractXtractorMono<P0, T>
{
	private static class XecutorStoreEach extends AbstractXecutor
	{
		private static final XecutorStoreEach INSTANCE = new XecutorStoreEach();
		
		@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
		{
			verifyParameterIndex(context, pushedParameter);
			context.setInterimValue(pushedValue);
			return this;
		}
	}

	private static class XecutorPushEach extends XecutorStoreEach
	{
		private static final XecutorPushEach INSTANCE = new XecutorPushEach();
		
		@Override public boolean isPushOrFinal()
		{
			return true;
		}
	}
	
	private static class XecutorStoreFirst extends XecutorStoreEach
	{
		private static final XecutorStoreFirst INSTANCE = new XecutorStoreFirst();

		@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
		{
			super.push(context, pushedParameter, pushedValue);
			return XecutorFinal.getInstance();
		}
	}
	
	private static class XecutorStoreFirstOnly extends XecutorStoreEach
	{
		private static final XecutorStoreFirstOnly INSTANCE = new XecutorStoreFirstOnly();

		@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
		{
			super.push(context, pushedParameter, pushedValue);
			return XecutorFail.getInstance();
		}
	}
	
	public StiXtractorMonoInterim(StiXtractor<? extends P0> p0)
	{
		super(new PushParameter<>(p0));
	}

	protected abstract T calculate(P0 p0);

	protected StiXtractorMonoInterim(PushParameter<P0> p0)
	{
		super(p0);
	}

	protected static StiXecutor createXecutorStoreEach() { return XecutorStoreEach.INSTANCE; } 
	protected static StiXecutor createXecutorPushEach() { return XecutorPushEach.INSTANCE; } 
	protected static StiXecutor createXecutorStoreFirst() { return XecutorStoreFirst.INSTANCE; } 
	protected static StiXecutor createXecutorStoreFirstOnly() { return XecutorStoreFirstOnly.INSTANCE; } 
	
	@Override public T evaluate(StiXecutorContext context)
	{
		@SuppressWarnings("unchecked") P0 pv0 = (P0) context.getInterimValue();
		return calculate(pv0);
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 10);
	}
}