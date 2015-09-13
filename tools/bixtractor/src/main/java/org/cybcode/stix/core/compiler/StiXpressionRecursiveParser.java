package org.cybcode.stix.core.compiler;

import org.cybcode.stix.api.StiXecutorContextBuilder;
import org.cybcode.stix.api.StiXtractor;

public class StiXpressionRecursiveParser 
{
	private final boolean deduplicate;	
	private StiXpressionParserSlot resultSlot;
//	private int nodesCount;
	private boolean	linked;
	
	public StiXpressionRecursiveParser() 
	{
		this(true);
	}
	
	public StiXpressionRecursiveParser(boolean deduplicate) 
	{
		this.deduplicate = deduplicate;
	}
	
	public void step1_buildTree(StiXpressionParserSlot.ParserContext context, StiXtractor<?> resultNode)
	{
		if (resultSlot != null) throw new IllegalStateException();

		resultSlot = StiXpressionParserSlot.parse(context, resultNode);
//		nodesCount = context.getNodeCount();
	}
	
	public void step1_buildTree(StiXtractor<?> resultNode)
	{
		step1_buildTree(StiXpressionParserSlot.createDefaultContext(256, deduplicate), resultNode);
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
		resultSlot.flatten(context);
	}

	public void step5_flattenTree(StiXecutorContextBuilder builder)
	{
		if (resultSlot == null || !linked) throw new IllegalStateException();
		StiXpressionFlattenToXecutorContextBuilder flattener = new StiXpressionFlattenToXecutorContextBuilder(builder);
		resultSlot.flatten(flattener);
		flattener.finish();
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
	
	public void finish()
	{
		if (prev == null) return;
		builder.addNode(prev);
		prev = null;
	}
}