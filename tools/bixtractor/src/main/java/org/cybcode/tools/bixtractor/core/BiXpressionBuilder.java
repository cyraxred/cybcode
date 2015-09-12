package org.cybcode.tools.bixtractor.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.cybcode.tools.bixtractor.api.BiXource;
import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.MonoPushParameter;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;
import org.cybcode.tools.bixtractor.ops.RootXtractor;
import org.cybcode.tools.bixtractor.ops.SubroutineXtractor;
import org.cybcode.tools.bixtractor.ops.XtractorFormatter;
import org.cybcode.tools.lists.SmallListCollector;

class BiXpressionBuilder
{
	private BiXpressionBuilder() {}
	
	private static NodeEntry createEntry(BiXtractor<?> op, NodeEntry container, Map<BiXtractor<?>, NodeEntry> nodeMap)
	{
		NodeEntry result = nodeMap.get(op);
		if (result == null) {
			result = NodeEntryFactory.newEntry(op, container);
			nodeMap.put(op, result);
		}
		if (container != null) {
			result.addOutlet(container);
		}
		return result;
	}

	private static NodeEntry createEntry(NodeEntry entry, int paramIndex, Map<BiXtractor<?>, NodeEntry> nodeMap)
	{
		return createEntry(entry.params.get(paramIndex).getExtractor(), entry, nodeMap);
	}

	private static List<NodeEntry> leafEntries(BiXtractor<?> op)
	{
		Map<BiXtractor<?>, NodeEntry> nodeMap = new IdentityHashMap<>();
		NodeEntry entry = createEntry(op, null, nodeMap);
		
		Queue<NodeEntry> inQueue = new LinkedList<>(); 
		Map<Object, NodeEntry> leafs = new HashMap<>();
		do {
			entry.visitNode();
			int sz = entry.params.size();
			if (sz == 0) {
				Object leafKey = entry.op.getOperationToken();
				if (leafKey == null) {
					leafKey = entry.op.getClass();
				}
				NodeEntry existingEntry = leafs.get(leafKey);
				if (existingEntry != null) {
					existingEntry.addOutletsOf(entry);
				} else {
					leafs.put(leafKey, entry);
				}
				entry = inQueue.poll(); 
				continue;
			}
			
			NodeEntry nextEntry = createEntry(entry, 0, nodeMap);
			for (int i = 1; i < sz; i++) {
				inQueue.add(createEntry(entry, i, nodeMap));
			}
			entry = nextEntry;
		} while (entry != null);
		
		throw new UnsupportedOperationException();
//		leafs.ensureCapacity(nodeMap.size());
//		return leafs;
	}
	
	public static List<OpNode> flatten(BiXtractor<?> op, List<OpNode> outNodes)
	{
		List<NodeEntry> nodes = leafEntries(op);
		if (nodes.isEmpty()) return Collections.emptyList();
		
		int generationStartIndex = 0;
		int nextGenerationNumber = 1;
		
		do {
			int generationEndIndex = nodes.size();
			for (int i = generationStartIndex; i < generationEndIndex; i++) {
				NodeEntry node = nodes.get(i);
				node.pullupOutlets(nodes, nextGenerationNumber);
			}
			generationStartIndex = generationEndIndex;
			nextGenerationNumber++;
		} while (generationStartIndex < nodes.size());
		
		Collections.sort(nodes);
		
		int outSize = nodes.size();
		if (outNodes == null) {
			outNodes = new ArrayList<>(outSize);
		} else if (outNodes instanceof ArrayList) {
			((ArrayList<?>) outNodes).ensureCapacity(outSize);
		}
		
		new NodeEntryMapper(nodes.size()).entriesToOpNodes(nodes, outNodes);
		
		return outNodes;
	}

	private static class NodeEntryMapper
	{
		private final Map<BiXtractor<?>, OpNode> nodeMap;
		
		NodeEntryMapper(int capacity)
		{
			nodeMap = new IdentityHashMap<>(capacity);
		}
		
		void entriesToOpNodes(List<NodeEntry> nodes, List<OpNode> outNodes)
		{
			for (NodeEntry node : nodes) {
				OpNode opNode = nodeMap.get(node.op);
				if (opNode == null) {
					opNode = node.createOpNode();
					opNode.nodeIndex = outNodes.size();
					opNode.distanceFromRoot = node.complexityFromRoot;
					
					switch (node.params.size()) {
						case 0: break;
						case 1: opNode.setParameter(mapParameter(opNode, node.params.get(0), 0)); break; 
						default: opNode.setParameters(mapParameters(opNode, node.params)); break;
					}
					
					nodeMap.put(node.op, opNode);
				}
				outNodes.add(opNode);
			}
		}
	
		private OpLink[] mapParameters(OpNode opNode, SmallListCollector<? extends Parameter<?>> params)
		{
			OpLink[] result = new OpLink[params.size()];
			for (int i = 0; i < result.length; i++) {
				result[i] = mapParameter(opNode, params.get(i), i);
			}
			return result;
		}
	
		private OpLink mapParameter(OpNode opNode, Parameter<?> param, int paramIndex)
		{
			OpNode paramSource = nodeMap.get(param.getExtractor());
			if (paramSource == null) {
				throw new IllegalArgumentException("Circular link: param=" + param);
			}
			param.setParamIndex(paramIndex, false); //TODO paramSource.isMultiValue()
			return paramSource.addOutlet(opNode, param);
		}
	}
}

class NodeEntry extends SmallListCollector<NodeEntry> implements Comparable<NodeEntry>
{
	int generationNumber;
	final int complexityFromRoot;
	final int distanceFromRoot;
	final BiXtractor<?> op;
	final SubroutineEntry parentJunction;
	final Params params;

	static class Params extends SmallListCollector<Parameter<?>> implements XpressionRegistrator 
	{
		@Override public void registerParameter(Parameter<?> param) { add(param); }
	}
	
	public NodeEntry(BiXtractor<?> op, NodeEntry containerEntry)
	{
		this.op = op;
		this.params = new Params();
		
		if (containerEntry == null) {
			this.parentJunction = null;
			this.complexityFromRoot = 0;
			this.distanceFromRoot = 0;
		} else {
			this.parentJunction = containerEntry.getCurrentJunction();
			this.complexityFromRoot = containerEntry.complexityFromRoot + Math.max(1, op.getOperationComplexity());
			this.distanceFromRoot = containerEntry.distanceFromRoot + 1;
		}
	}
	
	public void addOutletsOf(NodeEntry entry)
	{
		for (int outletIndex = 0; outletIndex < entry.size(); outletIndex++) {
			addOutlet(entry.get(outletIndex));
		}
	}

	public OpNode createOpNode()
	{
		return new OpValueNode(op); 
	}

	public void pullupOutlets(List<NodeEntry> entries, int generationNumber)
	{
		for (int i = size() - 1; i >= 0; i--) {
			NodeEntry entry = get(i);
			if (entry.generationNumber > 0) continue;
			entry.generationNumber = generationNumber;
			entries.add(entry);
		}
	}

	public void addOutlet(NodeEntry container)
	{
		this.add(container);
	}
	
	public boolean hasParameters()
	{
		return params.size() > 0;
	}

	public SubroutineEntry getCurrentJunction()
	{
		return parentJunction;
	}

	public BiXtractor<?> getSubroutineVar()
	{
		if (parentJunction == null) return RootXtractor.getInstance();
		return parentJunction.getSubroutineVar();
	}

	public void visitNode()
	{
		op.visit(params);
	}

	@Override public int compareTo(NodeEntry o)
	{
		if (this.generationNumber < o.generationNumber) return -1;
		if (this.generationNumber > o.generationNumber) return 1;
		if (this.complexityFromRoot < o.complexityFromRoot) return -1;
		if (this.complexityFromRoot > o.complexityFromRoot) return 1;
		return 0;
	}
	
	@Override public String toString()
	{
		return XtractorFormatter.nameAndTokenOf(op).toString();
	}
}

class SubroutineEntry extends NodeEntry
{
	private SubroutineVarXtractor<?>	varSource;

	public SubroutineEntry(SubroutineXtractor<?> op, NodeEntry currentEntry)
	{
		super(op, currentEntry);
	}

	public SubroutineEntry getCurrentJunction()
	{
		return this;
	}
	
	public BiXtractor<?> getSubroutineVar()
	{
		if (varSource == null) throw new IllegalStateException();
		return varSource;
	}
}

enum NodeEntryFactory
{
	DEFAULT {
		@Override public NodeEntry createEntry(BiXtractor<?> op, NodeEntry containerEntry) { return new NodeEntry(op, containerEntry); }
	}, 
	SUBROUTINE {
		@Override public SubroutineEntry createEntry(BiXtractor<?> op, NodeEntry containerEntry) { return new SubroutineEntry((SubroutineXtractor<?>) op, containerEntry); }
	}, 
	SOURCE {
		@Override public NodeEntry createEntry(BiXtractor<?> op, NodeEntry containerEntry) 
		{ 
			return new NodeEntry(op, containerEntry) 
			{
				@Override public OpNode createOpNode() { return new OpSourceNode((BiXource) op); }
			}; 
		}
	};
	
	protected abstract NodeEntry createEntry(BiXtractor<?> op, NodeEntry containerEntry);
	
	public static NodeEntry newEntry(BiXtractor<?> op, NodeEntry container)
	{
		if (op instanceof SubroutineXtractor) return SUBROUTINE.createEntry(op, container);
		if (op instanceof BiXource) return SOURCE.createEntry(op, container);
		return DEFAULT.createEntry(op, container);
	}
}

class SubroutineVarXtractor<T> implements BiXtractor<T>
{
	private final MonoPushParameter<T> p0;
	
	SubroutineVarXtractor(BiXtractor<? extends T> p0)
	{
		this.p0 = new MonoPushParameter<T>(p0); 
	}

	@Override public void visit(XpressionRegistrator registry)
	{
		registry.registerParameter(p0);
	}
	
	@Override public T evaluate(XecutionContext context)
	{
		return p0.get(context);
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_VAR;
	}

	@Override public String toString()
	{
		return "VAR" + "(" + p0.getExtractor() + ")";
		//(isRepeatable() ? "*" : "") + 
	}
}