package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXpressionSequencer;


class XecutorRunnerFramedNode extends XecutorRunnerNode
{
	protected final XecutorRunnerFrame frame;

	public XecutorRunnerFramedNode(XecutorContextNode contextNode, XecutorRunnerFrame frame, StiXecutor initialState)
	{
		super(contextNode, initialState);
		this.frame = frame;
	}

	public void init(Object rootValue)
	{
		int startIndex = frame.getStartIndex(); 
		int endIndex = frame.getEndIndex();
		storage.clearRange(startIndex, endIndex, null);
		setFinalValue(rootValue);
	}

	public XecutorRunnerFrame getFrame()
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