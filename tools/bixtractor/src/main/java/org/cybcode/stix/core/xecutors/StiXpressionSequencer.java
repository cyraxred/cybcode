package org.cybcode.stix.core.xecutors;

import java.util.List;

public interface StiXpressionSequencer
{
	public abstract void addImmediateTargets(List<StiXpressionNode.PushTarget> targets);
	public abstract void addPostponeTargets(List<StiXpressionNode.PushTarget> targets);
	public void discardTargets(int startIndex, int endIndex);
	
	public abstract StiXpressionNode.PushTarget nextImmediateTarget();
	public abstract StiXpressionNode.PushTarget nextPostponedTargetBefore(StiXpressionNode node);
	public abstract void resetSequencer();

}