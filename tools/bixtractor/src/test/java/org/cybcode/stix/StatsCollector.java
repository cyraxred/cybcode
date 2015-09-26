package org.cybcode.stix;

import org.cybcode.stix.api.StiXtractor.Parameter;
import org.cybcode.stix.core.xecutors.StiXecutorStatsCollector;
import org.cybcode.stix.core.xecutors.StiXpressionNode;

class StatsCollector implements StiXecutorStatsCollector
{
	public int evaluateCount;
	public int pushAttemptCount;
	public int pushEvaluateCount;
	public int nodeCount;
	public int fieldCount;
	public int frameEntered;
	public int frameSkipped;
	public int frameResult;
	public int frameCompleted;
	
	@Override public void resetStats(int nodeCount)
	{
		this.nodeCount = nodeCount;
		evaluateCount = 0;
		pushAttemptCount = 0;
		pushEvaluateCount = 0;
		fieldCount = 0;
		frameEntered = 0;
		frameSkipped = 0;
		frameResult = 0;
		frameCompleted = 0;
	}

	@Override public void onFieldSkipped()
	{
		fieldCount++;
	}

	@Override public void onFieldParsed()
	{
		fieldCount++;
	}

	@Override public void onEvaluated(StiXpressionNode node, Object value)
	{
		System.out.println("EVAL: " + node + " = " + value);
		evaluateCount++;
	}

	@Override public void onPushEvaluated(StiXpressionNode node, Object value)
	{
		System.out.println("POUT: " + node + " = " + value);
		pushEvaluateCount++;
	}

	@Override public void onPushAttempt(StiXpressionNode node, Parameter<?> param, Object pushedValue)
	{
		System.out.println("P_IN: " + param.toNameString() + ":" + pushedValue + " >>= " + node);
		pushAttemptCount++;
	}

	@Override public void onFrameEnter(int startIndex)
	{
		System.out.println("ENTR: " + startIndex);
		frameEntered++;
	}

	@Override public void onFrameSkip(int startIndex)
	{
		System.out.println("SKIP: " + startIndex);
		frameSkipped++;
	}

	@Override public void onFrameResolve(int startIndex, boolean completed)
	{
		frameResult++;
		if (!completed) {
			System.out.println("EXIT: " + startIndex);
			return;
		} else {
			System.out.println("PART: " + startIndex);
		}
		frameCompleted++;
	}
}