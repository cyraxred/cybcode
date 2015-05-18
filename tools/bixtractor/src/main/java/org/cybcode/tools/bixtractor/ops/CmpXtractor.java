package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.BiXtractor;

public class CmpXtractor extends BiFunctionXtractor<Object, Object, Boolean>
{
	public enum CmpType
	{
		EQ,
	}

	private final CmpType cmpType;
	
	public CmpXtractor(CmpType cmpType, BiXtractor<?> p0, BiXtractor<?> p1)
	{
		super(p0, p1);
		this.cmpType = cmpType;
	}

	@Override public Object getOperationToken()
	{
		return cmpType;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_OPERATION;
	}

	@Override protected Boolean evaluate(Object p0, Object p1)
	{
		if (p0 == null || p1 == null) return null;
		
		switch (cmpType) {
			case EQ: return p0.equals(p1);

			default:
				return null;
		}
	}
}