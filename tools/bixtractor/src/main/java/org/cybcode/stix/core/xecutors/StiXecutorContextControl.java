package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;

public interface StiXecutorContextControl extends StiXecutorContextInspector
{
	StiXecutorContext getContext();
	int getNodeCount();

	void resetContext();

	StiXpressionNode setCurrentIndex(int xtractorIndex);
	StiXtractor<?> getCurrentXtractor();
	int getCurrentIndex();

	Object getPublicValue(int xecutorIndex);
	void clearRange(int startIndex, int endIndex);
	boolean setValue(int xecutorIndex, Object value);
	Object getValue(int valueIndex);
}