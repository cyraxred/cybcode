package org.cybcode.stix.core.xecutors;

public interface StiXecutorStatsCollector
{
	public static StiXecutorStatsCollector NULL = new StiXecutorStatsCollector() 
	{
		@Override public void onPushEvaluated() {}
		@Override public void onPushAttempt() {}
		@Override public void onEvaluated() {}
		@Override public void resetStats(int nodeCount) {}
		@Override public void onFieldSkipped() {}
		@Override public void onFieldParsed() {}
	};

	void onPushEvaluated();
	void onPushAttempt();
	void onEvaluated();
	void resetStats(int nodeCount);
	void onFieldSkipped();
	void onFieldParsed();
}
