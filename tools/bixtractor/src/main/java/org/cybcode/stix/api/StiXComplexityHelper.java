package org.cybcode.stix.api;

import com.google.common.base.Function;

public interface StiXComplexityHelper
{
	int getComplexityOf(StiXtractor<?> xtractor, int defaultComplexity);
	int getComplexityOf(Function<?, ?> fn, int defaultComplexity);
	
	public static final StiXComplexityHelper DEFAULT = new StiXComplexityHelper() 
	{
		@Override public int getComplexityOf(StiXtractor<?> xtractor, int defaultComplexity) { return defaultComplexity; }
		@Override public int getComplexityOf(Function<?, ?> fn, int defaultComplexity) { return defaultComplexity; }
	};
}