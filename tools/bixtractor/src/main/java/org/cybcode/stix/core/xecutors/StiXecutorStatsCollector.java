package org.cybcode.stix.core.xecutors;

import org.cybcode.stix.api.StiXtractor.Parameter;

public interface StiXecutorStatsCollector
{
	public static StiXecutorStatsCollector NULL = new StiXecutorStatsCollector() 
	{
		@Override public void onPushEvaluated(StiXpressionNode node, Object value) {}
		@Override public void onPushAttempt(StiXpressionNode node, Parameter<?> param, Object pushedValue) {}
		@Override public void onEvaluated(StiXpressionNode node, Object value) {}
		@Override public void resetStats(int nodeCount) {}
		@Override public void onFieldSkipped() {}
		@Override public void onFieldParsed() {}
	};

	void onPushEvaluated(StiXpressionNode node, Object value);
	void onPushAttempt(StiXpressionNode node, Parameter<?> param, Object pushedValue);
	void onEvaluated(StiXpressionNode node, Object value);
	void resetStats(int nodeCount);
	void onFieldSkipped();
	void onFieldParsed();
}
