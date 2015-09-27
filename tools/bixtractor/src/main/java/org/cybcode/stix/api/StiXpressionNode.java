package org.cybcode.stix.api;

import java.util.List;

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
	
	List<StiXpressionNode.PushTarget> getCallbackTargets();
	List<StiXpressionNode.PushTarget> getPushTargets();
	List<StiXpressionNode.PushTarget> getNotifyTargets();

	int getFrameStartIndex();
}