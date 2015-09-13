package org.cybcode.stix;

import java.util.LinkedList;
import java.util.List;

import org.cybcode.stix.core.xecutors.StiXpressionNode;
import org.cybcode.stix.core.xecutors.StiXpressionSequencer;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

class SimpleXpressionSequencer implements StiXpressionSequencer
{
	private final LinkedList<PushTarget> pushQueue = new LinkedList<>();
	private final LinkedList<PushTarget> notifyQueue;
	
	public SimpleXpressionSequencer(boolean enableNotify)
	{
		if (enableNotify) {
			notifyQueue = new LinkedList<>();
		} else {
			notifyQueue = null;
		}
	}

	@Override public void addPushTargets(List<PushTarget> targets)
	{
		pushQueue.addAll(targets);
	}

	@Override public void addNotifyTargets(List<PushTarget> targets)
	{
		if (notifyQueue == null) return;
		notifyQueue.addAll(targets);
	}

	@Override public PushTarget nextTargetBefore(StiXpressionNode node)
	{
		if (!pushQueue.isEmpty()) return pushQueue.removeLast();
		if (notifyQueue == null || notifyQueue.isEmpty()) return null;
		return notifyQueue.removeLast();
	}

	@Override public void resetSequencer()
	{
		pushQueue.clear();
	}
}