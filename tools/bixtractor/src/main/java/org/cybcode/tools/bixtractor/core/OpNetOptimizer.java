package org.cybcode.tools.bixtractor.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class OpNetOptimizer
{
	private final List<OpNode> mappedNodes;
	private final Map<OpNodeToken, OpNode> nodeMap;
	
	public OpNetOptimizer(List<OpNode> nodes)
	{
		this.mappedNodes = new ArrayList<>(nodes.size());
		this.nodeMap = new HashMap<>(nodes.size());

		List<OpNode> leafNodes = new ArrayList<>();
		for (int i = nodes.size() - 1; i >= 0; i--) {
			OpNode node = nodes.get(i);
			if (node.hasReceivers()) {
				if (node.hasParameters()) continue;
				leafNodes.add(node);
			}
			nodes.set(i, null);
		}
		
		if (leafNodes.size() > 1) {
			Collections.sort(leafNodes, OpNode.NODE_DISTANCE_ASC);
		}
		
		for (OpNode node : leafNodes) {
			mapNode(node);
		}
		
		for (int i = nodes.size() - 1; i >= 0; i--) {
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
		OpNode prev = nodeMap.put(nodeToken, mappedNode);
		if (prev != null) {
			throw new IllegalStateException();
		}
	}
}

