package org.cybcode.stix.core.compiler;

import org.cybcode.stix.api.StiXtractor;

public abstract class FrameOwnerSlot extends RegularParserSlot
{
	public final int frameLevel;

	public FrameOwnerSlot(StiXtractor<?> node, RegularParserSlot nextToResult, ParserContext parserContext)
	{
		super(node, nextToResult, parserContext);
		frameLevel = traversedFrameOwner.frameLevel + 1;
		parserContext.reportFrame(frameLevel);
	}

	protected FrameOwnerSlot(ParserContext context)
	{
		super(context);
		frameLevel = 0;
	}

	public FrameOwnerSlot selectInnerFrame(FrameOwnerSlot anotherFrameOwner)
	{
		if (anotherFrameOwner == this) return this;
		if (this.isOuterFrame(anotherFrameOwner)) return this;
		if (anotherFrameOwner.isOuterFrame(this)) return anotherFrameOwner;

		throw new IllegalStateException("Invalid cross-subroutine references: sub1=" + this + ", sub2=" + anotherFrameOwner);
	}

	protected FrameOwnerSlot(StiXtractor<?> node, ParserContext parserContext)
	{
		super(node, parserContext);
		frameLevel = 0;
	}
	
	public void verifyAlternativeFrameOwner(FrameOwnerSlot anotherFrameOwner)
	{
		if (anotherFrameOwner == this) return;
		if (anotherFrameOwner != null && anotherFrameOwner.isOuterFrame(this)) return;
		throw new IllegalStateException("Invalid cross-subroutine references: sub1=" + this + ", sub2=" + anotherFrameOwner);
	}

	public abstract RegularParserSlot replaceFrameSlot(RegularParserSlot operation, StiXtractor<?> operationParameter);

	@Override public FrameOwnerSlot getParamFrameOwner()
	{
		return this;
	}

	public boolean isOuterFrame(FrameOwnerSlot sub)
	{
		FrameOwnerSlot frameOwner = traversedFrameOwner;
		while (true) {
			if (frameOwner == sub) return true;
			FrameOwnerSlot outerFrameOwner = frameOwner.traversedFrameOwner;
			if (outerFrameOwner == frameOwner) return false;
			frameOwner = outerFrameOwner; 
		}
	}

	public static FrameOwnerSlot getOuterFrame(FrameOwnerSlot currentFrameOwner, int levelsUp)
	{
		while (levelsUp-- > 0) {
			FrameOwnerSlot outerFrameOwner = currentFrameOwner.traversedFrameOwner;
			if (outerFrameOwner == currentFrameOwner) return null;
			currentFrameOwner = outerFrameOwner;	
		}
		return currentFrameOwner;
	}

	protected abstract void registerInnerSlot(FrameOwnerSlot innerSlot);
}