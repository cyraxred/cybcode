package org.cybcode.stix.core.compiler;

import org.cybcode.stix.api.StiXtractor;

public class RootParserSlot extends RegularParserSlot
{
	protected RootParserSlot(StiXtractor<?> node, ParserContext parserContext)
	{
		super(node, parserContext);
	}
	
	@Override protected FrameOwnerSlot findActualFrameOwner()
	{
		return traversedFrameOwner;
	}
	
	@Override public int getXtractorFrameOwnerIndex()
	{
		return 0;
	}
}