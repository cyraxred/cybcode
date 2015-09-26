package org.cybcode.stix.api;

import org.cybcode.stix.core.xecutors.StiXecutorContextControl;
import org.cybcode.stix.core.xecutors.StiXecutorContextInspector;
import org.cybcode.stix.core.xecutors.XpressionRunnerBuilder;

public interface StiXecutorContextBinder extends StiXecutorContextInspector
{
	StiXecutorContextControl bind(XpressionRunnerBuilder.Runner runner);
}