package org.cybcode.stix.core.xecutors;

import java.util.Arrays;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.ops.StiX_Root;

public class StiXecutorDefaultContext implements StiXecutorContext
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
		sequencer.addPostponeTargets(currentNode.getPushTargets());
		sequencer.addPostponeTargets(currentNode.getNotifyTargets());
		
		if (nodes.length > 1) {
			setCurrentIndex(1);
		}
		stats.onEvaluate();
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
			if (xecutor == null) throw new NullPointerException("Xecutor can't be null, xtractor=" + node.getXtractor());
			initialState[i] = xecutor; 
		}
	}
	
	private boolean hasPublicValue(int xtractorIndex)
	{
		StiXecutor state = currentState[xtractorIndex];
		return state == DefaultXecutors.FINAL;
	}
	
	private boolean hasPrivateValue(int xtractorIndex)
	{
		StiXecutor state = currentState[xtractorIndex];
		return state != null && state != DefaultXecutors.FINAL;
	}

	private void setValue(int xtractorIndex, Object value)
	{
		results[xtractorIndex] = value;
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

	@Override public boolean hasParamValue(int paramIndex)
	{
		int index = mapParamIndex(paramIndex);
		return hasPublicValue(index) && hasValue(index);
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
		if (currentState[index] == null) {
			currentState[index] = initialState[index];
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
		return hasPrivateValue(index) && hasValue(index);
	}
	
	private StiXecutor getXecutor(int index)
	{
		StiXecutor result = currentState[index];
		if (result != null) return result;
		
		result = initialState[index];
		currentState[index] = result;
		return result;
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
	
	private boolean evaluateCurrent()
	{
		int index = currentIndex();
		StiXpressionNode node = currentNode;
		if (node == null) return false;

		StiXecutor xecutor = currentState[index];
		if (xecutor != null && xecutor.isPushOrFinal()) {
			if (xecutor == DefaultXecutors.FINAL) return false;
			setFinalValue(index, null);
			return false;
		} 

		stats.onEvaluate();
		StiXtractor<?> xtractor = getCurrentXtractor();
		Object result = xtractor.evaluate(this);
		setFinalValue(index, result);
		if (result == null) return false;
		
		sequencer.addPostponeTargets(node.getPushTargets());
		sequencer.addPostponeTargets(node.getNotifyTargets());
		return true;
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
		StiXecutor xecutor = getXecutor(targetIndex);

		stats.onPushAttempt();
		xecutor = xecutor.push(this, targetParam, pushedValue);
		if (xecutor == null) throw new NullPointerException();

		if (xecutor == DefaultXecutors.FINAL) {
			currentState[targetIndex] = DefaultXecutors.PUSH_FINAL; 
		} else {
			currentState[targetIndex] = xecutor;
			if (!xecutor.isPushOrFinal()) return;
		}
		
		stats.onPushEvaluate();
		StiXtractor<?> xtractor = getCurrentXtractor();
		Object result = xtractor.evaluate(this);
		
		if (xecutor == DefaultXecutors.FINAL) { //replace PUSH_FINAL
			setFinalValue(targetIndex, result);
		} else {
			setValue(targetIndex, result);
		}
		if (result == null) return;
		
		if (xecutor == DefaultXecutors.FINAL) {
			sequencer.addPostponeTargets(currentNode.getPushTargets());
			sequencer.addPostponeTargets(currentNode.getNotifyTargets());
		} else if (immediate) {
			sequencer.addImmediateTargets(currentNode.getPushTargets());
		} else {
			sequencer.addPostponeTargets(currentNode.getPushTargets());
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
}
