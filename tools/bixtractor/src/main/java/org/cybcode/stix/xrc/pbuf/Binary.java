package org.cybcode.stix.xrc.pbuf;

public class Binary
{
	private final byte[] bytes;

	public Binary(byte[] bytes)
	{
		this.bytes = bytes;
	}

	public byte[] getBytes()
	{
		return bytes;
	}
}
