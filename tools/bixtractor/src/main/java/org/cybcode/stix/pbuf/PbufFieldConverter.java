package org.cybcode.stix.pbuf;

import java.io.IOException;

import com.google.common.base.Function;
import com.google.protobuf.WireFormat;

public enum PbufFieldConverter implements Function<PbufFieldValue, Object> 
{
	STRING {
		@Override public String apply(PbufFieldValue input)
		{
			input.ensureWireType(WireFormat.WIRETYPE_LENGTH_DELIMITED);
			try {
				return new String(input.rawDelimBytes(), "UTF-8");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	},
	DOUBLE {
		@Override public Double apply(PbufFieldValue input)
		{
			if (input.ensureWireType(WireFormat.WIRETYPE_FIXED32, WireFormat.WIRETYPE_FIXED64) == WireFormat.WIRETYPE_FIXED32) {
				return (double) Float.intBitsToFloat((int) input.rawLiteralValue);
			} else {
				return Double.longBitsToDouble(input.rawLiteralValue);
			}
		}
	},
}
