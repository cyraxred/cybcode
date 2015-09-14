package org.cybcode.stix.core.compiler;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.cybcode.stix.api.StiXecutorContextBuilder;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.ops.StiX_Root;
import org.cybcode.tools.lists.SmallListCollector;

public class StiXpressionParserSlot extends SmallListCollector<StiXpressionParserSlot.SlotLink> implements StiXtractor.Visitor, StiXecutorContextBuilder.NodeDetails
{
	public static class ParserContext
	{
		private StiXpressionParserSlot rootSlot; 
		private final IdentityHashMap<StiXtractor<?>, StiXpressionParserSlot> nodesMap;
		private final Map<XpressionToken, StiXpressionParserSlot> dedupMap;
		
		public ParserContext(int initialCapacity, boolean deduplicate)
		{
			this.nodesMap = new IdentityHashMap<>(initialCapacity);
			if (deduplicate) {
				this.dedupMap = new HashMap<>(initialCapacity);
			} else {
				this.dedupMap = null;
			}
		}

		public StiXpressionParserSlot createParamSlot(StiXpressionParserSlot operation, StiXtractor<?> parameter)
		{
			return new StiXpressionParserSlot(parameter, operation);
		}

		public StiXpressionParserSlot createRootSlot(StiXtractor<?> parameter)
		{
			return new StiXpressionParserSlot(parameter);
		}

		public int getNodeCount()
		{
			return nodesMap.size();
		}

		public StiXpressionParserSlot getRoot()
		{
			if (rootSlot == null) {
				rootSlot = createRootSlot(StiX_Root.getInstance());
			}
			return rootSlot;
		}
	}
	
	public static ParserContext createDefaultContext(int initialCapacity, boolean deduplicate)
	{
		return new ParserContext(initialCapacity, deduplicate);
	}

	public static class SlotLink
	{
		public final StiXpressionParserSlot target;
		public final StiXtractor.Parameter<?> parameter;

		public SlotLink(StiXpressionParserSlot target, Parameter<?> parameter)
		{
			this.target = target;
			this.parameter = parameter;
		}
	}
	private ParserContext parserContext;
	
	public final StiXtractor<?> node;
	public final int stepsToResult;
	public final float opComplexity;
	private float totalOpComplexity;
	private final SmallListCollector<SlotLink> consumers = new SmallListCollector<>();
	private int parsedIndex;
	private int flattenIndex;

	public StiXpressionParserSlot(StiXtractor<?> node, StiXpressionParserSlot nextToResult)
	{
		this.node = node;
		stepsToResult = nextToResult.stepsToResult + 1;
		opComplexity = Math.max(1, node.getOperationComplexity());
		parsedIndex = -1;
		flattenIndex = -1;
	}
	
	protected StiXpressionParserSlot(StiXtractor<?> node)
	{
		this.node = node;
		stepsToResult = Integer.MAX_VALUE;
		opComplexity = Math.max(1, node.getOperationComplexity());
		parsedIndex = 0;
		flattenIndex = 0;
	}
	
	private StiXpressionParserSlot()
	{
		this.node = null;
		stepsToResult = 0;
		opComplexity = 0;
		parsedIndex = -1;
		flattenIndex = -1;
	}
	
	protected void beforeParse(ParserContext parserContext)
	{
		if (parserContext == null) throw new NullPointerException();
		if (this.parserContext != null || parsedIndex >= 0) throw new IllegalStateException();
		this.parserContext = parserContext;
	}
	
	protected void afterParse()
	{
		if (this.parserContext == null) throw new IllegalStateException();
		this.parserContext = null;
	}
	
	@Override public void visitParameter(Parameter<?> param)
	{
		if (this.parserContext == null) throw new IllegalStateException();
		
		param.setParamIndex(this.size());
		StiXtractor<?> paramSource = param.getSourceXtractor();
		
		StiXpressionParserSlot paramSlot;
		if (paramSource instanceof StiX_Root) {
			paramSlot = parserContext.rootSlot;
		} else { //if (paramSource instanceof StiXSubroutine)
			paramSlot = parserContext.nodesMap.get(paramSource);
			
			if (paramSlot != null) {
				if (paramSlot.parsedIndex < 0) throw new IllegalStateException("Circular reference");
			} else if (paramSlot == null) {
				paramSlot = parserContext.createParamSlot(this, paramSource);
				paramSlot.beforeParse(parserContext);
				parserContext.nodesMap.put(paramSource, paramSlot);
				paramSlot.node.visit(paramSlot);
				paramSlot.afterParse();
				
				if (parserContext.dedupMap != null) {
					XpressionToken dedupToken = paramSlot.createToken();
					StiXpressionParserSlot duplicateSlot = parserContext.dedupMap.get(dedupToken);
					if (duplicateSlot != null) {
						parserContext.nodesMap.put(paramSource, duplicateSlot);
						paramSlot = duplicateSlot;
					} else {
						paramSlot.parsedIndex = parserContext.nodesMap.size() - 1;
						parserContext.dedupMap.put(dedupToken, paramSlot);
					}
				} else {
					paramSlot.parsedIndex = parserContext.nodesMap.size() - 1;
				}
			}
		}
		
		this.add(new SlotLink(paramSlot, param));
	}

	public void link()
	{
		for (int i = 0; i < size(); i++) {
			SlotLink slotParam = get(i);
			StiXpressionParserSlot slot = slotParam.target;
			slot.link();
			slot.addTarget(this, slotParam.parameter);
		}
	}

	public void flatten(StiXpressionFlattenContext context)
	{
		float total = opComplexity;
		if (size() > 0) {
			for (int i = 0; i < size(); i++) {
				StiXpressionParserSlot paramSlot = get(i).target;
				paramSlot.flatten(context);
				total += paramSlot.opComplexity;
				context.addParamCount(1);
			}
		}
		if (flattenIndex < 0) {
			flattenIndex = context.addNode(this);
		}
		totalOpComplexity = total;
		context.addLinkCount(consumers.size());
	}
	
	private void addTarget(StiXpressionParserSlot consumer, Parameter<?> parameter)
	{
		consumers.add(new SlotLink(consumer, parameter));
	}
	
	public XpressionToken createToken()
	{
		int paramCount = size();
		switch (paramCount) {
			case 0: return new XpressionToken(node);
			case 1: return new XpressionToken(node, get(0).target.parsedIndex);
		}
		int[] args = new int[paramCount];
		for (int i = 0; i < paramCount; i++) {
			args[i] = get(1).target.parsedIndex;
		}
		return new XpressionToken(node, args);
	}

	@Override public int getXtractorIndex()
	{
		if (flattenIndex < 0) throw new IllegalStateException();
		return flattenIndex;
	}

	@Override public StiXtractor<?> getXtractor()
	{
		return node;
	}

	@Override public int getParamCount()
	{
		return size();
	}

	@Override public int getParamXtractorIndex(int index)
	{
		return get(index).target.getXtractorIndex();
	}

	@Override public int getTargetCount()
	{
		return consumers.size();
	}

	@Override public int getTargetXtractorIndex(int index)
	{
		return consumers.get(index).target.getXtractorIndex();
	}

	@Override public Parameter<?> getTargetParameter(int index)
	{
		return consumers.get(index).parameter;
	}
	
	@Override public boolean isNotifyOnRegularTarget(int index)
	{
		return false;
	}

	public static StiXpressionParserSlot parse(ParserContext context, StiXtractor<?> resultNode)
	{
		StiXpressionParserSlot stubSlot = new StiXpressionParserSlot();
		stubSlot.beforeParse(context);
		stubSlot.visitParameter(new StiXtractor.Parameter<>(resultNode));
		stubSlot.afterParse();
		return stubSlot.getFirstValueOrNull().target;
	}
}