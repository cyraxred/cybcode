package org.cybcode.stix.api;

import com.google.common.base.Function;

public interface StiXFunction<F, T> extends Function<F, T>, StiXtractorBase
{
	Class<? extends T> resultType();
}