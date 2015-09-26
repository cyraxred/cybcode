package org.cybcode.stix.core.xecutors;

import java.util.List;

public interface StiXpressionSequencer
{
	void addImmediateTargets(List<StiXpressionNode.PushTarget> targets);
	void addPostponeTargets(List<StiXpressionNode.PushTarget> targets);
	
	StiXpressionNode.PushTarget nextImmediateTarget();
	StiXpressionNode.PushTarget nextPostponedTargetBefore(StiXpressionNode node);
	void resetSequencer();
}