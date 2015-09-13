package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

public class XecutorDuo implements StiXecutor
{
	private final XecutorMono leftXecutor;
	private final XecutorMono rightXecutor;

	public XecutorDuo(int leftIndex, int rightIndex) 
	{
		leftXecutor	= new XecutorMono(leftIndex);
		rightXecutor = new XecutorMono(rightIndex);
	}

	@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		int pushedParamIndex = pushedParameter.getParamIndex();
		
		StiXecutor result;
		if (leftXecutor.paramIndex == pushedParamIndex) {
			result = rightXecutor;
		} else if (rightXecutor.paramIndex == pushedParamIndex) {
			result = leftXecutor;
		} else {
			throw new IllegalArgumentException("Parameter of unexpected index");
		}
		if (isPushToFinal(context, pushedParameter, pushedValue)) return XecutorFinal.getInstance();
		return result;
	}

	protected boolean isPushToFinal(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		return false;
	}

	@Override public boolean isPushOrFinal()
	{
		return false;
	}
}
