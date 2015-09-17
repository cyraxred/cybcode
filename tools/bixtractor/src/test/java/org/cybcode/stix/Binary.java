package org.cybcode.stix;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.xrc.pbuf.PbufFieldValue;

import com.google.protobuf.CodedInputStream;

public class Binary
{
	public static StiXFunction<Binary, PbufFieldValue> PREPARE = new StiXFunction<Binary, PbufFieldValue>()
	{
		@Override public PbufFieldValue apply(Binary input)
		{
			return new PbufFieldValue(0, CodedInputStream.newInstance(input.bytes));
		}

		@Override public Object getOperationToken() { return this; }
		@Override public int getOperationComplexity() { return 1000; }
		@Override public Class<PbufFieldValue> resultType() { return PbufFieldValue.class; }
	};
	
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
