package org.cybcode.stix.core.xecutors;

import java.util.Arrays;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXpressionContext;
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
		resetFrameContext(rootValue);
		if (nodes.length == 1) {
			currentFrame.setFinalIfResult(0);
		} else {
			setCurrentIndex(1);
		}
	}

	private void resetFrameContext(Object rootValue)
	{
		int rootIndex = currentFrame.getRootIndex();
		int resultIndex = currentFrame.getResultIndex();
		if (rootIndex < 0) throw new IllegalStateException();
		if (currentState[rootIndex] != null) {
			Arrays.fill(currentState, rootIndex, resultIndex + 1, null);
			Arrays.fill(results, rootIndex, resultIndex + 1, null);
		}
		currentNode = nodes[rootIndex];
		setValue(rootIndex, rootValue);
		currentState[rootIndex] = DefaultXecutors.FINAL;

		sequencer.addPushTargets(currentNode.getPushTargets());
		sequencer.addNotifyTargets(currentNode.getNotifyTargets());
	}
	
	private void createInitialState()
	{
		StiXpressionContext context = new StiXpressionDefaultContext();
		for (int i = nodes.length - 1; i >= 0; i--) {
			StiXecutor xecutor = nodes[i].createXecutor(context);
			if (xecutor == null) throw new NullPointerException();
			initialState[i] = xecutor; 
		}
	}
	
	private boolean hasPublicValue(int xtractorIndex)
	{
		StiXecutor state = currentState[xtractorIndex];
		return state == DefaultXecutors.FINAL;
	}
	
	private boolean isPushOrFinal(int xtractorIndex)
	{
		StiXecutor xecutor = currentState[xtractorIndex];
		return xecutor != null && xecutor.isPushOrFinal();
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
		if (hasPublicValue(index)) throw new IllegalStateException();
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
	
	private boolean evaluateCurrent()
	{
		int index = currentIndex();
		StiXpressionNode node = currentNode;
		if (node == null) return false;

		if (isPushOrFinal(index)) {
			if (hasPublicValue(index)) return false;
			setFinalValue(index, null);
			return false;
		} 

		stats.onEvaluate();
		StiXtractor<?> xtractor = getCurrentXtractor();
		Object result = xtractor.evaluate(this);
		setFinalValue(index, result);
		if (result == null) return false;
		
		sequencer.addPushTargets(node.getPushTargets());
		sequencer.addNotifyTargets(node.getNotifyTargets());
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
		evaluatePush(index, target.getXtractorParam(), pushedValue);
		return true;
	}
	
	private void evaluatePush(int targetIndex, Parameter<?> targetParam, Object pushedValue)
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
		
		sequencer.addPushTargets(currentNode.getPushTargets());
		if (xecutor == DefaultXecutors.FINAL) {
			sequencer.addNotifyTargets(currentNode.getNotifyTargets());
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

	private boolean executeBefore(StiXpressionNode nextNode)
	{
		StiXpressionNode.PushTarget pushTarget;
		boolean stateWasChanged = false;
		while ((pushTarget = sequencer.nextTargetBefore(nextNode)) != null) {
			if (evaluatePush(pushTarget)) stateWasChanged = true;
		}
		return stateWasChanged;
	}

	//executeNode
	
	public Object evaluateExpression(StiXpressionSequencer sequencer, Object rootValue)
	{
		resetContext(rootValue, sequencer);
		if (currentIndex < nodes.length) {
			do {
				StiXpressionNode nextNode = currentNode;
				if (nextNode == null) continue;
				
				//boolean stateWasChanged = 
				executeBefore(nextNode);
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
		if (currentFrame.isEndOfFrame(currentIndex) && currentFrame.isInnerFrame()) {
			currentFrame = currentFrame.closeFrame();
		}
		int nextIndex = currentIndex + 1;
		if (nextIndex >= nodes.length) return false;
		setCurrentIndex(nextIndex);
		return true;
	}

	public void setStatsCollector(StiXecutorStatsCollector stats)
	{
		this.stats = stats == null ? StiXecutorStatsCollector.getInstance() : stats;
	}
}

