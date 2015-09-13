package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

public class XecutorMonoPush extends XecutorMono
{
	private static final XecutorMonoPush INSTANCE	= new XecutorMonoPush(0);

	public static StiXecutor getInstance()
	{
		return INSTANCE;
	}
	
	public XecutorMonoPush(int paramIndex) 
	{
		super(paramIndex);
	}

	@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		super.push(context, pushedParameter, pushedValue);
		return this;
	}

	@Override public boolean isPushOrFinal()
	{
		return true;
	}
}
