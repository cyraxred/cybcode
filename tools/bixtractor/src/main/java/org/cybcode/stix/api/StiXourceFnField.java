package org.cybcode.stix.api;


public class StiXourceFnField<P0, T> extends StiXourceField<P0, T>
{
	private final StiXFunction<P0, T> fn;

	public StiXourceFnField(StiXource<?, ?, ?, P0> p0, ValueMode mode, StiXFunction<P0, T> fn)
	{
		super(p0, mode);
		if (fn == null) throw new NullPointerException();
		this.fn = fn;
	}

	@Override public Object getOperationToken()
	{
		return TokenPair.of(super.getOperationToken(), fn.getOperationToken());
	}

	@Override public int getOperationComplexity()
	{
		return fn.getOperationComplexity();
	}

	@Override protected T transform(P0 pv0)
	{
		return fn.apply(pv0);
	}
	
	@Override public Class<? extends T> resultType()
	{
		return fn.resultType();
	}
}