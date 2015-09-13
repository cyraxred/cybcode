package org.cybcode.stix.pbuf;

import java.io.IOException;
import java.util.Arrays;

import org.cybcode.stix.core.xource.StiXNumberedFieldXource;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

class PbufFieldValue implements StiXNumberedFieldXource.FieldValue
{
	public final int fieldId;
	public final int wireType;
	public final long rawLiteralValue;
	private CodedInputStream rawDelimValue;

	public PbufFieldValue(int fieldId, int wireType, long rawLiteralValue)
	{
		this.fieldId = fieldId;
		this.wireType = wireType;
		this.rawLiteralValue = rawLiteralValue;
		this.rawDelimValue = null;
	}
	
	public PbufFieldValue(int fieldId, CodedInputStream rawDelimValue, boolean multiUse)
	{
		this.fieldId = fieldId;
		this.wireType = WireFormat.WIRETYPE_LENGTH_DELIMITED;
		this.rawLiteralValue = 0;
		this.rawDelimValue = rawDelimValue;
		//TODO multi-use
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
	
	void discardDelimValue()
	{
		rawDelimValue = null;
	}
	
	CodedInputStream rawDelimStream()
	{
		if (rawDelimValue == null) throw new IllegalStateException();
		CodedInputStream result = rawDelimValue;
		rawDelimValue = null;
		return result;
	}

	@Override public int fieldId()
	{
		return fieldId;
	}

	public byte[] rawDelimBytes() throws IOException
	{
		CodedInputStream in = rawDelimStream();
		return in.readRawBytes(in.getBytesUntilLimit());
	}
}