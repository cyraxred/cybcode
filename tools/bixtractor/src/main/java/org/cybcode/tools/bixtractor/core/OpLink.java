package org.cybcode.tools.bixtractor.core;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XpressionConfiguration;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.BiXourceLink;

class OpLink implements BiXourceLink
{
	final OpNode sourceNode;
	final OpNode receiverNode;
	final Parameter<?> param;

	public static OpLink newInstance(OpNode source, OpNode receiver, Parameter<?> param)
	{
		if (!(param instanceof PushParameter)) return new OpLink(source, receiver, param);
			
		return new OpLink(source, receiver, param) 
		{
			@Override protected boolean pushLinkValue(InternalXecutionContext context, Object value)
			{
				return ((PushParameter) param).pushValue(context, value);
			}

			@Override public boolean isPushLink(XpressionConfiguration config)
			{
				return true;
			}
		};
	}
	
	private OpLink(OpNode source, OpNode receiver, Parameter<?> param)
	{
		this.sourceNode = source;
		this.receiverNode = receiver;
		this.param = param;
	}
	
	@Override public boolean pushAndEvaluate(XecutionContext context, Object value)
	{
		InternalXecutionContext ctx = receiverNode.prepareContext(context);
		if (!pushLinkValue(ctx, value)) return false;
		if (!sourceNode.isPushEnabled()) return false;
		
		ctx = receiverNode.prepareContext(context);
		return receiverNode.evaluateAfterPush(ctx);
	}
	
	protected boolean pushLinkValue(InternalXecutionContext context, Object value) 
	{
		return receiverNode.pushValueByNode(context, param, value);
	}
	
	boolean isPushLink(XpressionConfiguration config)
	{
		return receiverNode.isPushByNodeEnabled(config);
	}
	
	int getSourceParamIndex()
	{
		return sourceNode.nodeIndex;
	}

	@Override public BiXtractor<?> getReceiver()
	{
		return receiverNode.op;
	}
	
	@Override public String toString()
	{
		return sourceNode.toShortString() + "=>" + param.toNameString() + "@" + receiverNode.toShortString();
	}
}

