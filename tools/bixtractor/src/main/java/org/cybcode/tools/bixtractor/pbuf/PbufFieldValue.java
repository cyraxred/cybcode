package org.cybcode.tools.bixtractor.pbuf;

import java.util.Arrays;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

class PbufFieldValue
{
	public final int fieldId;
	public final int wireType;
	public final long rawLiteralValue;
	public final CodedInputStream rawDelimValue;

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
	
	public int ensureWireType(int... types)
	{
		if (isWireType(types)) return wireType;
		throw new IllegalArgumentException("Unexpected wire type: actual=" + wireType + ", expected=" + Arrays.toString(types));
	}
}