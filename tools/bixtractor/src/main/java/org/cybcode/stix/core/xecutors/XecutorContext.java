package org.cybcode.stix.core.xecutors;

import java.util.Arrays;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.ops.StiX_Root;

class XecutorContext implements StiXecutorContext, StiXecutorPushContext, XpressionRunnerBuilder.Context
{
	private final StiXpressionNode[] nodes;

	//Late initialization
	private StiXecutor[] initialState;
	private StiXecutor[] currentState;
	private Object[] results;
	
	private StiXecutorStatsCollector stats;
	private XpressionRunnerBuilder.Runner runner;

	private final CtxFrame outerFrame;
	private CtxFrame currentFrame;
	
	private StiXpressionNode currentNode;
	private int currentIndex;
	
	public XecutorContext(StiXpressionNode[] nodes)
	{
		if (!(nodes[0].getXtractor() instanceof StiX_Root)) throw new IllegalArgumentException("Root must be present as index 0");
		this.nodes = nodes;
		setStatsCollector(null);

		this.outerFrame = new CtxFrame(nodes.length);
		this.currentFrame = outerFrame;
	}

	@Override public void setStatsCollector(StiXecutorStatsCollector stats)
	{
		this.stats = stats == null ? StiXecutorStatsCollector.NULL : stats;
	}
	
	public void resetContext(Object rootValue, XpressionRunnerBuilder.Runner runner)
	{
		if (rootValue == null) throw new NullPointerException();
		if (runner == null) throw new NullPointerException();
		this.runner = runner;
		stats.resetStats(nodes.length);
		
		if (currentState == null) {
			initialState = new StiXecutor[nodes.length];
			createInitialState();
			initialState[0] = DefaultXecutors.FINAL;

			currentState = new StiXecutor[nodes.length];
			results = new Object[nodes.length];
		} else {
			Arrays.fill(currentState, null);
			Arrays.fill(results, null);
		}
		
		currentFrame = outerFrame.enterFrame(outerFrame);

		/* Imitation of root being evaluated */
		currentIndex = 0;
		currentNode = nodes[0];
		currentState[0] = DefaultXecutors.FINAL;
		setValue(0, rootValue);
		stats.onEvaluated(currentNode, rootValue);
		runner.setFinalValueOf(0, currentNode.getNotifyTargets());
	}

	void enterFrame(CtxFrame frame)
	{
		currentFrame = currentFrame.enterFrame(frame);
		int startIndex = frame.getStartIndex(); 
		int endIndex = frame.getEndIndex();
		Arrays.fill(currentState, startIndex, endIndex, null);
		Arrays.fill(results, startIndex, endIndex, null);
	}
	
	void skipFrame(CtxFrame frame)
	{
		int startIndex = frame.getStartIndex();
		int endIndex = frame.getEndIndex();
		currentState[startIndex] = DefaultXecutors.FINAL;
		currentState[endIndex - 1] = DefaultXecutors.FINAL;
		results[endIndex - 1] = null;
		runner.jumpTo(endIndex);
	}

	void resolveFrame(CtxFrame.ResultMarker frameMarker, boolean repeatable)
	{
		final CtxFrame resolvedFrame = frameMarker.getFrame();
		if (currentFrame != resolvedFrame) throw new IllegalStateException();
		if (!frameMarker.setFinalState()) return;
		
		int startIndex = resolvedFrame.getStartIndex();
		int endIndex = resolvedFrame.getEndIndex();
		currentState[startIndex] = repeatable ? initialState[startIndex] : DefaultXecutors.FINAL;
		
		resolveInnerFrames(resolvedFrame);
		
		runner.discardPushTargets(startIndex, endIndex);
		runner.jumpTo(endIndex);
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

//	@Override public void resetFrameContent(int rootIndex, int resultIndex, boolean finalState)
//	{
//		ensureFrameStart(rootIndex);
//		if (finalState) {
//			Arrays.fill(currentState, rootIndex, resultIndex + 1, DefaultXecutors.FINAL);
//		} else {
//			if (currentState[rootIndex] == null) return;
//			Arrays.fill(currentState, rootIndex, resultIndex + 1, null);
//		}
//		Arrays.fill(results, rootIndex, resultIndex + 1, null);
//	}
//	
//	@Override public void prepareFrameContent(int startIndex, int endIndex)
//	{
//		ensureFrameStart(rootIndex);
//		if (currentState[rootIndex] == null) return;
//			Arrays.fill(currentState, rootIndex, resultIndex + 1, null);
//		}
//		Arrays.fill(results, rootIndex, resultIndex + 1, null);
//	}
//	
//	@Override public void completeFrameContent(int rootIndex)
//	{
//		currentState[rootIndex] = ensureFrameStart(rootIndex);
//	}

	private void createInitialState()
	{
		XecutorConstructionContext context = new XecutorConstructionContext(nodes.length, outerFrame);
		for (int i = 0; i < nodes.length - 1; i++) { //MUST BE in direct order, otherwise frame initialization will break 
			StiXpressionNode node = nodes[i];
			context.setNode(node);
			StiXecutor xecutor = node.createXecutor(context);
			initialState[i] = xecutor;
		}
	}
	
	private boolean hasPublicValue(int xtractorIndex)
	{
		StiXecutor state = currentState[xtractorIndex];
		return state == DefaultXecutors.FINAL;
	}
	
	private boolean setValue(int xtractorIndex, Object value)
	{
		if (value == StiXecutor.KEEP_LAST_VALUE) return false;
		results[xtractorIndex] = value;
		return true;
	}

	private boolean hasValue(int xtractorIndex)
	{
		return results[xtractorIndex] != null;
	}

	private Object getValue(int xtractorIndex)
	{
		return results[xtractorIndex];
	}

	public Object getPublicValue(int xtractorIndex)
	{
		if (!hasPublicValue(xtractorIndex)) return null;
		return getValue(xtractorIndex);
	}

	
	@Override public Object getParamValue(int paramIndex)
	{
		int index = mapParamIndex(paramIndex);
		if (!hasPublicValue(index)) return null;
		return getValue(index);
	}

	@Override public boolean hasParamFinalValue(int paramIndex)
	{
		int index = mapParamIndex(paramIndex);
		return hasPublicValue(index);
	}

	@Override public boolean hasFrameFinalState()
	{
		return currentFrame.hasFinalState();
	}
	
	@Override public void setInterimValue(Object value)
	{
		int index = currentIndex();
		if (hasPublicValue(index)) {
			throw new IllegalStateException();
		}
		setValue(index, value);
	}
	
	@Override public Object getInterimValue()
	{
		int index = currentIndex();
		if (hasPublicValue(index)) throw new IllegalStateException();
		return getValue(index);
	}
	
	@Override public boolean hasInterimValue()
	{
		int index = currentIndex();
		return currentState[index] != DefaultXecutors.FINAL && hasValue(index);
	}
	
	private int currentIndex()
	{
		return currentIndex;
	}
	
	public StiXpressionNode setCurrentIndex(int index)
	{
		if (index < 0 || index >= nodes.length) throw new IllegalArgumentException();
		if (currentIndex != index) {
			currentIndex = index;
			currentNode = nodes[index];
		}
		return currentNode;
	}

	private int mapParamIndex(int paramIndex)
	{
		return currentNode.mapParamIndex(paramIndex);
	}
	
	@Override public StiXtractor<?> getCurrentXtractor()
	{
		return currentNode.getXtractor();
	}

	public int getNodeCount()
	{
		return nodes.length;
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
		currentState[currentIndex] = xecutor;
	}

	@Override public void setFinalState()
	{
		currentState[currentIndex] = DefaultXecutors.FINAL;
	}

	public void evaluateFinalState(int index)
	{
		StiXecutor xecutor = currentState[index];
		if (xecutor == DefaultXecutors.FINAL) return;
		
		while (!currentFrame.isInsideFrame(index)) {
			currentFrame = currentFrame.getOuterFrame();
		}
		
		StiXpressionNode node = setCurrentIndex(index);
		Object finalValue;
		if (xecutor != null) {
			finalValue = xecutor.evaluateFinal(this);
		} else {
			finalValue = getCurrentXtractor().apply(this);
		}
		currentState[index] = DefaultXecutors.FINAL;
		stats.onEvaluated(currentNode, finalValue);
		if (!setValue(index, finalValue) || finalValue == null) return;
		runner.setFinalValueOf(index, node.getNotifyTargets());
	}
	
	public boolean evaluatePush(StiXpressionNode.PushTarget target)
	{
		int index = target.getXtractorIndex();
		if (hasPublicValue(index)) return false;
		
		Object pushedValue = getValue(target.getValueIndex());
		if (pushedValue == null) return false;
		
		evaluatePush0(index, target.getXtractorParam(), pushedValue);
		return true;
	}
	
	private void evaluatePush0(int targetIndex, Parameter<?> targetParam, Object pushedValue)
	{
		StiXpressionNode node = setCurrentIndex(targetIndex);
		stats.onPushAttempt(currentNode, targetParam, pushedValue);

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
			if (!setValue(targetIndex, finalValue) || finalValue == null) return;
			stats.onPushEvaluated(currentNode, finalValue);
	
			runner.setFinalValueOf(targetIndex, node.getNotifyTargets());
		} else {
			if (finalValue == null || !setValue(targetIndex, finalValue)) return;
			stats.onPushEvaluated(currentNode, finalValue);
			
			runner.setPushValueOf(targetIndex, node.getPushTargets(), finalValue);
		}
	}

	void evaluateDirectPush(StiXpressionNode.PushTarget[] targets, Object pushedValue)
	{
		if (pushedValue == null || targets.length == 0) return;
		
		int callingNode = currentIndex;
		try {
			for (StiXpressionNode.PushTarget target : targets) {
				int index = target.getXtractorIndex();
				if (hasPublicValue(index)) continue;
				if (runner.executePostponedTargetsBefore(nodes[index])) return;
				evaluatePush0(index, target.getXtractorParam(), pushedValue);
				if (hasFrameFinalState()) break;
			}
			runner.executeImmediateTargets();
		} finally {
			setCurrentIndex(callingNode);
		}
	}
	
	void evaluateDirectPush(StiXpressionNode.PushTarget target, Object pushedValue)
	{
		if (pushedValue == null) return;
		
		int callingNode = currentIndex;
		try {
			int index = target.getXtractorIndex();
			if (hasPublicValue(index)) return;
			if (runner.executePostponedTargetsBefore(nodes[index])) return;
			evaluatePush0(index, target.getXtractorParam(), pushedValue);
			runner.executeImmediateTargets();		
		} finally {
			setCurrentIndex(callingNode);
		}
	}
}
