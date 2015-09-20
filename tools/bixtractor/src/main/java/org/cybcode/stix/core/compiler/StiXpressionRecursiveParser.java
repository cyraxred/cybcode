package org.cybcode.stix.core.compiler;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutorContextBuilder;
import org.cybcode.stix.api.StiXtractor;

public class StiXpressionRecursiveParser 
{
	private final boolean deduplicate;
	private StiXpressionParserSlot rootSlot;
	private StiXpressionParserSlot resultSlot;
//	private int nodesCount;
	private boolean	linked;
	private StiXComplexityHelper complexityHelper;
	
	public StiXpressionRecursiveParser() 
	{
		this(true);
	}
	
	public StiXpressionRecursiveParser(boolean deduplicate) 
	{
		this.deduplicate = deduplicate;
		setComplexityHelper(null);
	}
	
	public void step1_buildTree(StiXpressionParserSlot.ParserContext context, StiXtractor<?> resultNode)
	{
		if (resultSlot != null) throw new IllegalStateException();

		rootSlot = context.getRoot();
		resultSlot = StiXpressionParserSlot.parse(context, resultNode);
//		nodesCount = context.getNodeCount();
	}
	
	public void step1_buildTree(StiXtractor<?> resultNode)
	{
		step1_buildTree(StiXpressionParserSlot.createDefaultContext(256, deduplicate, complexityHelper), resultNode);
	}
	
	public void step2_optimizeTree()
	{
		if (resultSlot == null) throw new IllegalStateException();
//		nodesCount = 0;
		//collapses nodes by node properties
	}
	
	public void step3_linkTree()
	{
		if (resultSlot == null || linked) throw new IllegalStateException();
		linked = true;
		resultSlot.link();
	}

	public void step4_optimizeLinkedTree()
	{
		if (resultSlot == null || !linked) throw new IllegalStateException();
//		nodesCount = 0;
		//collapses nodes by node linkage properties
	}

	public void step5_flattenTree(StiXpressionFlattenContext context)
	{
		if (resultSlot == null || !linked) throw new IllegalStateException();
		if (context.addNode(rootSlot) != 0) throw new IllegalStateException(); //TODO have special method
		resultSlot.flatten(context);
		context.finished();
	}

	public void step5_flattenTree(StiXecutorContextBuilder builder)
	{
		StiXpressionFlattenToXecutorContextBuilder flattener = new StiXpressionFlattenToXecutorContextBuilder(builder);
		step5_flattenTree(flattener);
	}

	public void setComplexityHelper(StiXComplexityHelper complexityHelper)
	{
		this.complexityHelper = complexityHelper == null ? StiXComplexityHelper.DEFAULT : complexityHelper;
	}
}

class StiXpressionFlattenToXecutorContextBuilder implements StiXpressionFlattenContext
{
	private final StiXecutorContextBuilder builder;
	private StiXpressionParserSlot prev;
	private int nodeCount;

	public StiXpressionFlattenToXecutorContextBuilder(StiXecutorContextBuilder builder)
	{
		this.builder = builder;
		builder.setCapacities(-1, -1, -1);
	}

	@Override public void addParamCount(int count)
	{
	}

	@Override public void addLinkCount(int count)
	{
	}
	
	@Override public int addNode(StiXpressionParserSlot slot)
	{
		if (prev != null) {
			builder.addNode(prev);
		}
		prev = slot;
		return nodeCount++;
	}
	
	@Override public void finished()
	{
		if (prev != null) {
			builder.addNode(prev);
			prev = null;
		}
		builder.processNodeTargets();
	}
}