package org.cybcode.tools.bixtractor.core;

import java.util.List;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.BiXourceContext;

class OpValueNode extends OpNode
{
	private OpLink[] parameters;
	
	OpValueNode(int nodeIndex, BiXtractor<?> op, int distanceFromRoot)
	{
		super(nodeIndex, op, distanceFromRoot);
	}

	@Override OpValueNode copy()
	{
		OpValueNode result = (OpValueNode) super.copy();
		result.parameters = null;
		return result;
	}

	@Override public OpNodeToken createToken()
	{
		int[] params;
		if (parameters == null || parameters.length == 0) {
			params = null;
		} else {
			params = new int[parameters.length];
			for (int i = params.length - 1; i >= 0; i--) {
				params[i] = parameters[i].sourceNode.nodeIndex;
			}
		}
		return new OpNodeToken(op.getClass(), op.getOperationToken(), params);
	}

	@Override public boolean hasParameters()
	{
		return parameters != null && parameters.length > 0;
	}

	@Override public int mapParamIndex(int paramIndex)
	{
		return parameters[paramIndex].getSourceParamIndex();
	}
	
	@Override void mapParameters(OpNode node, List<OpNode> mappedNodes)
	{
		if (parameters != null) throw new IllegalStateException();
		
		OpLink[] nodeParams =  ((OpValueNode) node).parameters;
		if (nodeParams == null) return;
		
		OpLink[] mappedNodeParams = new OpLink[nodeParams.length];
		for (int i = 0; i < mappedNodeParams.length; i++) {
			mappedNodeParams[i] = mapParameter(nodeParams[i], mappedNodes);
		}
		this.parameters = mappedNodeParams;
	}

	@Override void setParameter(OpLink opLink)
	{
		setParameters(opLink == null ? null : new OpLink[] { opLink });
	}

	@Override void setParameters(OpLink[] opLinks)
	{
		if (parameters != null) throw new IllegalStateException();
		parameters = opLinks;
	}
	
	@Override BiXourceContext getSourceContext()
	{
		throw new UnsupportedOperationException();
	}

	@Override protected boolean pushValueByNode(InternalXecutionContext context, Parameter<?> param, Object value)
	{
		switch (parameters.length) {
			case 1: {
				return evaluate(context); //single value is always evaluated immediately when parameter is available 
			}
			case 2: {
				OpLink otherParam = parameters[0].param == param ? parameters[1] : parameters[0];
				if (!context.hasNodeResultValue(otherParam.sourceNode)) return false;
				return evaluate(context);
			}
			default: return false;
		}
	}

	@Override protected boolean canPushByNode()
	{
		return parameters.length == 1 || parameters.length == 2;
	}
}