package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.TokenPair;

public abstract class StiXtractorLimiter<P0, T> extends AbstractXtractorMono<P0, T>
{
	private final Multiplicity mode;
	
	public StiXtractorLimiter(StiXtractor<? extends P0> p0, Multiplicity mode, StiXFunction<P0, ? extends T> fn)
	{
		super(mode.createParameter(p0, fn));
		this.mode = mode;
	}

	@SuppressWarnings("unchecked") @Override public final T apply(StiXecutorContext context)
	{
		if (!mode.usesInterim()) return null;
		Object value = context.getInterimValue();
		if (value == null) return null;
		return ((MultiplicityParameter<P0, T>) p0).convert(value);
	}
	
	@Override public Object getOperationToken()
	{
		return TokenPair.of(mode, getFn().getOperationToken());
	}
	
	@SuppressWarnings("unchecked") protected final StiXFunction<P0, ? extends T> getFn()
	{
		return ((MultiplicityParameter<P0, T>) p0).fn;
	}
	
	@SuppressWarnings("unchecked") @Override protected final T calculate(P0 p0)
	{
		return ((MultiplicityParameter<P0, T>) p0).convert(p0);
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(getFn(), mode.getOperationComplexity());
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		return null;
	}

	@Override public Class<? extends T> resultType()
	{
		return getFn().resultType();
	}
}