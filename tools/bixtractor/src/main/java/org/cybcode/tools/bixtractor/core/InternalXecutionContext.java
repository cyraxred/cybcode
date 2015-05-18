package org.cybcode.tools.bixtractor.core;

import org.cybcode.tools.bixtractor.api.BiXourceContext;
import org.cybcode.tools.bixtractor.api.XecutionContext;

class InternalXecutionContext implements XecutionContext
{
	private final Object rootValue;
	private final Object[] context;
	
	private OpNode node;
	
	public InternalXecutionContext(Object rootValue, int count)
	{
		this.rootValue = rootValue;
		this.context = new Object[count << 1];
	}

	@Override public Object getRootValue()
	{
		return rootValue;
	}
	
	private Object nullValue(Object value)
	{
		if (value == this) return null;
		return value;
	}

	@Override public Object getParamValue(int paramIndex)
	{
		return nullValue(context[1 + (node.mapParamIndex(paramIndex) << 1)]);
	}

	@Override public Object getLocalValue()
	{
		return context[node.nodeIndex << 1];
	}

	@Override public void setLocalValue(Object value)
	{
		context[node.nodeIndex << 1] = value;
	}

	void setNode(OpNode node)
	{
		this.node = node;
	}
	
	void setResultValue(Object value)
	{
		setNodeResultValue(node, value);
	}

	void setNodeResultValue(OpNode node, Object value)
	{
		context[1 + (node.nodeIndex << 1)] = value != null ? value : this;
	}

	boolean hasNodeResultValue(OpNode node)
	{
		return context[1 + (node.nodeIndex << 1)] != null;
	}
	
	Object getResultValue()
	{
		return nullValue(context[1 + (node.nodeIndex << 1)]);
	}

	Object getFinalResultValue()
	{
		return nullValue(context[context.length - 1]);
	}

	@Override public BiXourceContext getSourceContext()
	{
		return node.getSourceContext();
	}
}