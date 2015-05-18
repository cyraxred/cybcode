package org.cybcode.tools.bixtractor.core;

import java.util.List;

import org.cybcode.tools.bixtractor.api.BiXource;
import org.cybcode.tools.bixtractor.api.BiXourceContext;

class OpSourceNode extends OpNode
{
	private OpLink parameter;
	private BiXourceContext sourceContext;

	OpSourceNode(int nodeIndex, BiXource op, int distanceFromRoot)
	{
		super(nodeIndex, op, distanceFromRoot);
	}
	
	@Override OpSourceNode copy()
	{
		OpSourceNode result = (OpSourceNode) super.copy();
		result.parameter = null;
		result.sourceContext = null;
		return result;
	}

	@Override public int mapParamIndex(int paramIndex)
	{
		if (this.parameter == null || paramIndex != 0) throw new IllegalArgumentException();
		return parameter.getSourceParamIndex();
	}

	@Override public boolean hasParameters()
	{
		return this.parameter != null;
	}

	@Override void mapParameters(OpNode node, List<OpNode> mappedNodes)
	{
		if (this.parameter != null) throw new IllegalStateException();
		
		OpLink nodeParam =  ((OpSourceNode) node).parameter;
		if (nodeParam == null) return;
		
		this.parameter = mapParameter(nodeParam, mappedNodes);
	}

	@Override void setParameter(OpLink opLink)
	{
		if (this.parameter != null) throw new IllegalStateException();
		this.parameter = opLink;
	}

	@Override void setParameters(OpLink[] opLinks)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override BiXourceContext getSourceContext()
	{
		if (sourceContext == null) throw new IllegalStateException();
		return sourceContext;
	}
	
	@Override protected void registerValueReceiver(OpLink link)
	{
		if (!link.isPushLink()) {
			throw new IllegalArgumentException("All source links must be push-able: link=" + link);
		}
		super.registerValueReceiver(link);
	}

	@Override protected boolean pushToReceivers(InternalXecutionContext context, Object value)
	{
		return BiXource.Result.STOP.equals(value);
	}

	@Override protected boolean evaluateAfterPush(InternalXecutionContext context)
	{
		return true;
	}
	
	@Override void compile()
	{
		super.compile();
		if (sourceContext != null) throw new IllegalStateException();
		sourceContext = ((BiXource) op).buildContext(getPushReceivers().clone());
	}

	@Override protected boolean pushValueByNode(InternalXecutionContext context, Parameter<?> param, Object value)
	{
		throw new IllegalStateException("Can't be called");
	}

	@Override protected boolean canPushByNode()
	{
		return false; //push behavior is controlled by source operation
	}
}