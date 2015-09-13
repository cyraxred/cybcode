package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

public class XecutorTrio implements StiXecutor
{
	private final XecutorDuo[] nextXecutors;
	
	private class XecDuo extends XecutorDuo
	{
		public XecDuo(int leftIndex, int rightIndex)
		{
			super(leftIndex, rightIndex);
		}

		@Override protected boolean isPushToFinal(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
		{
			return XecutorTrio.this.isPushToFinal(context, pushedParameter, pushedValue);
		}
	}
	
	public XecutorTrio()
	{
		nextXecutors = new XecDuo[3]; 
		nextXecutors[0] = new XecDuo(1, 2);
		nextXecutors[1] = new XecDuo(0, 2);
		nextXecutors[2] = new XecDuo(0, 1);
	}

	@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		int pushedParamIndex = pushedParameter.getParamIndex();
		if (pushedParamIndex < 0 || pushedParamIndex >= nextXecutors.length) {
			throw new IllegalArgumentException("Parameter of unexpected index");
		}
		
		if (isPushToFinal(context, pushedParameter, pushedValue)) return XecutorFinal.getInstance();
		return nextXecutors[pushedParamIndex];
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
