package org.cybcode.stix.api;

import com.google.common.base.Function;

public interface StiXFunction<F, T> extends Function<F, T>
{
	Object getOperationToken();
	int getOperationComplexity();
	Class<? extends T> resultType();
}