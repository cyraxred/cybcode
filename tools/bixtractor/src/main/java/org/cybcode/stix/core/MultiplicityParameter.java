package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.PushParameter;

abstract class MultiplicityParameter<P0, T> extends PushParameter<P0> //push each
{
	final StiXFunction<P0, ? extends T> fn;

	public MultiplicityParameter(StiXtractor<? extends P0> source, StiXFunction<P0, ? extends T> fn)
	{
		super(source);
		this.fn = fn;
	}

	@Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue) 
	{
		if (pushedValue == null) return null;
		return convert(pushedValue);
	};

	@SuppressWarnings("unchecked") T convert(Object pushedValue) 
	{ 
		return fn.apply((P0) pushedValue); 
	}
}