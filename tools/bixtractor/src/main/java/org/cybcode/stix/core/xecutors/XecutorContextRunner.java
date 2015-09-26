package org.cybcode.stix.core.xecutors;

import java.util.Arrays;
import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorContextBinder;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

import com.google.common.base.Function;

class XecutorContextRunner implements XpressionRunnerBuilder.Runner, Function<Object, Object>, StiXecutorPushContext
{
	private final StiXecutorContextControl context;
	private final StiXpressionSequencer sequencer;
	private final int nodeCount; 
	private final StiXecutor[] initialState;
	private final StiXecutor[] currentState;
	
	private final CtxFrame outerFrame;
	private CtxFrame currentFrame;

	private StiXecutorStatsCollector stats;
	
	private int nextIndex;
//	private StiXpressionNode currentNode;

	public XecutorContextRunner(StiXecutorContextBinder contextBinder, StiXpressionSequencer sequencer)
	{
		this.sequencer = sequencer;
		setStatsCollector(null);

		this.context = contextBinder.bind(this);
		this.nodeCount = context.getNodeCount();
		
		this.initialState = new StiXecutor[nodeCount];
		this.currentState = new StiXecutor[nodeCount];
		this.outerFrame = new CtxFrame(nodeCount);
		this.currentFrame = outerFrame;
	}

	public void setStatsCollector(StiXecutorStatsCollector stats)
	{
		this.stats = stats == null ? StiXecutorStatsCollector.NULL : stats;
	}
	
	private void resetContext(Object rootValue)
	{
		if (rootValue == null) throw new NullPointerException();
		stats.resetStats(nodeCount);
		sequencer.resetSequencer();
		
		if (initialState[0] == null) {
			createInitialState();
			initialState[0] = DefaultXecutors.FINAL;
		} else {
			Arrays.fill(currentState, null);
		}
		currentFrame = outerFrame.enterFrame(outerFrame);
		
		context.resetContext();
		context.setValue(0, rootValue);

		/* Imitation of root being evaluated */
		StiXpressionNode currentNode = context.setCurrentIndex(0);
		currentState[0] = DefaultXecutors.FINAL;
		stats.onEvaluated(currentNode, rootValue);
		setFinalValueOf(0, currentNode.getNotifyTargets());
	}
	
	private void createInitialState()
	{
		XecutorConstructionContext context = new XecutorConstructionContext(nodeCount, outerFrame);
		for (int i = 0; i < nodeCount - 1; i++) { //MUST BE in direct order, otherwise frame initialization will break 
			StiXpressionNode node = this.context.getNode(i);
			context.setNode(node);
			StiXecutor xecutor = node.createXecutor(context);
			initialState[i] = xecutor;
		}
	}

//	private void setCurrentIndex(int xtractorIndex)
//	{
//		currentNode = context.setCurrentIndex(xtractorIndex);
//	}

	void jumpToFrame(CtxFrame frame)
	{
		currentFrame.setReturnPosition(nextIndex);
		jumpTo(frame.getStartIndex());
	}
	
	void enterFrame(CtxFrame frame)
	{
		currentFrame = currentFrame.enterFrame(frame);
		int startIndex = frame.getStartIndex(); 
		int endIndex = frame.getEndIndex();
		Arrays.fill(currentState, startIndex + 1, endIndex, null);
		context.clearRange(startIndex + 1, endIndex);
		stats.onFrameEnter(startIndex);
	}

	void skipFrame(CtxFrame frame)
	{
		int startIndex = frame.getStartIndex();
		int endIndex = frame.getEndIndex();
		currentState[startIndex] = DefaultXecutors.FINAL;
		currentState[endIndex - 1] = DefaultXecutors.FINAL;
		context.setValue(endIndex - 1, null);
		jumpTo(endIndex);
		stats.onFrameSkip(startIndex);
	}

	void resolveFrame(CtxFrame.ResultMarker frameMarker, boolean repeatable)
	{
		final CtxFrame resolvedFrame = frameMarker.getFrame();
		if (currentFrame != resolvedFrame) throw new IllegalStateException();

		int startIndex = resolvedFrame.getStartIndex();
		if (!frameMarker.setFinalState()) {
			stats.onFrameResolve(startIndex, false);
			return;
		}
		
		currentState[startIndex] = repeatable ? initialState[startIndex] : DefaultXecutors.FINAL;
		
		resolveInnerFrames(resolvedFrame);
		
		jumpTo(resolvedFrame.getOuterFrame().getReturnPosition());
		stats.onFrameResolve(startIndex, true);
	}
	
	private void resolveInnerFrames(CtxFrame resolvedFrame)
	{
		CtxFrame frame = currentFrame;
		while (frame != resolvedFrame) {
			frame = frame.getOuterFrame();
			if (frame == outerFrame) {
				throw new IllegalStateException("Inactive frame was resolved");
			}
		}
	}

	@Override public void onXourceFieldSkipped()
	{
		stats.onFieldSkipped();
	}

	@Override public void onXourceFieldParsed()
	{
		stats.onFieldParsed();
	}

	@Override public void setNextState(StiXecutor xecutor)
	{
		if (xecutor == null) throw new NullPointerException();
		currentState[context.getCurrentIndex()] = xecutor;
	}

	@Override public void setFinalState()
	{
		setNextState(DefaultXecutors.FINAL);
	}

	public void evaluateFinalState(int index)
	{
		StiXecutor xecutor = currentState[index];
		if (xecutor == DefaultXecutors.FINAL) return;
		
		while (!currentFrame.isInsideFrame(index)) {
			currentFrame = currentFrame.getOuterFrame();
		}
		
		StiXpressionNode node = context.setCurrentIndex(index);
		Object finalValue;
		if (xecutor != null) {
			finalValue = xecutor.evaluateFinal(this);
		} else {
			xecutor = initialState[index];
			if (xecutor == null) {
				finalValue = context.getCurrentXtractor().apply(getXecutorContext());
			} else {
				finalValue = xecutor.evaluateFinal(this);
			}
		}
		currentState[index] = DefaultXecutors.FINAL;
		stats.onEvaluated(node, finalValue);
		if (!context.setValue(index, finalValue) || finalValue == null) return;
		setFinalValueOf(index, node.getNotifyTargets());
	}
	
	public boolean evaluatePush(StiXpressionNode.PushTarget target)
	{
		int index = target.getXtractorIndex();
		if (hasFinalState(index)) return false;
		
		Object pushedValue = context.getValue(target.getValueIndex());
		if (pushedValue == null) return false;
		
		evaluatePush0(index, target.getXtractorParam(), pushedValue);
		return true;
	}
	
	private void evaluatePush0(int targetIndex, Parameter<?> targetParam, Object pushedValue)
	{
		StiXpressionNode node = context.setCurrentIndex(targetIndex);
		stats.onPushAttempt(node, targetParam, pushedValue);

		Object finalValue;
		StiXecutor xecutor = currentState[targetIndex];
		if (xecutor == null) {
			xecutor = initialState[targetIndex];
			if (xecutor == null) {
				finalValue = targetParam.evaluatePush(this, pushedValue);
			} else {
				currentState[targetIndex] = xecutor;
				finalValue = xecutor.evaluatePush(this, targetParam, pushedValue);
			}
		} else {
			if (xecutor == DefaultXecutors.FINAL) return;
			finalValue = xecutor.evaluatePush(this, targetParam, pushedValue);
		}
		
		if (currentState[targetIndex] == DefaultXecutors.FINAL) {
			if (targetIndex + 1 == outerFrame.getEndIndex() && outerFrame == currentFrame) {
				outerFrame.resetFrame(true);
			}
			if (!context.setValue(targetIndex, finalValue) || finalValue == null) return;
			stats.onPushEvaluated(node, finalValue);
	
			setFinalValueOf(targetIndex, node.getNotifyTargets());
		} else {
			if (finalValue == null || !context.setValue(targetIndex, finalValue)) return;
			stats.onPushEvaluated(node, finalValue);
			
			setPushValueOf(targetIndex, node.getPushTargets(), finalValue);
		}
	}

	void evaluateDirectPush(StiXpressionNode.PushTarget[] targets, Object pushedValue)
	{
		if (pushedValue == null || targets.length == 0) return;
		
		int callingNode = context.getCurrentIndex();
		try {
			for (StiXpressionNode.PushTarget target : targets) {
				int index = target.getXtractorIndex();
				if (hasFinalState(index)) continue;
				if (executePostponedTargetsBefore(context.getNode(index))) return;
				evaluatePush0(index, target.getXtractorParam(), pushedValue);
				if (hasFrameFinalState()) break;
			}
			executeImmediateTargets();
		} finally {
			context.setCurrentIndex(callingNode);
		}
	}
	
	void evaluateDirectPush(StiXpressionNode.PushTarget target, Object pushedValue)
	{
		if (pushedValue == null) return;
		
		int callingNode = context.getCurrentIndex();
		try {
			int index = target.getXtractorIndex();
			if (hasFinalState(index)) return;
			if (executePostponedTargetsBefore(context.getNode(index))) return;
			evaluatePush0(index, target.getXtractorParam(), pushedValue);
			executeImmediateTargets();		
		} finally {
			context.setCurrentIndex(callingNode);
		}
	}
	
	private boolean executeImmediateTargets()
	{
		StiXpressionNode.PushTarget pushTarget;
		while ((pushTarget = sequencer.nextImmediateTarget()) != null) {
			if (!evaluatePush(pushTarget)) continue;
			if (hasFrameFinalState()) return true;
		}
		return false;
	}
	
	private boolean executePostponedTargetsBefore(StiXpressionNode nextNode)
	{
		int nextNodeIndex = nextNode.getIndex();
		StiXpressionNode.PushTarget pushTarget;
		while ((pushTarget = sequencer.nextPostponedTargetBefore(nextNode)) != null) {
			if (pushTarget.getXtractorIndex() <= nextNodeIndex && !pushTarget.getXtractorParam().getBehavior().isMandatory()) continue;
			if (!evaluatePush(pushTarget)) continue;
			if (hasFrameFinalState()) return true;
		}
		return false;
	}

	public Object apply(Object rootValue)
	{
		resetContext(rootValue);
		runExpression();
		return context.getPublicValue(nodeCount - 1);
	}

	private void setPushValueOf(int targetIndex, List<PushTarget> pushTargets, Object finalValue)
	{
		sequencer.addImmediateTargets(pushTargets);
	}

	private void setFinalValueOf(int xtractorIndex, List<PushTarget> targets)
	{
		sequencer.addPostponeTargets(targets);
	}

	private void runExpression()
	{
		while (nextIndex < nodeCount) {
			int currentIndex = nextIndex++;
			StiXpressionNode node = context.setCurrentIndex(currentIndex);

			if (node == null) continue;
			
			if (executePostponedTargetsBefore(node)) break;
			evaluateFinalState(currentIndex);
		}
	}
	
	private void jumpTo(int index)
	{
		if (index <= 0) throw new IllegalArgumentException();
		nextIndex = index;
	}

	@Override public StiXecutorContext getXecutorContext()
	{
		return context.getContext();
	}

	@Override public boolean hasFinalState(int xtractorIndex)
	{
		return currentState[xtractorIndex] == DefaultXecutors.FINAL;
	}

	@Override public boolean hasFrameFinalState()
	{
		return currentFrame.hasFinalState();
	}

	@Override public StiXtractor<?> getCurrentXtractor()
	{
		return context.getCurrentXtractor();
	}

	@Override public Object getInterimValue()
	{
		int index = context.getCurrentIndex();
		if (hasFinalState(index)) throw new IllegalStateException();
		return context.getValue(index);
	}

	@Override public void setInterimValue(Object value)
	{
		int index = context.getCurrentIndex();
		if (hasFinalState(index)) throw new IllegalStateException();
		context.setValue(index, value);
	}

	@Override public boolean hasInterimValue()
	{
		int index = context.getCurrentIndex();
		if (hasFinalState(index)) return false;
		return context.getValue(index) != null;
	}
}