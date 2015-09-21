package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXtractor;

public interface StiXpressionNode
{
	public interface PushTarget
	{
		StiXtractor.Parameter<?> getXtractorParam();
		int getXtractorIndex();
		int getValueIndex();
	}
	
	StiXecutor createXecutor(StiXecutorConstructionContext context);
	StiXtractor<?> getXtractor();
	int getIndex();
	int mapParamIndex(int paramIndex);
	int getFrameLastIndex();
	
	List<StiXpressionNode.PushTarget> getCallbackTargets();
	List<StiXpressionNode.PushTarget> getPushTargets();
	List<StiXpressionNode.PushTarget> getNotifyTargets();
}