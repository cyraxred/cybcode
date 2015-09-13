package org.cybcode.stix.core;

import com.google.common.base.Function;

public interface FunctionWithComplexity<F, T> extends Function<F, T>
{
	int getOperationComplexity();
	@Override boolean equals(Object object);
	@Override int hashCode();
}