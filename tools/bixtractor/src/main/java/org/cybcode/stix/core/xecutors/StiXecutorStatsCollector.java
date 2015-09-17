package org.cybcode.stix.core.xecutors;

public interface StiXecutorStatsCollector
{
	public static StiXecutorStatsCollector NULL = new StiXecutorStatsCollector() 
	{
		@Override public void onPushEvaluate() {}
		@Override public void onPushAttempt() {}
		@Override public void onEvaluate() {}
		@Override public void resetStats(int nodeCount) {}
		@Override public void onFieldSkipped() {}
		@Override public void onFieldParsed() {}
	};

	void onPushEvaluate();
	void onPushAttempt();
	void onEvaluate();
	void resetStats(int nodeCount);
	void onFieldSkipped();
	void onFieldParsed();
}
