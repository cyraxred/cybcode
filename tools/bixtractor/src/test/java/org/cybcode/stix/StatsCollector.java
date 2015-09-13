package org.cybcode.stix;

import org.cybcode.stix.core.xecutors.StiXecutorStatsCollector;

class StatsCollector extends StiXecutorStatsCollector
{
	public int evaluateCount;
	public int pushAttemptCount;
	public int pushEvaluateCount;
	public int nodeCount;
	
	@Override public void onEvaluate()
	{
		evaluateCount++;
	}
	
	@Override public void onPushAttempt()
	{
		pushAttemptCount++;
	}
	
	@Override public void onPushEvaluate()
	{
		pushEvaluateCount++;
	}
	
	@Override public void resetStats(int nodeCount)
	{
		this.nodeCount = nodeCount;
		evaluateCount = 0;
		pushAttemptCount = 0;
		pushEvaluateCount = 0;
	}
}