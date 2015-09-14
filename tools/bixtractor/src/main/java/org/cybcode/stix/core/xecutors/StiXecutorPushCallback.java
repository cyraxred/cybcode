package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor.Parameter;

public interface StiXecutorPushCallback
{
	boolean isPushToFinal(StiXecutorContext context, Parameter<?> pushedParameter, Object pushedValue);
}