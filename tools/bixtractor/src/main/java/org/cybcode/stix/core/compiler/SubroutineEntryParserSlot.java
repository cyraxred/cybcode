package org.cybcode.stix.core.compiler;

import java.util.Map;

import org.cybcode.stix.ops.StiX_Subroutine;

public class SubroutineEntryParserSlot extends RegularParserSlot
{
//	private final List<StiXpressionParserSubroutineSlot> inners;
	private final SubroutineParserSlot subroutineSlot;

	public SubroutineEntryParserSlot(StiX_Subroutine<?, ?>.EntryPoint node, SubroutineParserSlot nextToResult, ParserContext parserContext)
	{
		super(node, nextToResult, parserContext);
		this.subroutineSlot = nextToResult;
//		this.inners = new ArrayList<>();
	}
	
	@Override public FrameOwnerSlot getParamFrameOwner()
	{
		return subroutineSlot.getOuterFrameOwner();
	}

	@Override protected FrameOwnerSlot findActualFrameOwner()
	{
		return subroutineSlot;
	}
	
	@Override protected void registerOuterDependencies() {}

	void addOuterDependencies(Map<Integer, SlotLink> outerDependencies)
	{
		for (int i = size() - 1; i >= 0; i--) {
			outerDependencies.remove(get(i).target.getParsedIndex());
		}
		for (SlotLink link : outerDependencies.values()) {
			add(link);
		}
	}

	public SubroutineParserSlot getSubroutineSlot()
	{
		return subroutineSlot;
	}
}