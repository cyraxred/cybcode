package org.cybcode.stix.core.compiler;

import java.util.HashMap;
import java.util.Map;

import org.cybcode.stix.ops.StiX_Subroutine;

public class SubroutineEntryParserSlot extends RegularParserSlot
{
//	private final List<StiXpressionParserSubroutineSlot> inners;
	private final SubroutineParserSlot subroutineSlot;
	private final Map<Integer, SlotLink> outerDependencies = new HashMap<>();

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
		this.outerDependencies.putAll(outerDependencies);
	}
	
	@Override protected void beforeLink()
	{
		super.beforeLink();
		if (outerDependencies.isEmpty()) return;
		SlotLink source = get(0);
		clear();
		outerDependencies.remove(source.target.getParsedIndex());
		for (SlotLink link : outerDependencies.values()) {
			add(link);
		}
		outerDependencies.clear();
		add(source);
	}

	public SubroutineParserSlot getSubroutineSlot()
	{
		return subroutineSlot;
	}

	@Override public int getXtractorFrameOwnerIndex()
	{
		return subroutineSlot.getOuterFrameOwner().getXtractorFrameOwnerIndex();
	}
}