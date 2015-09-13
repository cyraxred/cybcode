package org.cybcode.stix.core.xecutors;

import java.util.Arrays;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXpressionContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.ops.StiX_Root;

public class StiXecutorDefaultContext implements StiXecutorContext
{
	private final StiXpressionNode[] nodes;

	//Late initialization
	private StiXecutor[] initialState;
	private StiXecutor[] currentState;
	private Object[] results;
	
	private StiXpressionSequencer sequencer;
	private StiXpressionNode currentNode;
	private int currentIndex;
	private Frame currentFrame;
	
	private static class Frame
	{
		private final int rootIndex;
		private final int resultIndex;
		private final Frame outerFrame;
		private Frame innerFrame;
		private boolean hasResult;
		
		Frame(int rootIndex, int resultIndex, Frame outerFrame)
		{
			this.rootIndex = rootIndex;
			this.resultIndex = resultIndex;
			this.outerFrame = outerFrame;
		}
		
		public Frame createInner(int rootIndex, int resultIndex)
		{
			if (innerFrame != null) throw new IllegalStateException();
			if (rootIndex <= this.rootIndex || resultIndex >= this.resultIndex || rootIndex > resultIndex) throw new IllegalArgumentException();
			Frame result = new Frame(rootIndex, resultIndex, this);
			this.innerFrame = result;
			return result;
		}
		
		public Frame close()
		{
			Frame outer = this.outerFrame;
			if (outer == null || outer.innerFrame != this) throw new IllegalStateException();
			outer.innerFrame = null;
			return outer;
		}

		public void setFinalIfResult(int xtractorIndex)
		{
			if (xtractorIndex != resultIndex) return;
			hasResult = true;			
		}
		
		public void resetFrameContext(StiXecutorDefaultContext context, Object rootValue)
		{
			if (context.currentState[rootIndex] != null) {
				Arrays.fill(context.currentState, rootIndex, resultIndex + 1, null);
				Arrays.fill(context.results, rootIndex, resultIndex + 1, null);
			}
			StiXpressionNode currentNode = context.nodes[rootIndex];
			context.currentNode = currentNode;
			context.setValue(rootIndex, rootValue);
			context.currentState[rootIndex] = DefaultXecutors.FINAL;

			context.sequencer.addPushTargets(currentNode.getPushTargets());
			context.sequencer.addNotifyTargets(currentNode.getNotifyTargets());
		}
	}
	
	public StiXecutorDefaultContext(StiXpressionNode[] nodes)
	{
		if (!(nodes[0].getXtractor() instanceof StiX_Root)) throw new IllegalArgumentException("Root must be present as index 0");
		this.nodes = nodes;
		this.currentFrame = new Frame(0, nodes.length - 1, null);
	}
	
	private void resetContext(Object rootValue, StiXpressionSequencer sequencer)
	{
		if (rootValue == null) throw new NullPointerException();
		if (sequencer == null) throw new NullPointerException();
		
		sequencer.resetSequencer();
		this.sequencer = sequencer;
		
		if (currentState == null) {
			initialState = new StiXecutor[nodes.length];
			currentState = new StiXecutor[nodes.length];
			results = new Object[nodes.length];
			createInitialState();
		} else {
			while (currentFrame.outerFrame != null) {
				currentFrame = currentFrame.outerFrame;
			}
		}
		currentFrame.resetFrameContext(this, rootValue);
		if (nodes.length == 1) {
			currentFrame.setFinalIfResult(0);
		} else {
			setCurrentIndex(1);
		}
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

//	private boolean hasFinalValue(int xtractorIndex)
//	{
//		return currentState[xtractorIndex] == DefaultXecutors.FINAL;
//	}
	
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
		return currentFrame.hasResult;
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
		Object pushedValue = getValue(target.getValueIndex());
		return evaluatePush(target, pushedValue);
	}
	
	private boolean evaluatePush(StiXpressionNode.PushTarget target, Object pushedValue)
	{
		int index = target.getXtractorIndex();
		
		if (hasPublicValue(index)) return false;
		StiXecutor xecutor = getXecutor(index);

		setCurrentIndex(index);
		xecutor = xecutor.push(this, target.getXtractorParam(), pushedValue);
		if (xecutor == null) throw new NullPointerException();

		if (xecutor == DefaultXecutors.FINAL) {
			currentState[index] = DefaultXecutors.PUSH_FINAL; 
		} else {
			currentState[index] = xecutor;
			if (!xecutor.isPushOrFinal()) return true;
		}
		
		StiXtractor<?> xtractor = getCurrentXtractor();
		Object result = xtractor.evaluate(this);
		
		if (xecutor == DefaultXecutors.FINAL) { //replace PUSH_FINAL
			setFinalValue(index, result);
		} else {
			setValue(index, result);
		}
		if (result == null) return true;
		
		sequencer.addPushTargets(currentNode.getPushTargets());
		if (xecutor == DefaultXecutors.FINAL) {
			sequencer.addNotifyTargets(currentNode.getNotifyTargets());
		}
		return true;
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
				setCurrentIndex(currentFrame.resultIndex);				
			} while (nextNode());
		}
		
		return getValue(nodes.length - 1);
	}

	private boolean nextNode()
	{
		if (currentFrame.outerFrame != null && currentIndex == currentFrame.resultIndex) {
			currentFrame = currentFrame.close();
		}
		int nextIndex = currentIndex + 1;
		if (nextIndex >= nodes.length) return false;
		setCurrentIndex(nextIndex);
		return true;
	}
}
