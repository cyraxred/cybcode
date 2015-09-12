package org.cybcode.tools.bixtractor.core;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XecutionContext;

public abstract class Parameter<P>
{
	private int paramIndex = -1;
	private boolean multiValue;
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
	
	public boolean has(XecutionContext context)
	{
		return context.hasParamValue(paramIndex);
	}
	
	void setParamIndex(int paramIndex, boolean multiValue)
	{
		if (this.paramIndex == paramIndex) return;
		if (this.paramIndex >= 0) throw new IllegalArgumentException();
		this.paramIndex = paramIndex;
		this.multiValue = multiValue;
	}
	
	protected boolean isMultiValue(XecutionContext context)
	{
		if (this.paramIndex < 0) throw new IllegalStateException();
		return multiValue;
	}
	
	
	@Override public String toString()
	{
		return toString(toNameString());
	}

	String toNameString()
	{
		return "p" + (paramIndex < 0 ? "?" : "" + paramIndex);
	}

	public String toString(String paramName)
	{
		return paramName + (this instanceof PushParameter ? "<<=" : "=") + extractor;
	}
}
