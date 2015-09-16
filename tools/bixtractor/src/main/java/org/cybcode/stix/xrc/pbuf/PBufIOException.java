package org.cybcode.stix.xrc.pbuf;

import java.io.IOException;

public class PBufIOException extends RuntimeException
{
	private static final long	serialVersionUID	= 3709728697205809143L;

	public PBufIOException(IOException cause) { super(cause); }
}
