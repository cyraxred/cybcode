package org.cybcode.stix.core.xecutors;

public class XecutorFailException extends RuntimeException
{
	private static final long	serialVersionUID	= -4430663456026699281L;

	public XecutorFailException() {}

	public XecutorFailException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public XecutorFailException(String message)
	{
		super(message);
	}

	public XecutorFailException(Throwable cause)
	{
		super(cause);
	}
}