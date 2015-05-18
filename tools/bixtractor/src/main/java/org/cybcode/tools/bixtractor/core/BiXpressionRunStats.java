package org.cybcode.tools.bixtractor.core;

public class BiXpressionRunStats
{
	private int totalNodeCount;
	private int evaluatedNodeCount;
	
	public int getTotalNodeCount()
	{
		return totalNodeCount;
	}
	
	public void setTotalNodeCount(int totalNodeCount)
	{
		this.totalNodeCount = totalNodeCount;
	}
	
	public int getEvaluatedNodeCount()
	{
		return evaluatedNodeCount;
	}

	public void setEvaluatedNodeCount(int evaluatedNodeCount)
	{
		this.evaluatedNodeCount = evaluatedNodeCount;
	}
}