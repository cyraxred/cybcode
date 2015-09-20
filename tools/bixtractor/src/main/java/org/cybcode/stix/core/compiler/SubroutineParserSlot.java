package org.cybcode.stix.core.compiler;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.ops.StiX_Subroutine;
import org.cybcode.stix.ops.StiX_SubroutineRoot;

public class SubroutineParserSlot extends FrameOwnerSlot
{
	private SubroutineEntryParserSlot entrySlot;
//	private Parameter<?> postponedParam;
	
	public SubroutineParserSlot(StiX_Subroutine<?, ?> node, RegularParserSlot nextToResult, ParserContext parserContext)
	{
		super(node, nextToResult, parserContext);
	}
	
	@Override public void visitParameter(Parameter<?> param)
	{
		switch (this.size()) {
			case 0: {
				if (entrySlot != null) throw new IllegalStateException();
				super.visitParameter(param);
				//Late capture here allows deduplication to operate properly
				entrySlot = (SubroutineEntryParserSlot) get(0).target;
				break;
			}
			case 1: {
				super.visitParameter(param);
				break;
			}
			default:
				throw new IllegalArgumentException();
		}
	}
	
	@Override protected RegularParserSlot createParamSlot(StiXtractor<?> paramSource, ParserContext parserContext)
	{
		if (paramSource instanceof StiX_Subroutine.EntryPoint) {
			//DO NOT capture here or deduplication may be broken
			return new SubroutineEntryParserSlot((StiX_Subroutine<?, ?>.EntryPoint) paramSource, this, parserContext);
		}
		return super.createParamSlot(paramSource, parserContext);
	}
	
	public RegularParserSlot replaceFrameSlot(RegularParserSlot operation, StiXtractor<?> parameter)
	{
		if (!(parameter instanceof StiX_SubroutineRoot)) return null;
		
		int frameLevelDelta = ((StiX_SubroutineRoot<?>) parameter).getSubroutineLevel();
		FrameOwnerSlot referredFrameOwner = getOuterFrame(getParamFrameOwner(), frameLevelDelta);
		if (referredFrameOwner instanceof SubroutineParserSlot) {
			return ((SubroutineParserSlot) referredFrameOwner).entrySlot;
		}
		throw new IllegalArgumentException("Invalid subroutine level: xtractor=" + parameter);
	}

	@Override protected void registerInnerSlot(FrameOwnerSlot innerSlot) {}
	
	@Override protected FrameOwnerSlot findActualFrameOwner()
	{
		return super.getOuterFrameOwner();
	}
}