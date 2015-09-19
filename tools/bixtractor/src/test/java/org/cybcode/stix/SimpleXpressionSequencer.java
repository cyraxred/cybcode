package org.cybcode.stix;

import java.util.LinkedList;
import java.util.List;

import org.cybcode.stix.core.xecutors.StiXpressionNode;
import org.cybcode.stix.core.xecutors.StiXpressionSequencer;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

class SimpleXpressionSequencer implements StiXpressionSequencer
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
		
		int nodeIndex = node.getIndex();
		
		while (!postponeQueue.isEmpty()) {
			PushTarget result = postponeQueue.removeLast();
			if (result.getXtractorIndex() > nodeIndex || result.getXtractorParam().getBehavior().isMandatory()) return result;
		}
		return null;
	}
}