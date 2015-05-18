package org.cybcode.tools.bixtractor.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.BiXource;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;

class BiXpressionBuilder implements XpressionRegistrator
{
	private final List<OpNode> nodes;
	private final Map<BiXtractor<?>, OpNode> nodeMap = new IdentityHashMap<>();
	
	private OpNode currentNode;
	private List<OpLink> currentNodeLinks;
	private OpLink currentNodeSingleLink;
	private OpNode currentDomainOwner;
	private int currentDistanceFromRoot;
	
	public static void flatten(BiXtractor<?> xtractor, List<OpNode> nodes)
	{
		BiXpressionBuilder builder = new BiXpressionBuilder(xtractor, nodes);
		builder.build();
	}
	
	private BiXpressionBuilder(BiXtractor<?> xtractor, List<OpNode> nodes)
	{
		this.nodes = nodes;
		OpNode node = new OpValueNode(xtractor) 
		{
			@Override public boolean evaluate(InternalXecutionContext context)
			{
				super.evaluate(context);
				return true; //allow push to stop when it reaches the root
			}
			
			@Override boolean hasReceivers()
			{
				return true;
			}
		};
		node.distanceFromRoot = 0;
		node.domainOwner = node;
		this.currentDomainOwner = node;
		this.nodes.add(node);
		nodeMap.put(node.op, node);
	}
	
	private void build()
	{
		for (int i = 0; i < nodes.size(); i++) {
			currentNode = nodes.get(i);
			currentDistanceFromRoot = currentNode.distanceFromRoot + Math.max(1, currentNode.op.getOperationComplexity());
			
			currentNode.op.visit(this); //list of nodes expands here
			if (currentNodeSingleLink != null) {
				currentNode.setParameter(currentNodeSingleLink);
				currentNodeSingleLink = null;
			} else if (currentNodeLinks == null || currentNodeLinks.isEmpty()) {
				currentNode.setParameter(null);
			} else {
				currentNode.setParameters(currentNodeLinks.toArray(new OpLink[currentNodeLinks.size()]));
			}
			currentNodeLinks = null;
		}
		Collections.reverse(nodes);
		for (int i = nodes.size() - 1; i >= 0; i--) {
			nodes.get(i).nodeIndex = i;
		}
	}
	
	@Override public void registerParameter(Parameter<?> param)
	{
		OpNode sourceNode = addNode(param.getExtractor());

		int paramIndex;
		if (currentNodeLinks != null) {
			paramIndex = currentNodeLinks.size();
		} else if (currentNodeSingleLink == null) {
			paramIndex = 0;
		} else {
			currentNodeLinks = new ArrayList<>(2);
			currentNodeLinks.add(currentNodeSingleLink);
			currentNodeSingleLink = null;
			paramIndex = 1;
		}
		param.setParamIndex(paramIndex);
		OpLink link = sourceNode.addValueReceiver(currentNode, param);
		if (paramIndex == 0) {
			currentNodeSingleLink = link;
			return;
		} 
		currentNodeLinks.add(link);
	}
	
	private OpNode addNode(BiXtractor<?> op)
	{
		OpNode node = nodeMap.get(op);
		if (node != null) {
			if (node.domainOwner.nodeIndex != currentDomainOwner.nodeIndex) {
				throw new IllegalStateException();
			}
			
			if (node.distanceFromRoot > currentDistanceFromRoot) {
				node.distanceFromRoot = currentDistanceFromRoot; 
			}
			return node;
		}

		if (op instanceof BiXource) {
			node = new OpSourceNode((BiXource) op);
		} else {
			node = new OpValueNode(op);
		}
		node.distanceFromRoot = currentDistanceFromRoot;
		node.nodeIndex = nodes.size();
		node.domainOwner = currentDomainOwner;
		
		if (currentNode != null && !currentNode.isPushEnabled()) {
			node.disablePush();
		}
		
		nodes.add(node);
		nodeMap.put(op, node);
		
		return node;
	}
}
