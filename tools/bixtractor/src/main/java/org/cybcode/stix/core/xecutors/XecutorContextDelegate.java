package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;

public abstract class XecutorContextDelegate<T extends StiXecutorContext> implements StiXecutorContext
{
	protected final T backing;

	public XecutorContextDelegate(T backing)
	{
		if (backing == null) throw new NullPointerException();
		this.backing = backing;
	}

	@Override public Object getParamValue(int paramIndex)
	{
		return backing.getParamValue(paramIndex);
	}

	@Override public Object getInterimValue()
	{
		return backing.getInterimValue();
	}

	@Override public boolean hasParamFinalValue(int paramIndex)
	{
		return backing.hasParamFinalValue(paramIndex);
	}

	@Override public void setInterimValue(Object value)
	{
		backing.setInterimValue(value);
	}

	@Override public boolean hasInterimValue()
	{
		return backing.hasInterimValue();
	}

	@Override public StiXtractor<?> getCurrentXtractor()
	{
		return backing.getCurrentXtractor();
	}

	@Override public boolean hasResultValue()
	{
		return backing.hasResultValue();
	}
}