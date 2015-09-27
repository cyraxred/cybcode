package org.cybcode.stix.api;

import java.util.List;

public interface StiXpressionSequencer
{
	void addImmediateTargets(List<StiXpressionNode.PushTarget> targets);
	void addImmediateTarget(StiXpressionNode.PushTarget target);
	void addPostponeTargets(List<StiXpressionNode.PushTarget> targets);
	
	StiXpressionNode.PushTarget nextImmediateTarget();
	StiXpressionNode.PushTarget nextPostponedTargetBefore(StiXpressionNode node);
	void resetSequencer();
}