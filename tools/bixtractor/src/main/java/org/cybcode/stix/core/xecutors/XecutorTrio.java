package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

public abstract class XecutorTrio extends AbstractXecutor
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
		
		@Override public boolean isPushOrFinal()
		{
			return XecutorTrio.this.isPushOrFinal();
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
		int pushedParamIndex = verifyParameterIndex(context, pushedParameter); 
		if (pushedParamIndex >= nextXecutors.length) return this;
		
		if (isPushToFinal(context, pushedParameter, pushedValue)) return XecutorFinal.getInstance();
		return nextXecutors[pushedParamIndex];
	}

	protected abstract boolean isPushToFinal(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue);
}
