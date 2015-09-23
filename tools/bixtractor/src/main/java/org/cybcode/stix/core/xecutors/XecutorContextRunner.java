package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

import com.google.common.base.Function;

class XecutorContextRunner implements XpressionRunnerBuilder.Runner, Function<Object, Object>
{
	private final XpressionRunnerBuilder.Context context;
	private final StiXpressionSequencer sequencer;
	private final int nodeCount; 
	private int nextIndex;
	private StiXpressionNode currentNode;

	public XecutorContextRunner(XpressionRunnerBuilder.Context context, StiXpressionSequencer sequencer)
	{
		this.context = context;
		this.sequencer = sequencer;
		this.nodeCount = context.getNodeCount();
	}
	
	private void resetContext(Object rootValue)
	{
		sequencer.resetSequencer();
		context.resetContext(rootValue, this);
		setCurrentIndex(0);
	}
	
	private void setCurrentIndex(int xtractorIndex)
	{
		currentNode = context.setCurrentIndex(xtractorIndex);
	}

	@Override public boolean executeImmediateTargets()
	{
		StiXpressionNode.PushTarget pushTarget;
		while ((pushTarget = sequencer.nextImmediateTarget()) != null) {
			if (!context.evaluatePush(pushTarget)) continue;
			if (context.hasFrameFinalState()) return true;
		}
		return false;
	}
	
	@Override public boolean executePostponedTargetsBefore(StiXpressionNode nextNode)
	{
		int nextNodeIndex = nextNode.getIndex();
		StiXpressionNode.PushTarget pushTarget;
		while ((pushTarget = sequencer.nextPostponedTargetBefore(nextNode)) != null) {
			if (pushTarget.getXtractorIndex() <= nextNodeIndex && !pushTarget.getXtractorParam().getBehavior().isMandatory()) continue;
			if (!context.evaluatePush(pushTarget)) continue;
			if (context.hasFrameFinalState()) return true;
		}
		return false;
	}

	public Object apply(Object rootValue)
	{
		resetContext(rootValue);
		runExpression();
		return context.getPublicValue(nodeCount - 1);
	}

	@Override public void setPushValueOf(int targetIndex, List<PushTarget> pushTargets, Object finalValue)
	{
		sequencer.addImmediateTargets(pushTargets);
	}

	@Override public void setFinalValueOf(int xtractorIndex, List<PushTarget> targets)
	{
		sequencer.addPostponeTargets(targets);
	}

	private void runExpression()
	{
		while (nextIndex < nodeCount) {
			int currentIndex = nextIndex++;
			setCurrentIndex(currentIndex);

			if (currentNode == null) continue;
			
			if (executePostponedTargetsBefore(currentNode)) break;
			context.evaluateFinalState(currentIndex);
		}
	}
	
	@Override public void jumpTo(int index)
	{
		if (index < nextIndex) throw new IllegalArgumentException();
		nextIndex = index;
	}

	@Override public void discardPushTargets(int startIndex, int endIndex)
	{
		sequencer.discardTargets(startIndex, endIndex);
	}
}