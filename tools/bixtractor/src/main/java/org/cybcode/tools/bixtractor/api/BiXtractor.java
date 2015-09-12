package org.cybcode.tools.bixtractor.api;

public interface BiXtractor<T>
{
	public static final int COMPLEXITY_CONSTANT = 1;
	public static final int COMPLEXITY_VAR = 2;
	public static final int COMPLEXITY_JUNCTION	= 5;
	public static final int COMPLEXITY_OPERATION = 10;
	public static final int COMPLEXITY_SOURCE = 1000;
	
	interface NoPush {}
	interface Repeatable 
	{
		boolean isRepeated();
	}
	
	T evaluate(XecutionContext context);
	void visit(XpressionRegistrator visitor);
	Object getOperationToken();
	int getOperationComplexity();
}