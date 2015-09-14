package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXpressionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xecutors.XecutorMono;
import org.cybcode.stix.core.xecutors.XecutorMonoPush;
import org.cybcode.stix.ops.StiX_Const;

public abstract class StiXtractorMono<P0, T> implements StiXtractor<T>
{
	private final Parameter<P0> p0;

	public StiXtractorMono(StiXtractor<? extends P0> p0)
	{
		this.p0 = new NotifyParameter<P0>(p0);
	}

	StiXtractorMono(Parameter<P0> p0)
	{
		this.p0 = p0;
	}
	
	@Override public boolean isRepeatable()
	{
		return p0.isRepeatable();
	}
	
	@Override public final T evaluate(StiXecutorContext context)
	{
		P0 pv0 = p0.getValue(context);
		T result = calculate(pv0);
		return result;
	}
	
	protected abstract T calculate(P0 p0);

	@Override public StiXecutor createXecutor(StiXpressionContext context)
	{
		return isRepeatable() ? XecutorMonoPush.getInstance() : XecutorMono.getInstance();
	}

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
