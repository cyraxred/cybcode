package org.cybcode.stix.core.xecutors;

public class StiXecutorStatsCollector
{
	private static StiXecutorStatsCollector INSTANCE = new StiXecutorStatsCollector();
	
	public static StiXecutorStatsCollector getInstance() { return INSTANCE; }

	public void onPushEvaluate() {}
	public void onPushAttempt() {}
	public void onEvaluate() {}
	public void resetStats(int nodeCount) {}
}
