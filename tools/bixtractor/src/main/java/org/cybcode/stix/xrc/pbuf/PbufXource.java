package org.cybcode.stix.xrc.pbuf;

import java.io.IOException;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xource.StiXourceByIntTags;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

public class PbufXource extends StiXourceByIntTags<Binary, PbufFieldValue>
{

	public PbufXource(StiXourceByIntTags<Binary, PbufFieldValue> p0, int fieldId, ValueLimit limitMode)
	{
		super(p0, fieldId, limitMode);
	}

	public PbufXource(StiXtractor<? extends Binary> p0, StiXFunction<? super Binary, PbufFieldValue> fn, ValueLimit limitMode)
	{
		super(p0, fn, limitMode);
	}

//	@Override protected PbufFieldValue prepareValue(Binary value)
//	{
//		return new PbufFieldValue(fieldId == null ? 0 : fieldId, CodedInputStream.newInstance(value.getBytes()), false);
//	}

	@Override protected PbufFieldValue processNestedFields(StiXecutorContext context, FieldContainer<Integer, PbufFieldValue> container, 
		Settings settings, final PbufFieldValue pushedValue)
	{
		if (container == null) return pushedValue;

		//TODO silent mode
		pushedValue.ensureWireType(WireFormat.WIRETYPE_LENGTH_DELIMITED);
		final PbufFieldValue value = settings.hasPushTargets() ? pushedValue.enableMultipleUse() : pushedValue;
		
		int maxField = settings.hasSortedFields() && container.getMaxFieldTag() != null ? container.getMaxFieldTag() : Integer.MAX_VALUE;
		CodedInputStream in = value.getRawStream();

		try {
			int tag;
			while ((tag = in.readTag()) != 0) {
				int fieldId = tag >> 3;
				if (fieldId > maxField) break;
				FieldHandler<PbufFieldValue> handler = container.findFieldHandler(fieldId);
				if (handler == null) {
					in.skipField(tag);
					continue;
				}
				
				int wireType = tag & 7;
				long rawValue;
				switch (wireType) {
					case WireFormat.WIRETYPE_VARINT:
						rawValue = in.readRawVarint64();
						break;
					case WireFormat.WIRETYPE_FIXED32:
						rawValue = in.readRawLittleEndian32();
						break;
					case WireFormat.WIRETYPE_FIXED64:
						rawValue = in.readRawLittleEndian64();
						break;
					case WireFormat.WIRETYPE_LENGTH_DELIMITED: {
						int oldLimit = in.pushLimit(in.readRawVarint32());
						
						try {
							PbufFieldValue fieldValue = new PbufFieldValue(fieldId, in);
							if (handler.isMultiple()) {
								fieldValue = fieldValue.enableMultipleUse();
							}
							handler.process(context, fieldValue);
						} finally {
							in.skipRawBytes(in.getBytesUntilLimit());
							in.popLimit(oldLimit);
						}
						continue;
					}
					default:
						in.skipField(tag);
						continue;
				}
				PbufFieldValue fieldValue = new PbufFieldValue(fieldId, wireType, rawValue);
				if (handler.isMultiple()) {
					fieldValue = fieldValue.enableMultipleUse();
				}
				handler.process(context, fieldValue);
			} while (!context.hasResultValue());
		} catch (IOException e) {
			throw new PBufIOException(e);
		}
		
		return value;
	}

	@Override public Class<PbufFieldValue> resultType()
	{
		return PbufFieldValue.class;
	}
}

