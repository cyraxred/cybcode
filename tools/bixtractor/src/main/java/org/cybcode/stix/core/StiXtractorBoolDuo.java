package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXtractor;

public abstract class StiXtractorBoolDuo extends StiXtractorDuo<Boolean, Boolean, Boolean>
{
	protected final Boolean	defaultValue;

	public StiXtractorBoolDuo(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1, Boolean defaultParamValue)
	{
		super(p0, p1);
		this.defaultValue = defaultParamValue;
	}

	@Override public Object getOperationToken()
	{
		return defaultValue;
	}

	@Override public int getOperationComplexity()
	{
		return 2;
	}
	
	@Override protected boolean isPushToFinal(int parameterIndex, Object value)
	{
		if (defaultValue == null) return false;
		return isPushToFinal(parameterIndex, ((Boolean) value).booleanValue());
	}

	protected abstract boolean isPushToFinal(int parameterIndex, boolean value);
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