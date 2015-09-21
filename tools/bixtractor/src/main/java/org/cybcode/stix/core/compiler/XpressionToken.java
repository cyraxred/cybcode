package org.cybcode.stix.core.compiler;

import java.util.Arrays;

import org.cybcode.stix.api.StiXtractor;

public class XpressionToken
{
	private static final int[]	EMPTY_ARG_LIST	= new int[0];
	
	private final Class<?> operationClass;
	private final Object operationToken;
	private final int arg0;
	private final int[] args;
	
	public XpressionToken(Class<?> operationClass, Object operationToken, int arg0)
	{
		this.operationClass = operationClass;
		this.operationToken = operationToken;
		this.arg0 = arg0;
		this.args = null;
	}
	
	public XpressionToken(Class<?> operationClass, Object operationToken, int... args)
	{
		this.operationClass = operationClass;
		this.operationToken = operationToken;
		if (args == null || args.length == 0) {
			this.args = EMPTY_ARG_LIST;
			arg0 = 0;
		} else if (args.length == 1) {
			arg0 = args[0];
			this.args = null;
		} else {
			this.args = args;
			arg0 = 0;
		}
	}

	public XpressionToken(StiXtractor<?> operation, int... args)
	{
		this(operation.getClass(), operation.getOperationToken(), args);
		if (operation.paramCount() != getParamCount()) throw new IllegalArgumentException();
	}

	public XpressionToken(StiXtractor<?> operation, int arg0)
	{
		this(operation.getClass(), operation.getOperationToken(), arg0);
//		if (operation.paramCount() != getParamCount()) throw new IllegalArgumentException();
	}

	public XpressionToken(StiXtractor<?> operation)
	{
		this(operation, EMPTY_ARG_LIST);
	}
	
	public int getParamCount()
	{
		if (args == null) return 1;
		return args.length;
	}

	@Override public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + arg0;
		result = prime * result + ((operationClass == null) ? 0 : operationClass.hashCode());
		result = prime * result + Arrays.hashCode(args);
		result = prime * result + ((operationToken == null) ? 0 : operationToken.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		XpressionToken other = (XpressionToken) obj;
		if (arg0 != other.arg0) return false;
		if (operationClass != other.operationClass) return false;
		
		if (!Arrays.equals(args, other.args)) return false;

		if (operationToken == null) {
			if (other.operationToken != null) return false;
		} else if (!operationToken.equals(other.operationToken)) return false;
		
		return true;
	}

}