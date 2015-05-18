package org.cybcode.tools.bixtractor.core;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XecutionContext;

public abstract class Parameter<P>
{
	private int paramIndex = -1;
	protected final BiXtractor<? extends P> extractor;
	
	public Parameter(BiXtractor<? extends P> extractor)
	{
		this.extractor = extractor;
	}
	
	public BiXtractor<? extends P> getExtractor()
	{
		return extractor;
	}

	protected Object get(XecutionContext context)
	{
		return context.getParamValue(paramIndex);
	}
	
	void setParamIndex(int paramIndex)
	{
		if (this.paramIndex == paramIndex) return;
		if (this.paramIndex >= 0) throw new IllegalArgumentException();
		this.paramIndex = paramIndex;
	}
}
