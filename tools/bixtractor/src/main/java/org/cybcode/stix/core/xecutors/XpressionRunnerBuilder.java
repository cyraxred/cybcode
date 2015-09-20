package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;

import com.google.common.base.Function;

public class XpressionRunnerBuilder
{
	protected interface Runner extends Function<Object, Object>
	{
		boolean hasFrameResultValue();

		boolean executePostponedTargetsBefore(StiXpressionNode stiXpressionNode);
		boolean executeImmediateTargets();
		
		void setPushValueOf(int targetIndex, List<PushTarget> pushTargets, Object finalValue);
		void setFinalValueOf(int xtractorIndex, List<PushTarget> notifyTargets);
		
		void enterFrame();
	}

	protected interface Context
	{
		int getNodeCount();

		void resetContext(Object rootValue, Runner xecutorContextRunner);
		void resetFrameContent(int rootIndex, int resultIndex);
		void finalizeFrameContent(int startIndex);

		StiXpressionNode setCurrentIndex(int xtractorIndex);
		void evaluateFinalState(int xtractorIndex);
		boolean evaluatePush(PushTarget pushTarget);

		Object getPublicValue(int xecutorIndex);
		void setStatsCollector(StiXecutorStatsCollector stats);
	}

	private StiXecutorStatsCollector stats = StiXecutorStatsCollector.NULL;

	protected Context createContext(StiXpressionNode[] nodes)
	{
		return new XecutorContext(nodes);
	}
	
	protected StiXpressionSequencer createSequencer()
	{
		return new SimpleXpressionSequencer();
	}
	
	protected Runner createRunner(Context context, StiXpressionSequencer sequencer)
	{
		return new XecutorContextRunner(context, sequencer);
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
		Context context = createContext(nodes);
		context.setStatsCollector(stats);
		StiXpressionSequencer sequencer = createSequencer();
		return createRunner(context, sequencer);
	}

	public XecutorContextBuilder<Function<Object, Object>> createBuilder()
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