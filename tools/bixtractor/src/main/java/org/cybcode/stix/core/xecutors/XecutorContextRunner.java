package org.cybcode.stix.core.xecutors;

import java.util.ArrayList;
import java.util.List;

import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

import com.google.common.base.Function;

class XecutorContextRunner implements XpressionRunnerBuilder.Runner, Function<Object, Object>
{
	private final XpressionRunnerBuilder.Context context;
	private final StiXpressionSequencer sequencer;
	private final int nodeCount; 
	private final List<CtxFrame> frames;
	private CtxFrame currentFrame;
	private int nextIndex;
	private StiXpressionNode currentNode;

	public XecutorContextRunner(XpressionRunnerBuilder.Context context, StiXpressionSequencer sequencer)
	{
		this.context = context;
		this.sequencer = sequencer;
		this.nodeCount = context.getNodeCount();
		this.currentFrame = new CtxFrame(0, nodeCount - 1);
		this.frames = new ArrayList<>(8);
		this.frames.add(this.currentFrame);
	}
	
	private void resetContext(Object rootValue)
	{
		sequencer.resetSequencer();
		currentFrame = CtxFrame.closeAllFrames(frames);

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
			if (hasFrameResultValue()) return true;
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
			if (hasFrameResultValue()) return true;
		}
		return false;
	}

	private void runExpression()
	{
		while (nextIndex < nodeCount) {
			int currentIndex = nextIndex++;
			setCurrentIndex(currentIndex);
		
			if (currentNode != null) {
				if (executePostponedTargetsBefore(currentNode)) break;
				context.evaluateFinalState(currentIndex);
			}
			
			if (currentFrame.isEndOfFrame(currentIndex)) {
				exitFrame();
			}
		}
	}
	
	public Object apply(Object rootValue)
	{
		resetContext(rootValue);
		runExpression();
		return context.getPublicValue(nodeCount - 1);
	}

	@Override public void enterFrame()
	{
		int rootIndex = currentNode.getIndex();
		int resultIndex = currentNode.getFrameResultIndex();
		currentFrame = currentFrame.createInner(rootIndex, resultIndex, frames);
		context.resetFrameContent(rootIndex, resultIndex);
	}
	
	private void exitFrame()
	{
		if (!currentFrame.isInnerFrame()) return; //don't need for the outermost frame
		int startIndex = currentFrame.getRootIndex();
		currentFrame.closeFrame();
		context.finalizeFrameContent(startIndex);
	}

	@Override public boolean hasFrameResultValue()
	{
		return currentFrame.hasResult();
	}

	@Override public void setPushValueOf(int targetIndex, List<PushTarget> pushTargets, Object finalValue)
	{
		sequencer.addImmediateTargets(pushTargets);
	}

	@Override public void setFinalValueOf(int xtractorIndex, List<PushTarget> targets)
	{
		sequencer.addPostponeTargets(targets);
		if (!currentFrame.setHasResult(xtractorIndex)) return;
		nextIndex = currentFrame.getResultIndex() + 1;
	}
}