package org.cybcode.stix.core.compiler;

public interface StiXpressionFlattenContext
{
	void addParamCount(int count);
	int addNode(StiXpressionParserSlot slot);
	void addLinkCount(int count);
}