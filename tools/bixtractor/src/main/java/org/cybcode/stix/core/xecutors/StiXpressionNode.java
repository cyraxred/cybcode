package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXpressionContext;
import org.cybcode.stix.api.StiXtractor;

public interface StiXpressionNode
{
	public interface PushTarget
	{
		StiXtractor.Parameter<?> getXtractorParam();
		int getXtractorIndex();
		//StiXpressionNode getTargetNode();
		int getValueIndex();
	}
	
	StiXecutor createXecutor(StiXpressionContext context);
	StiXtractor<?> getXtractor();
	int getIndex();
	int mapParamIndex(int paramIndex);
	
	List<StiXpressionNode.PushTarget> getPushTargets();
	List<StiXpressionNode.PushTarget> getNotifyTargets();
}