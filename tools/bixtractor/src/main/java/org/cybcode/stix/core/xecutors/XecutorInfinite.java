package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

public class XecutorInfinite extends AbstractXecutor
{
	private static final XecutorInfinite INSTANCE	= new XecutorInfinite();

	public static StiXecutor getInstance()
	{
		return INSTANCE;
	}
	
	@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		verifyParameterIndex(context, pushedParameter);
		return this;
	}
}