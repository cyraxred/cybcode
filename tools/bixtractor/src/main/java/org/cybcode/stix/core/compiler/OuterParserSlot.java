package org.cybcode.stix.core.compiler;

import org.cybcode.stix.api.StiXtractor;

public class OuterParserSlot extends FrameOwnerSlot
{
	protected OuterParserSlot(ParserContext parserContext)
	{
		super(parserContext);
	}

	@Override public RegularParserSlot replaceFrameSlot(RegularParserSlot operation, StiXtractor<?> operationParameter)
	{
		return null;
	}

	@Override public boolean isOuterFrame(FrameOwnerSlot frameOwner)
	{
		return false;
	}

	@Override protected void registerInnerSlot(FrameOwnerSlot innerSlot)
	{
	}
	
	@Override protected FrameOwnerSlot findActualFrameOwner()
	{
		return this;
	}
}