package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXtractor.Parameter;

abstract class XecutorContextRunnerFramedNode extends XecutorContextRunnerNode
{
	protected final RunnerFrame frame;

	public XecutorContextRunnerFramedNode(XecutorContextNode contextNode, RunnerFrame frame)
	{
		super(contextNode);
		this.frame = frame;
	}

	public void init(Object rootValue)
	{
		int startIndex = frame.getStartIndex(); 
		int endIndex = frame.getEndIndex();
		storage.clearRange(startIndex, endIndex, null);
		setFinalValue(rootValue);
	}

	public RunnerFrame getFrame()
	{
		return frame;
	};
	
	@Override public StiXpressionSequencer getSequencer()
	{
		return frame.getSequencer();
	}

	@Override public boolean hasFrameFinalState()
	{
		return frame.hasFinalState();
	}
}

class XecutorContextRunnerResultNode extends XecutorContextRunnerStatelessNode
{
	protected final StiXecutor initialState;

	public XecutorContextRunnerResultNode(XecutorContextNode contextNode, RunnerFrame frame, StiXecutor initialState)
	{
		super(contextNode, frame);
		this.initialState = initialState;
	}
	
	protected StiXecutor getXecutor()
	{
		StiXecutor state = storage.getState(index);
		if (state != null) return state;
		return initialState;
	}
	
	@Override protected Object internalEvaluateFinal()
	{
		if (initialState == null) return super.internalEvaluateFinal();
		return getXecutor().evaluateFinal(this);
	}
	
	@Override public void setFinalState()
	{
		super.setFinalState();
		frame.resetFrame(true); //this will only work properly for the outermost frame
	}
	
	@Override protected Object internalEvaluatePush(Parameter<?> targetParam, Object pushedValue)
	{
		if (initialState == null) return super.internalEvaluatePush(targetParam, pushedValue);
		return getXecutor().evaluatePush(this, targetParam, pushedValue);
	}

	@Override public StiXecutor getInitialState()
	{
		return initialState;
	}

	@Override public void setNextState(StiXecutor xecutor)
	{
		if (initialState == null) throw new IllegalStateException();
		storage.setState(index, xecutor);
	}
}