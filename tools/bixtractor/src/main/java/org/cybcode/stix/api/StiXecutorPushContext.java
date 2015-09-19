package org.cybcode.stix.api;

public interface StiXecutorPushContext extends StiXecutorContext
{
	StiXtractor<?> getCurrentXtractor();

	void setNextState(StiXecutor xecutor);
	void setFinalState();

	void onXourceFieldSkipped();
	void onXourceFieldParsed();
}