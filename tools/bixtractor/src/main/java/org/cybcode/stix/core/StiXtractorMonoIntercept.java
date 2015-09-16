package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.core.xecutors.AbstractXecutor;

public abstract class StiXtractorMonoIntercept<P0, V, T> extends AbstractXtractorMono<P0, T>
{
	private static class Xecutor<T> implements StiXecutor
	{
		private final T info;

		Xecutor(T info) { this.info = info; }

		@SuppressWarnings("unchecked") @Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
		{
			AbstractXecutor.verifyParameterIndex(context, pushedParameter);
			if (pushedValue == null) return this;
			StiXecutor result = ((StiXtractorMonoIntercept<?, T, ?>) context.getCurrentXtractor()).processPush(context, info, pushedParameter, pushedValue);
			if (result != null) return result;
			return this;
		}

		@Override public boolean isPushOrFinal() { return false; }
	}

	private static class PushXecutor<T> extends Xecutor<T>
	{
		PushXecutor(T info) { super(info); }
		@Override public boolean isPushOrFinal() { return false; }
	}
	
	protected StiXtractorMonoIntercept(PushParameter<P0> p0)
	{
		super(p0);
	}

	protected abstract StiXecutor processPush(StiXecutorContext context, V info, Parameter<?> pushedParameter, Object pushedValue);

	private static final Xecutor<?> NO_PUSH = new Xecutor<Object>(null);
	private static final PushXecutor<?> PUSH = new PushXecutor<Object>(null);
	
	protected static <V> StiXecutor createXecutor(V info, boolean isPush) 
	{
		if (info == null) return isPush ? PUSH : NO_PUSH;
		return isPush ? new PushXecutor<V>(info) : new Xecutor<V>(info);
	} 
}