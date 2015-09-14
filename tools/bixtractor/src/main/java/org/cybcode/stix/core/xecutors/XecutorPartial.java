package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;

public class XecutorPartial extends AbstractXecutor
{
	private static final XecutorPartial[] STATES = new XecutorPartial[1 << 8];
	private static final int MAX_PARAM_COUNT = Integer.SIZE; 
	private static final int MAX_PARAM_MASK = 0xFFFF_FFFF; 

	private final int state;

	private static void verifyParamIndex(int paramIndex)
	{
		if (paramIndex < 0) throw new IllegalArgumentException();
		if (paramIndex >= MAX_PARAM_COUNT) {
			throw new UnsupportedOperationException("Parameter index=" + paramIndex + " is not supported, max=" + (MAX_PARAM_COUNT - 1));
		}
	}

	private static XecutorPartial valueOf(int state)
	{
		if (state >= 0 && STATES.length > state) return STATES[state];
		return new XecutorPartial(state);
	}
	
	public static XecutorPartial newInstance(StiXtractor<?> usedBy)
	{
		int paramCount = usedBy.paramCount();
		if (paramCount <= 0) throw new IllegalArgumentException();
		if (paramCount > MAX_PARAM_COUNT) {
			throw new UnsupportedOperationException("Parameter count=" + paramCount + " is not supported, max=" + MAX_PARAM_COUNT);
		}
		
		return STATES[0];
	}
	
	private XecutorPartial(int state) 
	{
		this.state = state; 
	}
	
	public boolean hasParameterResolved(int paramIndex)
	{
		if (paramIndex < 0) throw new IllegalArgumentException();
		if (paramIndex >= MAX_PARAM_COUNT) return false;
		return (state & (1 << paramIndex)) != 0;
	}

	private static int getAllParametersMask(int paramCount)
	{
		return paramCount == MAX_PARAM_COUNT ? MAX_PARAM_MASK : (1 << paramCount) - 1;
	}
	
	public boolean hasAllParametersResolved(int paramCount)
	{
		if (paramCount < 0) throw new IllegalArgumentException();
		if (paramCount > MAX_PARAM_COUNT) return false;
		int mask = getAllParametersMask(paramCount); 
		return mask == (state & mask);
	}

	public XecutorPartial nextParameterResolved(int resolvedParamater)
	{
		verifyParamIndex(resolvedParamater);
		int resolvedState = state | (1 << resolvedParamater);
		if (state == resolvedState) return this;
		return valueOf(resolvedState);
	}
	
	@Override public StiXecutor push(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue)
	{
		int paramIndex = pushedParameter.getParamIndex();
		StiXtractor<?> xtractor = context.getCurrentXtractor();
		int paramCount = xtractor.paramCount();
		if (paramIndex >= paramCount) {
			throw new IllegalArgumentException("Parameter index=" + paramIndex + ", xtractor=" + xtractor);
		}
		
		StiXecutorPushCallback callback = (StiXecutorPushCallback) xtractor;

		int nextState = this.state | (1 << paramIndex);
		if (this.state == nextState) return this;
		
		if (callback.isPushToFinal(context, pushedParameter, pushedValue)) return XecutorFinal.getInstance();
		if (pushedParameter.isRepeatable()) return this;

		int allParamMask = getAllParametersMask(paramCount);
		if (allParamMask == (nextState & allParamMask)) return XecutorFinal.getInstance();
		
		return valueOf(nextState);
	}

	@Override public boolean isPushOrFinal()
	{
		return false;
	}
}