package org.cybcode.stix.core.xecutors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cybcode.stix.api.StiXecutorContextInspector;
import org.cybcode.stix.api.StiXecutorStatsCollector;
import org.cybcode.stix.api.StiXpressionNode;
import org.cybcode.stix.api.StiXpressionSequencer;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

public class XpressionRunnerBuilder
{
	private StiXecutorStatsCollector stats = StiXecutorStatsCollector.NULL;

	protected AbstractXecutorContextStorage createContextStorage(StiXpressionNode[] nodes)
	{
		return new XecutorContextBoxedStorage(nodes.length);
	}
	
	protected Collection<? extends XecutorContextNode> createContextNodes(StiXpressionNode[] nodes)
	{
		AbstractXecutorContextStorage storage = createContextStorage(nodes);
		List<XecutorContextNode> result = new ArrayList<>(nodes.length);
		for (StiXpressionNode node : nodes) {
			result.add(new XecutorContextNode(node, storage));
		}
		return result;
	}
	
	protected Supplier<StiXpressionSequencer> createSequencerSupplier()
	{
		return new Supplier<StiXpressionSequencer>() 
		{
			@Override public StiXpressionSequencer get() { return new SimpleXpressionSequencer(); }
		};
	}
	
	protected XecutorContextRunner createRunner(Collection<? extends XecutorContextNode> contextNodes, Supplier<StiXpressionSequencer> sequencerSupplier)
	{
		XecutorContextRunner result = new XecutorContextRunner(contextNodes, sequencerSupplier, stats);
		return result;
	}

	public StiXecutorStatsCollector getStats()
	{
		return stats;
	}

	public XpressionRunnerBuilder setStats(StiXecutorStatsCollector stats)
	{
		this.stats = stats;
		return this;
	}

	public XecutorContextRunner createRunner(StiXpressionNode[] nodes)
	{
		Collection<? extends XecutorContextNode> contextNodes = createContextNodes(nodes);
		Supplier<StiXpressionSequencer> sequencerSupplier = createSequencerSupplier();
		return createRunner(contextNodes, sequencerSupplier);
	}

	public XecutorContextBuilder<StiXecutorContextInspector> createContextBuilder()
	{
		return new XecutorContextBuilder<StiXecutorContextInspector>() 
		{
			@Override protected StiXecutorContextInspector build(StiXpressionNode[] nodes)
			{
				return createRunner(nodes);
			}
		};
	}

	public XecutorContextBuilder<Function<Object, Object>> createRunnerBuilder()
	{
		return new XecutorContextBuilder<Function<Object,Object>>() 
		{
			@Override protected Function<Object, Object> build(StiXpressionNode[] nodes)
			{
				return createRunner(nodes);
			}
		};
	}
}