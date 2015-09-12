package org.cybcode.tools.bixtractor.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BiXpressionDeduplicator
{
	private static final BiXpressionOptimizer OPT_INSTANCE	= new BiXpressionOptimizer() 
	{
		@Override public List<OpNode> optimize(List<OpNode> opNet)
		{
			return new BiXpressionDeduplicator(opNet).getResult();
		}
	};

	public static BiXpressionOptimizer getInstance()
	{
		return OPT_INSTANCE;
	}
	
	private final List<OpNode> mappedNodes;
	private final Map<OpNodeToken, OpNode> nodeMap;
	
	private BiXpressionDeduplicator(List<OpNode> nodes)
	{
		this.mappedNodes = new ArrayList<>(nodes.size());
		this.nodeMap = new HashMap<>(nodes.size());

		int terminalNodeIndex = nodes.size() - 1;
		
//		List<OpNode> verbatimNodes = new ArrayList<>();
		List<OpNode> leafNodes = new ArrayList<>();
		for (int i = terminalNodeIndex; i >= 0; i--) {
			OpNode node = nodes.get(i);
			if (node.hasOutlets()) {
				if (node.hasParameters()) continue;
				
//				if (node.domainOwner.nodeIndex == terminalNodeIndex /* | constant nodes */) {
//					node.domainOwner = null;
					leafNodes.add(node);
//				} else {
//					verbatimNodes.add(node);
//					continue;
//				}
			}
			nodes.set(i, null);
		}
		
		if (leafNodes.size() > 1) {
			Collections.sort(leafNodes, OpNode.NODE_DISTANCE_ASC);
		}
		
		for (OpNode node : leafNodes) {
			mapNode(node);
		}

//		for (OpNode node : verbatimNodes) {
//			//pushDomainOwner
//			//mapNode(node);
//		}
		
		for (int i = 0; i < nodes.size(); i++) {
			OpNode node = nodes.get(i);
			if (node == null) continue;
			
			mapNode(node);			
		}
	}
	
	public List<OpNode> getResult()
	{
		return mappedNodes;
	}

	private void mapNode(OpNode node)
	{
		OpNodeToken nodeToken = node.createToken();
		
		OpNode mappedNode = nodeMap.get(nodeToken);
		if (mappedNode != null) {
			node.nodeIndex = mappedNode.nodeIndex;
			return;
		}
		
		mappedNode = node.copy();
		mappedNode.nodeIndex = mappedNodes.size();
		node.nodeIndex = mappedNode.nodeIndex;
		
		mappedNode.mapParameters(node, mappedNodes);
		
		nodeToken = mappedNode.createToken();
		mappedNodes.add(mappedNode);
//		mappedNode.domainOwner = mappedNodes.get(node.domainOwner.nodeIndex);
		
		OpNode prev = nodeMap.put(nodeToken, mappedNode);
		if (prev != null) {
			throw new IllegalStateException();
		}
	}
}

