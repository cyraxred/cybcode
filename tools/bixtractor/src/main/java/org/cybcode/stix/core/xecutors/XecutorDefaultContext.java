package org.cybcode.stix.core.xecutors;

import java.util.Arrays;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.ops.StiX_Root;

public class XecutorDefaultContext implements StiXecutorContext, StiXecutorPushContext
{
	private final StiXpressionNode[] nodes;

	//Late initialization
	private StiXecutor[] initialState;
	private StiXecutor[] currentState;
	private Object[] results;
	
	private StiXecutorStatsCollector stats;
	private StiXpressionNode currentNode;
	private int currentIndex;
	private ContextFrame currentFrame;
	
	public XecutorDefaultContext(StiXpressionNode[] nodes)
	{
		setStatsCollector(null);
		if (!(nodes[0].getXtractor() instanceof StiX_Root)) throw new IllegalArgumentException("Root must be present as index 0");
		this.nodes = nodes;
		this.currentFrame = new ContextFrame(0, nodes.length - 1, null);
	}
	
	void resetContext(Object rootValue, StiXpressionSequencer sequencer)
	{
		if (rootValue == null) throw new NullPointerException();
		if (sequencer == null) throw new NullPointerException();

		stats.resetStats(nodes.length);
		sequencer.resetSequencer();
		
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
	
	void exitFrame()
	{
		if (!currentFrame.isInnerFrame()) return; //don't need for the outermost frame
		int startIndex = currentFrame.getRootIndex();
		currentFrame = currentFrame.closeFrame();
		currentState[startIndex] = initialState[startIndex]; //clears FINAL to enable frame re-enter and not null to enable frame cleanup
	}
	
	private void setFinalValue(int xtractorIndex, Object value)
	{
		setValue(xtractorIndex, value);
		currentState[xtractorIndex] = DefaultXecutors.FINAL;
		if (value != null) {
			currentFrame.setFinalIfResult(xtractorIndex);
		}
	}

	private int currentIndex()
	{
		return currentIndex;
	}
	
	void setCurrentIndex(int index)
	{
		if (index < 0 || index >= nodes.length) throw new IllegalArgumentException();
		if (currentIndex == index) return;
		currentIndex = index;
		currentNode = nodes[index];
	}

	void setCurrentNode(StiXpressionNode node)
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

	public void setStatsCollector(StiXecutorStatsCollector stats)
	{
		this.stats = stats == null ? StiXecutorStatsCollector.NULL : stats;
	}
	
	public StiXecutorStatsCollector getStatsCollector()
	{
		return stats;
	}
	
	public int getNodeCount()
	{
		return nodes.length;
	}

	private StiXecutor nextXecutor;
	
	@Override public void setNextState(StiXecutor xecutor)
	{
		if (xecutor == null) throw new NullPointerException();
		nextXecutor = xecutor;
	}

	@Override public void setFinalState()
	{
		nextXecutor = DefaultXecutors.FINAL;
	}
	
	StiXecutor nextXecutor()
	{
		StiXecutor result = nextXecutor;
		nextXecutor = null;
		return result;
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