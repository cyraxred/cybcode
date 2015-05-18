package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.BiXtractor;

public class XorXtractor extends BiFunctionXtractor<Boolean, Boolean, Boolean>
{
	public XorXtractor(BiXtractor<Boolean> p0, BiXtractor<Boolean> p1)
	{
		super(p0, p1);
	}

	@Override protected Boolean evaluate(Boolean p0, Boolean p1)
	{
		return p0 ^ p1;
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_OPERATION;
	}
}