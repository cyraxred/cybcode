package org.cybcode.stix.xrc.pbuf;

import java.io.IOException;

import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXource;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xource.StiXNumberedFieldXource;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

public class PbufXource extends StiXNumberedFieldXource<Binary, PbufFieldValue>
{
	public PbufXource(StiXtractor<? extends Binary> p0, boolean repeatable)
	{
		super(p0, repeatable);
	}
	
	public PbufXource(StiXource<?, ?, Integer, PbufFieldValue> p0, int fieldId, boolean repeatable)
	{
		super(p0, repeatable, fieldId);
		if (fieldId <= 0) throw new IllegalArgumentException();
	}

	@Override protected PbufFieldValue prepareValue(Binary value)
	{
		return new PbufFieldValue(fieldId == null ? 0 : fieldId, CodedInputStream.newInstance(value.getBytes()), false);
	}

	@Override protected Integer getFieldDetails()
	{
		return fieldId;
	}
	
	public int fieldId()
	{
		return fieldId;
	}
	
	protected boolean hasSortedFields(FieldContainer container)
	{
		return false;
	}

	@Override protected void process(StiXecutorContext context, FieldContainer container, final PbufFieldValue value)
	{
		value.ensureWireType(WireFormat.WIRETYPE_LENGTH_DELIMITED);
		CodedInputStream in = value.rawDelimStream();
		container.getMaxFieldId();
		int maxField = hasSortedFields(container) ? Integer.MAX_VALUE : container.getMaxFieldId();

		try {
			int tag;
			while ((tag = in.readTag()) != 0) {
				int fieldId = tag >> 3;
				if (fieldId > maxField) break;
				FieldHandler receiver = container.findFieldHandler(fieldId);
				if (receiver == null) {
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
						
						PbufFieldValue fieldValue = new PbufFieldValue(fieldId, in, receiver.isMultiple());
						receiver.process(context, fieldValue);
						fieldValue.discardDelimValue();
						
						in.skipRawBytes(in.getBytesUntilLimit());
						in.popLimit(oldLimit);
						continue;
					}
					default:
						in.skipField(tag);
						continue;
				}
				PbufFieldValue fieldValue = new PbufFieldValue(fieldId, wireType, rawValue);
				receiver.process(context, fieldValue);
			} while (context.hasResultValue());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override public Class<PbufFieldValue> resultType()
	{
		return PbufFieldValue.class;
	}
}

