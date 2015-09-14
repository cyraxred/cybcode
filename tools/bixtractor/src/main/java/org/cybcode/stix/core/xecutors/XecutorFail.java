package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

public class XecutorFail extends AbstractXecutor
{
	private static final XecutorFail INSTANCE = new XecutorFail();

	public static StiXecutor getInstance()
	{
		return INSTANCE;
	}

	@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		throw new IllegalStateException();
	}
}