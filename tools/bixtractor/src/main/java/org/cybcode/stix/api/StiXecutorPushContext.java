package org.cybcode.stix.api;

public interface StiXecutorPushContext
{
	StiXecutorContext getXecutorContext();

	void setNextState(StiXecutor xecutor);
	void setFinalState();

	void onXourceFieldSkipped();
	void onXourceFieldParsed();

	StiXtractor<?> getCurrentXtractor();

	Object getInterimValue();
	void setInterimValue(Object value);
	boolean hasFrameFinalState();
	boolean hasInterimValue();
}