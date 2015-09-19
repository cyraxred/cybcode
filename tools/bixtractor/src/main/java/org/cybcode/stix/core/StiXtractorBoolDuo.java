package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;

public abstract class StiXtractorBoolDuo extends StiXtractorDuo<Boolean, Boolean, Boolean>
{
	protected final Boolean	defaultValue;

	protected StiXtractorBoolDuo(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1)
	{
		super(p0, false, p1, false);
		this.defaultValue = null;
	}

	protected StiXtractorBoolDuo(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1, boolean defaultParamValue)
	{
		super(p0, true, p1, true);
		this.defaultValue = defaultParamValue;
	}

	@Override public Object getOperationToken()
	{
		return defaultValue;
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 10);
	}

	@Override public Class<Boolean> resultType()
	{
		return Boolean.class;
	}

	@Override protected Boolean evaluatePushP0(StiXecutorPushContext context, Boolean pushedValue)
	{
		if (pushedValue == null || defaultValue == null) return null;
		Boolean result = calculatePartial(pushedValue.booleanValue());
		if (result != null) {
			context.setFinalState();
		}
		return result;
	}
	
	@Override protected Boolean evaluatePushP1(StiXecutorPushContext context, Boolean pushedValue)
	{
		if (pushedValue == null || defaultValue == null) return null;
		Boolean result = calculatePartial(pushedValue.booleanValue());
		if (result != null) {
			context.setFinalState();
		}
		return result;
	}
	
	protected abstract Boolean calculatePartial(boolean p);
	protected abstract boolean calculate(boolean p0, boolean p1);
	
	@Override protected Boolean calculate(Boolean p0, Boolean p1)
	{
		if (p0 == null) {
			if (defaultValue == null) return null;
			p0 = defaultValue;
		} 
		if (p1 == null) {
			if (defaultValue == null) return null;
			p1 = defaultValue;
		}
		return calculate(p0.booleanValue(), p1.booleanValue());
	}
	
	protected abstract StiXtractor<Boolean> curry(boolean value, StiXtractor<Boolean> otherParam);
	
	@SuppressWarnings("unchecked") @Override public StiXtractor<Boolean> curry(int parameterIndex, Object value)
	{
		StiXtractor<Boolean> otherParam = null;
		switch (parameterIndex) {
			case 0: otherParam = (StiXtractor<Boolean>) p1.getSourceXtractor(); break;
			case 1: otherParam = (StiXtractor<Boolean>) p0.getSourceXtractor(); break;
			default:
				throw new IllegalArgumentException();
		}
		return curry((Boolean) value, otherParam);
	}
}