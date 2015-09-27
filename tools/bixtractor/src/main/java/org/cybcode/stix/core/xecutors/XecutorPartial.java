package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
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

	private static int getAllParametersMask(int paramCount)
	{
		return paramCount == MAX_PARAM_COUNT ? MAX_PARAM_MASK : (1 << paramCount) - 1;
	}
	
	public static XecutorPartial newInstance(StiXtractor<?> usedBy)
	{
		int paramCount = usedBy.paramCount();
		if (paramCount <= 0) throw new IllegalArgumentException();
		if (paramCount > MAX_PARAM_COUNT) {
			throw new UnsupportedOperationException("Parameter count=" + paramCount + " is not supported, max=" + MAX_PARAM_COUNT);
		}
		
		return valueOf(getAllParametersMask(paramCount));
	}
	
	private XecutorPartial(int state) 
	{
		this.state = state; 
	}
	
	public boolean isParameterResolved(int paramIndex)
	{
		verifyParamIndex(paramIndex);
		return (state & (1 << paramIndex)) == 0;
	}

	public boolean hasAllParametersResolved()
	{
		return state == 0;
	}

	public XecutorPartial resolveParameter(int resolvedParamater)
	{
		verifyParamIndex(resolvedParamater);
		int nextState = state & ~(1 << resolvedParamater);
		if (state == nextState) return this;
		return valueOf(nextState);
	}
	
	@Override public Object evaluatePush(StiXecutorPushContext pContext, Parameter<?> pushedParameter, Object pushedValue)
	{
		StiXecutorContext context = pContext.getXecutorContext();
		if (pushedParameter.hasFinalValue(context)) {
			XecutorPartial nextState = this.resolveParameter(pushedParameter.getParamIndex());
			if (nextState.hasAllParametersResolved()) {
				pContext.setFinalState();
				return pContext.getCurrentXtractor().apply(context);
			}
			pContext.setNextState(nextState);
		}
		return pushedParameter.evaluatePush(pContext, pushedValue);
	}

	@Override public Object evaluateFinal(StiXecutorPushContext context)
	{
		return context.getCurrentXtractor().apply(context.getXecutorContext());
	}
}