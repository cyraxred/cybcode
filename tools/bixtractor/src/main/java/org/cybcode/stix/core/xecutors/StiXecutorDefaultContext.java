package org.cybcode.stix.core.xecutors;

import java.util.Arrays;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.ops.StiX_Root;

public class StiXecutorDefaultContext implements StiXecutorContext, StiXecutorPushContext
{
	private final StiXpressionNode[] nodes;

	//Late initialization
	private StiXecutor[] initialState;
	private StiXecutor[] currentState;
	private Object[] results;
	
	private StiXecutorStatsCollector stats;
	private StiXpressionSequencer sequencer;
	private StiXpressionNode currentNode;
	private int currentIndex;
	private ContextFrame currentFrame;
	
	public StiXecutorDefaultContext(StiXpressionNode[] nodes)
	{
		setStatsCollector(null);
		if (!(nodes[0].getXtractor() instanceof StiX_Root)) throw new IllegalArgumentException("Root must be present as index 0");
		this.nodes = nodes;
		this.currentFrame = new ContextFrame(0, nodes.length - 1, null);
	}
	
	private void resetContext(Object rootValue, StiXpressionSequencer sequencer)
	{
		if (rootValue == null) throw new NullPointerException();
		if (sequencer == null) throw new NullPointerException();

		stats.resetStats(nodes.length);
		sequencer.resetSequencer();
		this.sequencer = sequencer;
		
		if (currentState == null) {
			initialState = new StiXecutor[nodes.length];
			currentState = new StiXecutor[nodes.length];
			results = new Object[nodes.length];
			createInitialState();
		} else {
			currentFrame = currentFrame.closeAllFrames();
		}
		resetFrameContent();
		
		currentIndex = 0;
		currentNode = nodes[0];
		setFinalValue(0, rootValue);
		sequencer.addPostponeTargets(currentNode.getNotifyTargets());
		
		if (nodes.length > 1) {
			setCurrentIndex(1);
		}
		stats.onEvaluated();
	}

	private void resetFrameContent()
	{
		int rootIndex = currentFrame.getRootIndex();
		int resultIndex = currentFrame.getResultIndex();
		if (currentState[rootIndex] != null) {
			Arrays.fill(currentState, rootIndex, resultIndex + 1, null);
			Arrays.fill(results, rootIndex, resultIndex + 1, null);
		}
	}

	private void createInitialState()
	{
		XecutorConstructionContext context = new XecutorConstructionContext(nodes.length - 1);
		for (int i = nodes.length - 1; i >= 0; i--) { //MUST BE in reversed order, otherwise StiXourceXecutor will fail to initialize due to missing dependencies 
			StiXpressionNode node = nodes[i];
			context.setNode(node);
			StiXecutor xecutor = node.createXecutor(context);
//			if (xecutor == null) throw new NullPointerException("Xecutor can't be null, xtractor=" + node.getXtractor());
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

	@Override public boolean hasResultValue()
	{
		return currentFrame.hasResult();
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
	
	void enterFrame()
	{
		currentFrame = currentFrame.createInner(currentIndex, currentNode.getFrameResultIndex());
		resetFrameContent();
	}
	
	private void exitFrame()
	{
		if (!currentFrame.isInnerFrame()) return; //don't need for the outermost frame
		int startIndex = currentFrame.getRootIndex();
		currentFrame = currentFrame.closeFrame();
		currentState[startIndex] = initialState[startIndex]; //clears FINAL to enable frame re-enter and not null to enable frame cleanup
	}
	
	private void evaluateCurrent()
	{
		int index = currentIndex();
		StiXpressionNode node = currentNode;
		if (node == null) return;

		StiXecutor xecutor = currentState[index];
		if (xecutor == DefaultXecutors.FINAL) return;
		
		Object finalValue;
		if (xecutor != null) {
			finalValue = xecutor.evaluateFinal(this);
		} else {
			finalValue = getCurrentXtractor().apply(this);
		}
		currentState[index] = DefaultXecutors.FINAL;
		stats.onEvaluated();

		if (!setValue(index, finalValue) || finalValue == null) return;
		
		sequencer.addPostponeTargets(node.getNotifyTargets());
	}
	
	private void setFinalValue(int xtractorIndex, Object value)
	{
		setValue(xtractorIndex, value);
		currentState[xtractorIndex] = DefaultXecutors.FINAL;
		if (value != null) {
			currentFrame.setFinalIfResult(xtractorIndex);
		}
	}

	private boolean evaluatePush(StiXpressionNode.PushTarget target)
	{
		int index = target.getXtractorIndex();
		if (hasPublicValue(index)) return false;
		
		Object pushedValue = getValue(target.getValueIndex());
		if (pushedValue == null) return false;
		
		evaluatePush(false, index, target.getXtractorParam(), pushedValue);
		return true;
	}
	
	void evaluateDirectPush(StiXpressionNode.PushTarget[] targets, Object pushedValue)
	{
		if (pushedValue == null || targets.length == 0) return;
		
		StiXpressionNode callingNode = currentNode;
		try {
			for (StiXpressionNode.PushTarget target : targets) {
				int index = target.getXtractorIndex();
				if (hasPublicValue(index)) continue;
				if (executePostponedTargetsBefore(nodes[index])) return;
				
				evaluatePush(true, index, target.getXtractorParam(), pushedValue);
				if (hasResultValue()) break;
			}
			executeImmediateTargets();
		} finally {
			setCurrentNode(callingNode);
		}
	}
	
	void evaluateDirectPush(StiXpressionNode.PushTarget target, Object pushedValue)
	{
		if (pushedValue == null) return;
		
		StiXpressionNode callingNode = currentNode;
		try {
			int index = target.getXtractorIndex();
			if (hasPublicValue(index)) return;
			if (executePostponedTargetsBefore(nodes[index])) return;

			evaluatePush(true, index, target.getXtractorParam(), pushedValue);
			if (hasResultValue()) return;
	
			executeImmediateTargets();		
		} finally {
			setCurrentNode(callingNode);
		}
	}
	
	private void evaluatePush(boolean immediate, int targetIndex, Parameter<?> targetParam, Object pushedValue)
	{
		setCurrentIndex(targetIndex);
		stats.onPushAttempt();
//		if (currentNode == null) return; //TODO do we need it?

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
		} else if (xecutor == DefaultXecutors.FINAL) {
			return;
		} else {
			finalValue = xecutor.evaluatePush(this, targetParam, pushedValue);
		}
		
		if (currentState[targetIndex] == DefaultXecutors.FINAL) {
			if (!setValue(targetIndex, finalValue) || finalValue == null) return;
			stats.onPushEvaluated();
			
			currentFrame.setFinalIfResult(targetIndex);
			sequencer.addPostponeTargets(currentNode.getNotifyTargets());  
		} else {
			if (finalValue == null || !setValue(targetIndex, finalValue)) return;
			stats.onPushEvaluated();
			sequencer.addImmediateTargets(currentNode.getPushTargets());
		}
	}
	
	private int currentIndex()
	{
		return currentIndex;
	}
	
	private void setCurrentIndex(int index)
	{
		if (index < 0 || index >= nodes.length) throw new IllegalArgumentException();
		if (currentIndex == index) return;
		currentIndex = index;
		currentNode = nodes[index];
	}

	private void setCurrentNode(StiXpressionNode node)
	{
		if (currentNode == node) return;
		int index = node.getIndex();
		if (nodes[index] != node) throw new IllegalArgumentException();
		currentNode = node;
		currentIndex = index;
	}
	
	private int mapParamIndex(int paramIndex)
	{
		return currentNode.mapParamIndex(paramIndex);
	}
	
	@Override public StiXtractor<?> getCurrentXtractor()
	{
		return currentNode.getXtractor();
	}

	private boolean executeImmediateTargets()
	{
		StiXpressionNode.PushTarget pushTarget;
		while ((pushTarget = sequencer.nextImmediateTarget()) != null) {
			evaluatePush(pushTarget);
			if (hasResultValue()) return true;
		}
		return false;
	}
	
	private boolean executePostponedTargetsBefore(StiXpressionNode nextNode)
	{
		StiXpressionNode.PushTarget pushTarget;
		while ((pushTarget = sequencer.nextPostponedTargetBefore(nextNode)) != null) {
			evaluatePush(pushTarget);
			if (hasResultValue()) return true;
		}
		return false;
	}

	//executeNode
	
	public Object evaluateExpression(StiXpressionSequencer sequencer, Object rootValue)
	{
		resetContext(rootValue, sequencer);
		if (currentIndex < nodes.length) {
			do {
				StiXpressionNode nextNode = currentNode;
				if (nextNode == null) continue;
				
				if (executePostponedTargetsBefore(nextNode)) break;
				
				setCurrentNode(nextNode);
				evaluateCurrent();
				if (!hasResultValue()) continue;
				setCurrentIndex(currentFrame.getResultIndex());
			} while (nextNode());
		}
		
		return getValue(nodes.length - 1);
	}

	private boolean nextNode()
	{
		if (currentFrame.isEndOfFrame(currentIndex)) {
			exitFrame();
		}
		int nextIndex = currentIndex + 1;
		if (nextIndex >= nodes.length) return false;
		setCurrentIndex(nextIndex);
		return true;
	}

	public void setStatsCollector(StiXecutorStatsCollector stats)
	{
		this.stats = stats == null ? StiXecutorStatsCollector.NULL : stats;
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
}
