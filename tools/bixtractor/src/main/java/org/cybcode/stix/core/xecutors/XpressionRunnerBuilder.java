package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXecutorContextBinder;

import com.google.common.base.Function;

public class XpressionRunnerBuilder
{
	public interface Runner extends Function<Object, Object>
	{
		boolean hasFinalState(int xtractorIndex);
		boolean hasFrameFinalState();
	}

	private StiXecutorStatsCollector stats = StiXecutorStatsCollector.NULL;

	protected StiXecutorContextBinder createContext(StiXpressionNode[] nodes)
	{
		return new XecutorContext(nodes);
	}
	
	protected StiXpressionSequencer createSequencer()
	{
		return new SimpleXpressionSequencer();
	}
	
	protected Runner createRunner(StiXecutorContextBinder context, StiXpressionSequencer sequencer)
	{
		XecutorContextRunner result = new XecutorContextRunner(context, sequencer);
		result.setStatsCollector(stats);
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

	public Function<Object, Object> createRunner(StiXpressionNode[] nodes)
	{
		StiXecutorContextBinder context = createContext(nodes);
		StiXpressionSequencer sequencer = createSequencer();
		Runner runner = createRunner(context, sequencer);
		return runner;
	}

	public XecutorContextBuilder<StiXecutorContextInspector> createContextBuilder()
	{
		return new XecutorContextBuilder<StiXecutorContextInspector>() 
		{
			@Override protected StiXecutorContextInspector build(StiXpressionNode[] nodes)
			{
				return createContext(nodes);
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