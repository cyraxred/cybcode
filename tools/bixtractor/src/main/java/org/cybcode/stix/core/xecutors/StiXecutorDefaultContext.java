package org.cybcode.stix.core.xecutors;

import java.util.Arrays;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;

public class StiXecutorDefaultContext implements StiXecutorContext
{
	private final StiXpressionNode[] nodes;
	private StiXpressionSequencer sequencer;
	private StiXecutor[] currentState;
	private Object[] results;
	private int currentIndex;
	private StiXpressionNode currentNode;
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
			context.sequencer.addPushTargets(currentNode.getNotifyTargets());
		}
	}
	
	public StiXecutorDefaultContext(StiXpressionNode[] initialState)
	{
		if (initialState[0].getXecutor() != DefaultXecutors.FINAL) throw new IllegalArgumentException("Root must be present as index 0");
		this.nodes = initialState;
		this.currentFrame = new Frame(0, initialState.length - 1, null);
	}
	
	private void resetContext(Object rootValue, StiXpressionSequencer sequencer)
	{
		if (rootValue == null) throw new NullPointerException();
		if (sequencer == null) throw new NullPointerException();
		
		sequencer.resetSequencer();
		this.sequencer = sequencer;
		
		if (this.currentState == null) {
			this.currentState = new StiXecutor[nodes.length];
			this.results = new Object[nodes.length];
		} else {
			while (currentFrame.outerFrame != null) {
				currentFrame = currentFrame.outerFrame;
			}
		}
		currentFrame.resetFrameContext(this, rootValue);
		currentIndex = 1;
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

//	private boolean hasFinalValue(int xtractorIndex)
//	{
//		return currentState[xtractorIndex] == DefaultXecutors.FINAL;
//	}
	
	private void setValue(int xtractorIndex, Object value)
	{
		results[xtractorIndex] = value;
		if (value != null) {
			currentFrame.setFinalIfResult(xtractorIndex);
		}
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
			currentState[index] = currentNode.getXecutor();
		}
		setValue(index, value);
	}
	
	@Override public Object getInterimValue()
	{
		int index = currentIndex();
		if (hasPublicValue(index)) throw new IllegalStateException();
		return getValue(index);
	}
	
	@Override public Object getAndResetInterimValue()
	{
		int index = currentIndex();
		if (hasPublicValue(index)) throw new IllegalStateException();
		Object result = getValue(index);
		if (result == null) return null;

		setValue(index, null);
		return result;
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
		
		result = currentNode.getXecutor();
		currentState[index] = result;
		return result;
	}
	
	private boolean evaluateCurrent()
	{
		int index = currentIndex();
		StiXpressionNode node = currentNode;
		if (node == null || hasPublicValue(index)) return false;
		
		StiXtractor<?> xtractor = getCurrentXtractor();
		Object result = xtractor.evaluate(this);
		setValue(index, result);
		currentState[index] = DefaultXecutors.FINAL;
		if (result == null) return false;
		
		sequencer.addPushTargets(node.getPushTargets());
		sequencer.addNotificationTargets(node.getNotifyTargets());
		return true;
	}

	private boolean evaluatePush(StiXpressionNode.PushTarget target)
	{
		Object pushedValue = getValue(target.getValueIndex());
		return evaluatePush(target, pushedValue);
	}
	
	private boolean evaluatePush(StiXpressionNode.PushTarget target, Object pushedValue)
	{
		int index = target.getXtractorIndex();
		StiXpressionNode node = currentNode;
		
		if (node == null || hasPublicValue(index)) return false;
		StiXecutor xecutor = getXecutor(index);

		setCurrentIndex(index);
		xecutor = xecutor.push(this, target.getXtractorParam(), pushedValue);
		if (xecutor == null) throw new NullPointerException();
		currentState[index] = xecutor;

		if (!xecutor.canBeEvaluated()) return true;
		
		StiXtractor<?> xtractor = getCurrentXtractor();
		Object result = xtractor.evaluate(this);
		setValue(index, result);
		if (result == null) return false;
		
		sequencer.addPushTargets(node.getPushTargets());
		if (xecutor == DefaultXecutors.FINAL) {
			sequencer.addNotificationTargets(node.getNotifyTargets());
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
		currentIndex = index;
		currentNode = nodes[index];
	}

	private void setCurrentNode(StiXpressionNode node)
	{
		int index = node.getIndex();
		if (nodes[index] != null) throw new IllegalArgumentException();
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
			} while (nextNode() < nodes.length);
		}
		
		return getValue(nodes.length - 1);
	}

	private int nextNode()
	{
		if (currentIndex == currentFrame.resultIndex && currentFrame.outerFrame != null) {
			currentFrame = currentFrame.close();
		}
		return ++currentIndex;
	}
}
