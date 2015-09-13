package org.cybcode.stix.core.xecutors;

import java.util.List;

public interface StiXpressionSequencer
{
	public abstract void addPushTargets(List<StiXpressionNode.PushTarget> targets);
	public abstract void addNotifyTargets(List<StiXpressionNode.PushTarget> targets);
	
	public abstract StiXpressionNode.PushTarget nextTargetBefore(StiXpressionNode node);
	public abstract void resetSequencer();
}