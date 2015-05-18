package org.cybcode.tools.bixtractor.api;

public interface BiXtractor<T>
{
	public static final int COMPLEXITY_CONSTANT = 1;
	public static final int COMPLEXITY_VERBATIM = 2;
	public static final int COMPLEXITY_OPERATION = 10;
	public static final int COMPLEXITY_SOURCE = 1000;
	
	interface NoPush {}
	
	T evaluate(XecutionContext context);
	void visit(XpressionRegistrator visitor);
	Object getOperationToken();
	int getOperationComplexity();
}