package org.cybcode.stix.xrc.pbuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.cybcode.stix.core.xource.StiXourceByTags;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

class PbufFieldValue implements StiXourceByTags.FieldValue<Integer>
{
	private static final Charset UFT8 = Charset.forName("UTF-8");
	
	public final int fieldId;
	public final int wireType;
	public final long rawLiteralValue;
	private CodedInputStream rawDelimValue;
	private byte[] binaryValue;

	public PbufFieldValue(int fieldId, int wireType, long rawLiteralValue)
	{
		this.fieldId = fieldId;
		this.wireType = wireType;
		this.rawLiteralValue = rawLiteralValue;
		this.rawDelimValue = null;
	}
	
	public PbufFieldValue(int fieldId, CodedInputStream rawDelimValue)
	{
		this.fieldId = fieldId;
		this.wireType = WireFormat.WIRETYPE_LENGTH_DELIMITED;
		this.rawLiteralValue = 0;
		this.rawDelimValue = rawDelimValue;
	}
	
	public boolean isWireType(int... types)
	{
		for (int t : types) {
			if (wireType == t) return true;
		}
		return false;
	}
	
	public int ensureWireType(int type)
	{
		if (wireType == type) return wireType;
		throw new IllegalArgumentException("Unexpected wire type: actual=" + wireType + ", expected=" + type);
	}

	public int ensureWireType(int... types)
	{
		if (isWireType(types)) return wireType;
		throw new IllegalArgumentException("Unexpected wire type: actual=" + wireType + ", expected=" + Arrays.toString(types));
	}

	@Override public PbufFieldValue enableMultipleUse()
	{
		if (binaryValue != null) return this;
		if (rawDelimValue == null) throw new IllegalStateException();
		CodedInputStream raw = rawDelimValue;
		rawDelimValue = null;
		try {
			binaryValue = raw.readRawBytes(raw.getBytesUntilLimit());
		} catch (IOException e) {
			throw new PBufIOException(e);
		}
		return this;
	}
	
	private CodedInputStream getAndResetRawStream()
	{
		if (rawDelimValue == null) throw new IllegalStateException();
		CodedInputStream result = rawDelimValue;
		rawDelimValue = null;
		return result;
	}

	CodedInputStream getRawStream()
	{
		if (binaryValue != null) return CodedInputStream.newInstance(binaryValue);
		return getAndResetRawStream();
	}

	public byte[] getBytes() throws IOException
	{
		if (binaryValue != null) return binaryValue.length == 0 ? binaryValue : binaryValue.clone();
		CodedInputStream in = getAndResetRawStream();
		return in.readRawBytes(in.getBytesUntilLimit());
	}
	
	public ByteBuffer getByteBuffer() throws IOException
	{
		return ByteBuffer.wrap(binaryValue != null ? binaryValue : getBytes()).asReadOnlyBuffer(); 
	}
	
	public String getString() throws IOException
	{
		return new String(binaryValue != null ? binaryValue : getBytes(), UFT8); 
	}
	
	public int fieldId()
	{
		return fieldId;
	}

	@Override public Integer fieldTag()
	{
		return fieldId;
	}
}