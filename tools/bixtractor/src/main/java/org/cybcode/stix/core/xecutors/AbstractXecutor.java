package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;

public abstract class AbstractXecutor implements StiXecutor
{
	protected int verifyParameterIndex(StiXecutorContext context, Parameter<?> pushedParameter)
	{
		int paramIndex = pushedParameter.getParamIndex();
		StiXtractor<?> xtractor = context.getCurrentXtractor();
		if (xtractor.paramCount() > paramIndex) return paramIndex;
		
		throw new IllegalArgumentException("Parameter index=" + paramIndex + ", xtractor=" + xtractor);
	}
	
	@Override public boolean isPushOrFinal()
	{
		return false;
	}
}