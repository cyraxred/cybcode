package org.cybcode.stix.api;

public class StiXourceFnField<P0, T> extends StiXourceField<P0, T>
{
	private final StiXFunction<P0, T> fn;

	public StiXourceFnField(StiXource<?, ?, ?, P0> p0, ValueLimit mode, StiXFunction<P0, T> fn)
	{
		super(p0, mode);
		if (fn == null) throw new NullPointerException();
		this.fn = fn;
	}

	@Override public Object getOperationToken()
	{
		return TokenPair.of(super.getOperationToken(), fn.getOperationToken());
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(fn, fn.getOperationComplexity());
	}
	
	@Override protected T calculate(P0 pv0)
	{
		if (pv0 == null) return null;
		return fn.apply(pv0);
	}
	
	@Override public Class<? extends T> resultType()
	{
		return fn.resultType();
	}
}