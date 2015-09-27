package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutor;

class XecutorRunnerResultNode extends XecutorRunnerFramedNode
{
	public XecutorRunnerResultNode(XecutorContextNode contextNode, XecutorRunnerFrame frame, StiXecutor initialState)
	{
		super(contextNode, frame, initialState);
	}
	
	@Override public void setFinalState()
	{
		super.setFinalState();
		frame.resetFrame(true); //this will only work properly for the outermost frame
	}
}