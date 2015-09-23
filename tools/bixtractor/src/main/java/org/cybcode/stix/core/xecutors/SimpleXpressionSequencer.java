package org.cybcode.stix.core.xecutors;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

public class SimpleXpressionSequencer implements StiXpressionSequencer
{
	private final LinkedList<PushTarget> immediateQueue = new LinkedList<>();
	private final LinkedList<PushTarget> postponeQueue = new LinkedList<>();
	
	@Override public void addImmediateTargets(List<PushTarget> targets)
	{
		immediateQueue.addAll(targets);
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
		if (!immediateQueue.isEmpty()) return immediateQueue.removeLast();
		if (!postponeQueue.isEmpty()) return postponeQueue.removeLast();
		return null;
	}

	@Override public void discardTargets(int startIndex, int endIndex)
	{
		discardTargets(startIndex, endIndex, postponeQueue);
		discardTargets(startIndex, endIndex, immediateQueue);
	}

	private static void discardTargets(int startIndex, int endIndex, List<PushTarget> targets)
	{
		for (Iterator<PushTarget> i = targets.iterator(); i.hasNext();) {
			int index = i.next().getXtractorIndex();
			if (index < startIndex || index >= endIndex) continue;
			i.remove();
		}
	}
}