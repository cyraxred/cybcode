package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

public abstract class XecutorDuo extends AbstractXecutor
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
		int pushedParamIndex = verifyParameterIndex(context, pushedParameter);
		
		StiXecutor result;
		if (leftXecutor.paramIndex == pushedParamIndex) {
			result = rightXecutor;
		} else if (rightXecutor.paramIndex == pushedParamIndex) {
			result = leftXecutor;
		} else {
			return this;
		}
		if (isPushToFinal(context, pushedParameter, pushedValue)) return XecutorFinal.getInstance();
		return result;
	}

	protected abstract boolean isPushToFinal(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue);
}
