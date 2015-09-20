package org.cybcode.stix.core.compiler;

public interface StiXpressionFlattenContext
{
	void addParamCount(int count);
	int addNode(RegularParserSlot slot);
	void addLinkCount(int count);
	void finished();
}