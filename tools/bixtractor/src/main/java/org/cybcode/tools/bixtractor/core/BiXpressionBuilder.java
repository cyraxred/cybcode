package org.cybcode.tools.bixtractor.core;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.cybcode.tools.bixtractor.api.BiXource;
import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.MonoPushParameter;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;
import org.cybcode.tools.bixtractor.ops.JointXtractor;
import org.cybcode.tools.bixtractor.ops.JunctionXtractor;
import org.cybcode.tools.bixtractor.ops.RootXtractor;

class BiXpressionBuilder
{
	private final List<NodeEntry> nodes;
	private final Map<BiXtractor<?>, NodeEntry> nodeMap = new IdentityHashMap<>();
	
	private NodeEntry currentEntry;
	
	public static void flatten(BiXtractor<?> xtractor, List<OpNode> outNodes)
	{
		BiXpressionBuilder builder = new BiXpressionBuilder(xtractor);
		builder.build(outNodes);
	}
	
	private BiXpressionBuilder(BiXtractor<?> xtractor)
	{
		this.nodes = new ArrayList<>();
		OpNode node = new OpValueNode(xtractor) 
		{
			@Override public boolean evaluate(InternalXecutionContext context)
			{
				super.evaluate(context);
				return true; //allow push to stop iterations when it reaches the final node
			}
			
			@Override boolean hasReceivers()
			{
				return true;
			}
		};

		addNode(node);
	}
	
	private void build(List<OpNode> outNodes)
	{
		for (int i = 0; i < nodes.size(); i++) {
			currentEntry = nodes.get(i);
			currentEntry.visitNode(); //list of nodes expands here
		}
		
		if (outNodes instanceof ArrayList) {
			((ArrayList<?>) outNodes).ensureCapacity(outNodes.size() + nodes.size());
		}

		for (int i = nodes.size() - 1; i >= 0; i--) {
			OpNode node = nodes.get(i).node; 
			node.nodeIndex = outNodes.size();
			outNodes.add(node);
		}
	}

	private NodeEntry addNode(OpNode node)
	{
		node.nodeIndex = nodes.size();
		NodeEntry entry = node.op instanceof JunctionXtractor ? new JunctionEntry(node, currentEntry) : new NodeEntry(node, currentEntry);
		this.nodes.add(entry);
		nodeMap.put(node.op, entry);
		return entry;
	}
	
	private NodeEntry addNode(BiXtractor<?> op)
	{
		if (op instanceof JointXtractor) {
			op = currentEntry.getJointSource();
		}
		
		NodeEntry entry = nodeMap.get(op);
		if (entry != null) {
			if (entry.distanceFromRoot > currentEntry.distanceFromRoot) {
				entry.setDistanceFromRoot(currentEntry);
			}
			return entry;
		}

		OpNode node;
		if (op instanceof BiXource) {
			node = new OpSourceNode((BiXource) op);
		} else {
			node = new OpValueNode(op);
		}

		node.distanceFromRoot = currentEntry.distanceFromRoot;
		return addNode(node);
	}

	private class NodeEntry
	{
		int distanceFromRoot;
		final OpNode node;
		final JunctionEntry parentJunction;
		
		public NodeEntry(OpNode node, NodeEntry currentEntry)
		{
			this.node = node;
			this.parentJunction = currentEntry == null ? null : currentEntry.getCurrentJunction();
			setDistanceFromRoot();
		}
		
		public JunctionEntry getCurrentJunction()
		{
			return parentJunction;
		}

		public BiXtractor<?> getJointSource()
		{
			if (parentJunction == null) return RootXtractor.getInstance();

			return parentJunction.getJointSource();
		}

		public void visitNode()
		{
			class Params extends SmallListCollector<Parameter<?>> implements XpressionRegistrator 
			{
				@Override public void registerParameter(Parameter<?> param) { add(param); }
			}
			
			Params params = new Params();
			node.op.visit(params);
			applyParams(params);
		}
		
		protected void applyParams(SmallListCollector<Parameter<?>> params)
		{
			switch (params.size()) {
				case 0: node.setParameter(null); break;
				case 1: node.setParameter(addParameter(0, params.getFirstValueOrNull())); break;
				default: node.setParameters(addParameters(params.asList())); break;
			}
		}
	
		protected OpLink addParameter(int paramIndex, Parameter<?> param)
		{
			OpNode sourceNode = addNode(param.getExtractor()).node;
			param.setParamIndex(paramIndex);
			return sourceNode.addValueReceiver(this.node, param);
		}

		private OpLink[] addParameters(List<Parameter<?>> params)
		{
			OpLink[] links = new OpLink[params.size()];
			for (int i = 0; i < links.length; i++) {
				links[i] = addParameter(i, params.get(i));
			}
			return links;
		}

		public void setDistanceFromRoot(NodeEntry entry)
		{
			this.node.distanceFromRoot = entry.node.distanceFromRoot;
			setDistanceFromRoot();
		}
		
		private void setDistanceFromRoot()
		{
			this.distanceFromRoot = node.distanceFromRoot + Math.max(1, node.op.getOperationComplexity());
		}
	
		public void setParameters(OpLink[] links)
		{
			node.setParameters(links);
		}
	
		public void setParameter(OpLink link)
		{
			node.setParameter(link);
		}
	}
	
	class JunctionEntry extends NodeEntry
	{
		private BiXtractor<?>	jointSource;

		public JunctionEntry(OpNode node, NodeEntry currentEntry)
		{
			super(node, currentEntry);
			JunctionXtractor<?> op = ((JunctionXtractor<?>) node.op);
			op.getClass();
		}

		public JunctionEntry getCurrentJunction()
		{
			return this;
		}
		
		public BiXtractor<?> getJointSource()
		{
			if (jointSource == null) throw new IllegalStateException();
			return jointSource;
		}

		@Override protected void applyParams(SmallListCollector<Parameter<?>> params)
		{
			node.setParameter(addParameter(0, params.get(0)));
			this.jointSource = new JointSourceXtractor<>(params.get(1).getExtractor());
		}
	}
}

class JointSourceXtractor<T> implements BiXtractor<T>
{
	private final MonoPushParameter<T> p0;
	
	JointSourceXtractor(BiXtractor<? extends T> p0)
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

	@Override public boolean isRepeatable()
	{
		return p0.isRepeatable();
	}
	
	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_JUNCTION;
	}

	@Override public String toString()
	{
		return "VAR" + (isRepeatable() ? "*" : "") + "(" + p0.getExtractor() + ")";
	}
}