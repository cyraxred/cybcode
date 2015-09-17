package org.cybcode.stix.xrc.pbuf;

import org.cybcode.stix.api.StiXFunction;

import com.google.protobuf.WireFormat;

public class PbufFields 
{
	private PbufFields() {}
	
	public static StiXFunction<PbufFieldValue, Long> INT = new Fn<Long>("INT")
	{
		@Override public Long apply(PbufFieldValue input)
		{
			input.ensureWireType(WireFormat.WIRETYPE_FIXED32, WireFormat.WIRETYPE_FIXED64, WireFormat.WIRETYPE_VARINT);
			return input.rawLiteralValue;
		}

		@Override public Class<Long> resultType() { return Long.class; }
	};
	
	public static StiXFunction<PbufFieldValue, Double> FLOAT = new Fn<Double>("FLOAT")
	{
		@Override public Double apply(PbufFieldValue input)
		{
			if (input.ensureWireType(WireFormat.WIRETYPE_FIXED32, WireFormat.WIRETYPE_FIXED64) == WireFormat.WIRETYPE_FIXED32) {
				return (double) Float.intBitsToFloat((int) input.rawLiteralValue);
			} else {
				return Double.longBitsToDouble(input.rawLiteralValue);
			}
		}

		@Override public Class<Double> resultType() { return Double.class; }
	};
	
	public static StiXFunction<PbufFieldValue, String> STR = new Fn<String>("STR")
	{
		@Override public String apply(PbufFieldValue input)
		{
			input.ensureWireType(WireFormat.WIRETYPE_LENGTH_DELIMITED);
			return input.getString();
		}

		@Override public Class<String> resultType() { return String.class; }
	};
	
	private static abstract class Fn<T> implements StiXFunction<PbufFieldValue, T>
	{
		private final String name;
		
		Fn(String name) { this.name = name; }
		@Override public Object getOperationToken() { return this; }
		@Override public int getOperationComplexity() { return 10; }
		@Override public abstract Class<T> resultType();
		@Override public String toString() { return name; }
	}
}
