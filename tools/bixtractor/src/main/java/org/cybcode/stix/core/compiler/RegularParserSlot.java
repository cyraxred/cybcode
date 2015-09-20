package org.cybcode.stix.core.compiler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutorContextBuilder;
import org.cybcode.stix.api.StiXecutorContextBuilder.NodeDetails;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.ops.StiX_Root;
import org.cybcode.stix.ops.StiX_Subroutine;
import org.cybcode.tools.lists.SmallListCollector;

public class RegularParserSlot extends SmallListCollector<SlotLink> implements StiXtractor.Visitor, StiXecutorContextBuilder.NodeDetails
{
	public static class ParserContext
	{
		private FrameOwnerSlot outermostFrameOwner;
		private RegularParserSlot rootSlot; 
		private final IdentityHashMap<StiXtractor<?>, RegularParserSlot> nodesMap;
		private final Map<XpressionToken, RegularParserSlot> dedupMap;
		private final StiXComplexityHelper complexityHelper;
		
		public ParserContext(int initialCapacity, boolean deduplicate, StiXComplexityHelper complexityHelper)
		{
			if (complexityHelper == null) throw new NullPointerException();
			this.complexityHelper = complexityHelper;
			this.nodesMap = new IdentityHashMap<>(initialCapacity);
			if (deduplicate) {
				this.dedupMap = new HashMap<>(initialCapacity);
			} else {
				this.dedupMap = null;
			}
		}

		public RegularParserSlot createParamSlot(RegularParserSlot operation, StiXtractor<?> parameter)
		{
			if (parameter instanceof StiX_Subroutine) {
				return new SubroutineParserSlot((StiX_Subroutine<?, ?>) parameter, operation, this);
			}

			return new RegularParserSlot(parameter, operation, this);
		}

		public RegularParserSlot createRootSlot(StiXtractor<?> parameter)
		{
			return new RootParserSlot(parameter, this);
		}

		public int getNodeCount()
		{
			return nodesMap.size();
		}

		public RegularParserSlot getRoot()
		{
			if (rootSlot == null) {
				rootSlot = createRootSlot(StiX_Root.getInstance());
			}
			return rootSlot;
		}

		public StiXComplexityHelper getComplexityHelper()
		{
			return complexityHelper;
		}

		public void reportFrame(int frameLevel)
		{
		}

		public FrameOwnerSlot createOutermostFrame()
		{
			return new OuterParserSlot(this);
		}

		public FrameOwnerSlot getOutermostFrameOwner()
		{
			if (outermostFrameOwner == null) {
				outermostFrameOwner = createOutermostFrame();
			}
			return outermostFrameOwner;
		}
	}
	
	public static ParserContext createDefaultContext(int initialCapacity, boolean deduplicate, StiXComplexityHelper complexityHelper)
	{
		return new ParserContext(initialCapacity, deduplicate, complexityHelper);
	}

	private ParserContext parserContext;
	
	public final StiXtractor<?> node;
	public final int stepsToResult;
	public final float opComplexity;
	public final FrameOwnerSlot traversedFrameOwner;
	private final SmallListCollector<SlotLink> consumers = new SmallListCollector<>();
	private int parsedIndex;
	private int flattenIndex;
	private FrameOwnerSlot actualFrameOwner;

	@Override public String toString()
	{
		Object token = node.getOperationToken();
		return node.getClass().getSimpleName() + "(" + toNameString() + (token == null ? "" : ",token=" + token) + ")" + super.toString();		
	}
	
	public String toNameString()
	{
		return "#" + parsedIndex;
	}

	public RegularParserSlot(StiXtractor<?> node, RegularParserSlot nextToResult, ParserContext parserContext)
	{
		this.node = node;
		this.parserContext = parserContext;
		traversedFrameOwner = nextToResult.getParamFrameOwner();
		stepsToResult = nextToResult.stepsToResult + 1;
		opComplexity = Math.max(1, node.getOperationComplexity(parserContext.getComplexityHelper()));
		parsedIndex = -1;
		flattenIndex = -1;		
	}
	
	protected RegularParserSlot(StiXtractor<?> node, ParserContext parserContext)
	{
		this.node = node;
		this.parserContext = parserContext;
		traversedFrameOwner = parserContext.getOutermostFrameOwner();
		actualFrameOwner = findActualFrameOwner();
		stepsToResult = Integer.MAX_VALUE;
		opComplexity = Math.max(1, node.getOperationComplexity(parserContext.getComplexityHelper()));
		parsedIndex = 0;
		flattenIndex = 0;
	}
	
	protected RegularParserSlot(ParserContext context)
	{
		this.node = null;
		this.parserContext = context;
		traversedFrameOwner = findActualFrameOwner();
		stepsToResult = 0;
		opComplexity = 0;
		parsedIndex = -1;
		flattenIndex = -1;
	}
	
	private void afterParseAndResetContext()
	{
		if (this.parserContext == null) throw new IllegalStateException();
		afterParse();
		this.parserContext = null;
	}

	protected void afterParse()
	{
		this.actualFrameOwner = findActualFrameOwner();
	}

	public FrameOwnerSlot getParamFrameOwner()
	{
		return traversedFrameOwner;
	}
	
	public FrameOwnerSlot getOuterFrameOwner()
	{
		return traversedFrameOwner;
	}
	
	protected FrameOwnerSlot findActualFrameOwner()
	{
		int index = size() - 1;
		if (index < 0) return parserContext.getOutermostFrameOwner();

		FrameOwnerSlot result = get(index).target.actualFrameOwner;
		while (--index >= 0) {
			FrameOwnerSlot anotherFrameOwner = get(index).target.actualFrameOwner;
			result = result.selectInnerFrame(anotherFrameOwner); 
		}
		return result;
	}

	@Override public void visitParameter(Parameter<?> param)
	{
		if (this.parserContext == null) throw new IllegalStateException();
		
		param.setParamIndex(this.size());
		StiXtractor<?> paramSource = param.getSourceXtractor();
		
		RegularParserSlot paramSlot;
		if (paramSource instanceof StiX_Root) {
			paramSlot = parserContext.rootSlot;
		} else { 
			paramSlot = parserContext.nodesMap.get(paramSource);
			
			if (paramSlot != null) {
				paramSlot.verifyAlternativePathToRoot(this);
			} else {
				if (traversedFrameOwner != null) {
					paramSlot = traversedFrameOwner.replaceFrameSlot(this, paramSource);
				}
				if (paramSlot == null) {
					paramSlot = createParamSlot(paramSource, parserContext);
					int parsedIndex = parserContext.nodesMap.size(); 
					parserContext.nodesMap.put(paramSource, paramSlot);
					paramSlot.node.visit(paramSlot);
					paramSlot.afterParseAndResetContext();

					if (parserContext.dedupMap != null) {
						XpressionToken dedupToken = paramSlot.createToken();
						RegularParserSlot duplicateSlot = parserContext.dedupMap.get(dedupToken);
						if (duplicateSlot != null) {
							parserContext.nodesMap.put(paramSource, duplicateSlot);
							paramSlot = duplicateSlot;
						} else {
							paramSlot.parsedIndex = parsedIndex;
							parserContext.dedupMap.put(dedupToken, paramSlot);
						}
					} else {
						paramSlot.parsedIndex = parsedIndex;
					}
				}
			}
		}
		
		this.add(new SlotLink(paramSlot, param));
	}

	protected RegularParserSlot createParamSlot(StiXtractor<?> paramSource, ParserContext parserContext)
	{
		return parserContext.createParamSlot(this, paramSource);
	}

	private void verifyAlternativePathToRoot(RegularParserSlot nextToRoot)
	{
		if (parsedIndex < 0) throw new IllegalStateException("Circular reference");
		if (traversedFrameOwner == null) return;
		
		traversedFrameOwner.verifyAlternativeFrameOwner(nextToRoot.getParamFrameOwner());
	}

	public void link()
	{
		for (int i = 0; i < size(); i++) {
			SlotLink slotParam = get(i);
			RegularParserSlot slot = slotParam.target;
			slot.link();
			slot.addTarget(this, slotParam.parameter);
		}
	}

	public void flatten(StiXpressionFlattenContext context)
	{
		context.addParamCount(size());
		for (int i = 0; i < size(); i++) {
			RegularParserSlot paramSlot = get(i).target;
			paramSlot.flatten(context);
		}
		if (flattenIndex < 0) {
			flattenIndex = context.addNode(this);
		}
		context.addLinkCount(consumers.size());
	}
	
	private void addTarget(RegularParserSlot consumer, Parameter<?> parameter)
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
			args[i] = get(i).target.parsedIndex;
		}
		if (node instanceof StiXtractor.Commutative) {
			Arrays.sort(args);
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

	@Override public NodeDetails getTargetXtractor(int index)
	{
		return consumers.get(index).target;
	}
	
	@Override public Parameter<?> getTargetParameter(int index)
	{
		return consumers.get(index).parameter;
	}
	
	public static RegularParserSlot parse(ParserContext context, StiXtractor<?> resultNode)
	{
		RegularParserSlot stubSlot = new RegularParserSlot(context);
		stubSlot.visitParameter(new StiXtractor.Parameter<>(resultNode));
		stubSlot.afterParseAndResetContext();
		return stubSlot.getFirstValueOrNull().target;
	}
}