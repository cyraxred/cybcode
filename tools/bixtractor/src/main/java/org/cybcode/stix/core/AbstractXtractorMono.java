package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.ops.StiX_Const;

abstract class AbstractXtractorMono<P0, T> implements StiXtractor<T>
{
	protected final Parameter<P0> p0;

	AbstractXtractorMono(Parameter<P0> p0)
	{
		this.p0 = p0;
	}
	
	@Override public boolean isRepeatable()
	{
		return p0.isRepeatable();
	}
	
	protected abstract T calculate(P0 p0);

	@Override public int paramCount()
	{
		return 1;
	}
	
	@Override public void visit(StiXtractor.Visitor visitor)
	{
		visitor.visitParameter(p0);
	}

	@Override public StiXtractor<? extends T> curry(int parameterIndex, Object value)
	{
		if (parameterIndex != 0) throw new IllegalArgumentException();
		@SuppressWarnings("unchecked") P0 pv0 = (P0) value;
		T result = calculate(pv0); 
		return StiX_Const.of(result);
	}
}