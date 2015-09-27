package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;

public class XecutorContextNode implements StiXecutorContext
{
	private final AbstractXecutorContextStorage storage;
	private final StiXpressionNode node;
	private final int index;
	
	public XecutorContextNode(StiXpressionNode node, AbstractXecutorContextStorage storage)
	{
		if (storage == null) throw new NullPointerException();
		this.node = node;
		this.index = node.getIndex();
		this.storage = storage;
	}

	@Override public Object getParamValue(int paramIndex)
	{
		int xtractorIndex = node.mapParamIndex(paramIndex);
//		if (!storage.isFinalState(xtractorIndex)) return null;
		return storage.getResultValue(xtractorIndex);
	}
	
	@Override public boolean hasParamFinalValue(int paramIndex)
	{
		int xtractorIndex = node.mapParamIndex(paramIndex);
		if (!storage.isFinalState(xtractorIndex)) return false;
		return storage.getResultValue(xtractorIndex) != null;
	}
	
	@Override public StiXtractor<?> getCurrentXtractor()
	{
		return node.getXtractor();
	}
	
	@Override public Object getInterimValue()
	{
		return storage.getInterimValue(index);
	}
	
	@Override public boolean hasInterimValue()
	{
		return storage.hasInterimValue(index);
	}

	public StiXpressionNode getXpressionNode()
	{
		return node;
	}

	public int getIndex()
	{
		return index;
	}

	public AbstractXecutorContextStorage getStorage()
	{
		return storage;
	}
}