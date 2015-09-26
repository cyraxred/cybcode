package org.cybcode.stix.core.xecutors;

import java.util.Arrays;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXecutorContextBinder;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.ops.StiX_Root;

class XecutorContext implements StiXecutorContext, StiXecutorContextBinder
{
	private final StiXpressionNode[] nodes;
	private XpressionRunnerBuilder.Runner runner;
	private Object[] results;
	
	private StiXpressionNode currentNode;
	private int currentIndex;
	
	public XecutorContext(StiXpressionNode[] nodes)
	{
		if (!(nodes[0].getXtractor() instanceof StiX_Root)) throw new IllegalArgumentException("Root must be present as index 0");
		this.nodes = nodes;
	}

	@Override public StiXecutorContextControl bind(XpressionRunnerBuilder.Runner runner)
	{
		if (runner == null) throw new IllegalStateException();
		this.runner = runner;
		return new XecutorContextControl();
	}
	
	private boolean setValue(int xtractorIndex, Object value)
	{
		if (value == StiXecutor.KEEP_LAST_VALUE) return false;
		results[xtractorIndex] = value;
		return true;
	}

	private Object getValue(int xtractorIndex)
	{
		return results[xtractorIndex];
	}

	@Override public Object getParamValue(int paramIndex)
	{
		int index = mapParamIndex(paramIndex);
		if (!runner.hasFinalState(index)) return null;
		return getValue(index);
	}

	@Override public boolean hasParamFinalValue(int paramIndex)
	{
		int index = mapParamIndex(paramIndex);
		return runner.hasFinalState(index);
	}

	@Override public Object getInterimValue()
	{
		int index = currentIndex();
		if (runner.hasFinalState(index)) throw new IllegalStateException();
		return getValue(index);
	}
	
	@Override public boolean hasInterimValue()
	{
		int index = currentIndex();
		if (runner.hasFinalState(index)) return false;
		return results[index] != null;
	}
	
	private int currentIndex()
	{
		return currentIndex;
	}
	
	private int mapParamIndex(int paramIndex)
	{
		return currentNode.mapParamIndex(paramIndex);
	}
	
	@Override public StiXtractor<?> getCurrentXtractor()
	{
		return currentNode.getXtractor();
	}

	public int getCurrentIndex()
	{
		return currentIndex;
	}

	private class XecutorContextControl implements StiXecutorContextControl
	{
		@Override public StiXpressionNode setCurrentIndex(int index)
		{
			if (index < 0 || index >= nodes.length) throw new IllegalArgumentException();
			if (currentIndex != index) {
				currentIndex = index;
				currentNode = nodes[index];
			}
			return currentNode;
		}

		@Override public int getNodeCount()
		{
			return nodes.length;
		}

		@Override public StiXecutorContext getContext()
		{
			return XecutorContext.this;
		}

		@Override public int getCurrentIndex()
		{
			return currentIndex();
		}

		@Override public void resetContext()
		{
			if (runner == null) throw new NullPointerException();
			
			if (results == null) {
				results = new Object[nodes.length];
			} else {
				Arrays.fill(results, null);
			}
			
			currentIndex = 0;
			currentNode = nodes[0];
		}

		@Override public StiXtractor<?> getCurrentXtractor()
		{
			return currentNode.getXtractor();
		}

		@Override public Object getPublicValue(int xecutorIndex)
		{
			if (!runner.hasFinalState(xecutorIndex)) return null;
			return getValue(xecutorIndex);
		}

		@Override public void clearRange(int startIndex, int endIndex)
		{
			Arrays.fill(results, startIndex, endIndex, null);
		}

		@Override public boolean setValue(int xecutorIndex, Object value)
		{
			return XecutorContext.this.setValue(xecutorIndex, value);
		}

		@Override public Object getValue(int xecutorIndex)
		{
			return XecutorContext.this.getValue(xecutorIndex);
		}
	
		@Override public StiXpressionNode getNode(int xtractorIndex)
		{
			return nodes[xtractorIndex];
		}
	}

	@Override public int getNodeCount()
	{
		return nodes.length;
	}

	@Override public StiXpressionNode getNode(int xtractorIndex)
	{
		return nodes[xtractorIndex];
	}
}
