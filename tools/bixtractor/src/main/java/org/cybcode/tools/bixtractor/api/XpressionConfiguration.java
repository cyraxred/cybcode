package org.cybcode.tools.bixtractor.api;

public class XpressionConfiguration
{
	private static final XpressionConfiguration	DEFAULT	= new XpressionConfiguration(true, 2);
	private static final XpressionConfiguration	SAFE = new XpressionConfiguration(false, 0);
	
	private final boolean enableEarlyCompletion;
	private final int maxEarlyCompletionArguments;
	
	public static XpressionConfiguration getDefault()
	{
		return DEFAULT;
	}
	
	public static XpressionConfiguration getSafe()
	{
		return SAFE;
	}
	
	public XpressionConfiguration(boolean enableEarlyCompletion, int maxEarlyCompletionArguments)
	{
		this.enableEarlyCompletion = enableEarlyCompletion;
		this.maxEarlyCompletionArguments = maxEarlyCompletionArguments;
	}

	public boolean isEnableEarlyCompletion()
	{
		return enableEarlyCompletion;
	}
	
	public int getMaxEarlyCompletionArguments()
	{
		return maxEarlyCompletionArguments;
	}
}