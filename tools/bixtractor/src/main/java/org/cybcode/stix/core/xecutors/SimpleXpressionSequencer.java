package org.cybcode.stix.core.xecutors;

import java.util.LinkedList;
import java.util.List;

import org.cybcode.stix.api.StiXpressionNode;
import org.cybcode.stix.api.StiXpressionSequencer;
import org.cybcode.stix.api.StiXpressionNode.PushTarget;

public class SimpleXpressionSequencer implements StiXpressionSequencer
{
	private final LinkedList<PushTarget> immediateQueue = new LinkedList<>();
	private final LinkedList<PushTarget> postponeQueue = new LinkedList<>();
	
	@Override public void addImmediateTargets(List<PushTarget> targets)
	{
		immediateQueue.addAll(0, targets); //LIFO-style behavior for batches
	}
	
	@Override public void addImmediateTarget(PushTarget target)
	{
		immediateQueue.add(0, target); //LIFO-style behavior for batches
	}

	@Override public void addPostponeTargets(List<PushTarget> targets)
	{
		postponeQueue.addAll(targets);
	}

	@Override public void resetSequencer()
	{
		immediateQueue.clear();
		postponeQueue.clear();
	}

	@Override public PushTarget nextImmediateTarget()
	{
		if (!immediateQueue.isEmpty()) return immediateQueue.removeLast();
		return null;
	}

	@Override public PushTarget nextPostponedTargetBefore(StiXpressionNode node)
	{
//		if (!immediateQueue.isEmpty()) return immediateQueue.removeLast();
		if (!postponeQueue.isEmpty()) return postponeQueue.removeLast();
		return null;
	}
}