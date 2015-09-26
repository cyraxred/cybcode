package org.cybcode.stix.core.xecutors;

public interface StiXecutorContextInspector
{
	int getNodeCount();
	StiXpressionNode getNode(int xtractorIndex);
}