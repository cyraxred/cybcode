package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

public class XecutorMono implements StiXecutor
{
	private static final XecutorMono	INSTANCE	= new XecutorMono(0);

	public static StiXecutor getInstance()
	{
		return INSTANCE;
	}
	
	protected final int paramIndex;
	
	public XecutorMono(int paramIndex) 
	{
		if (paramIndex < 0) throw new IllegalArgumentException("paramIndex=" + paramIndex);
		this.paramIndex = paramIndex;
	}

	@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		if (pushedParameter.getParamIndex() != this.paramIndex) {
			throw new IllegalArgumentException("Parameter of unexpected index");
		}
		return XecutorFinal.getInstance();
	}

	@Override public boolean isPushOrFinal()
	{
		return false;
	}
}
