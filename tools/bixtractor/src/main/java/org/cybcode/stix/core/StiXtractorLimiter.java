package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xecutors.XecutorFail;
import org.cybcode.stix.core.xecutors.XecutorFinal;

public abstract class StiXtractorLimiter<P0, V, T> extends StiXtractorMonoIntercept<P0, V, T>
{
	public enum ValueLimit { 
		/** This may not work as expected */ //TODO This may not work as expected 
		SINGLE { @Override boolean isFirstOnly() { return true; } }, 
		FIRST  { @Override boolean isFirstOnly() { return true; } },
		LAST, 
		ALL;
		
		boolean isFirstOnly() { return false; }
	}
	
	private final ValueLimit mode;

	public StiXtractorLimiter(StiXtractor<? extends P0> p0, ValueLimit mode)
	{
		this(new PushParameter<>(p0), mode);
	}

	protected StiXtractorLimiter(PushParameter<P0> p0, ValueLimit mode)
	{
		super(p0);
		if (mode == null) throw new NullPointerException();
		this.mode = mode;
	}

	@Override public Object getOperationToken()
	{
		return mode;
	}
	
	@Override public boolean isRepeatable()
	{
		return mode == ValueLimit.ALL && p0.isRepeatable();
	}
	
	protected abstract V prepareContext(StiXecutorConstructionContext context);
	
	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		V info = prepareContext(context);
		return createXecutor(info, mode == ValueLimit.ALL);
	}
	
	protected final StiXecutor getXecutor()
	{
		switch (mode) {
			case SINGLE: return XecutorFail.getInstance();
			case FIRST: return XecutorFinal.getInstance();
			case LAST: return null;
			case ALL: return null;
			default:
				throw new UnsupportedOperationException();
		} 
	}
}