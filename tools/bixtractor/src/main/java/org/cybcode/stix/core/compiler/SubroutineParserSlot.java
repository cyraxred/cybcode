package org.cybcode.stix.core.compiler;

import java.util.HashMap;
import java.util.Map;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.ops.StiX_Subroutine;
import org.cybcode.stix.ops.StiX_SubroutineRoot;

public class SubroutineParserSlot extends FrameOwnerSlot
{
	private SubroutineEntryParserSlot entrySlot;
	private Map<Integer, SlotLink> outerDependencies;
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

	@Override protected FrameOwnerSlot findActualFrameOwner()
	{
		return super.getOuterFrameOwner();
	}
	
	@Override protected void registerOuterDependency(SlotLink paramLink)
	{
		try {
			paramLink.parameter.disableNotify();
		} catch (IllegalStateException e) {
			throw new IllegalStateException("Push links are not allowed for cross-subroutine dependencies: from=" + this + ",  to=" + paramLink.target, e);
		}
		if (paramLink.target instanceof SubroutineEntryParserSlot) return; //it is an outer entry, so we are depended on it anyway 
		if (paramLink.target instanceof SubroutineParserSlot) return; //result comes directly from another subroutine? huh //TODO is this acceptable? 
		
		if (outerDependencies == null) {
			outerDependencies = new HashMap<>();
		}
		outerDependencies.put(paramLink.target.getParsedIndex(), paramLink);
	}
	
	@Override protected void registerOuterDependencies() 
	{
		SlotLink resultLink = get(1);
		RegularParserSlot resultSlot = resultLink.target;
		FrameOwnerSlot resultSlotFrame = resultSlot.getParamFrameOwner();
		if (resultSlotFrame == this || resultSlotFrame == entrySlot.getSubroutineSlot()) return;
		registerOuterDependency(resultLink);
	}
	
	@Override protected void afterParse()
	{
		super.afterParse();
		if (outerDependencies != null) {
			entrySlot.addOuterDependencies(outerDependencies);
		}
	}
	
	@Override public int getXtractorFrameOwnerIndex()
	{
		if (entrySlot == null) throw new IllegalStateException();
		return entrySlot.getXtractorIndex();
	}
}