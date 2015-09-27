package org.cybcode.stix.xrc.pbuf;

import java.io.IOException;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXecutorStatsCollector;
import org.cybcode.stix.api.StiXource;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xource.StiXourceByIntTags;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

public class PbufXource<S> extends StiXourceByIntTags<S, PbufFieldValue>
{
	public PbufXource(StiXource<?, ?, Integer, PbufFieldValue> p0, int fieldId)
	{
		super(p0, fieldId);
	}

	public PbufXource(StiXtractor<? extends S> p0, StiXFunction<? super S, PbufFieldValue> fn)
	{
		super(p0, fn);
	}

	@Override protected PbufFieldValue processNestedFields(StiXecutorPushContext context, FieldContainer<Integer, PbufFieldValue> container, 
		Settings settings, final PbufFieldValue pushedValue)
	{
		if (container == null) return pushedValue;
		StiXecutorStatsCollector stats = settings.getStatsCollector();

		//TODO silent fail mode
		pushedValue.ensureWireType(WireFormat.WIRETYPE_LENGTH_DELIMITED);
		final PbufFieldValue value = settings.hasPushTargets() ? pushedValue.enableMultipleUse() : pushedValue;
		
		int maxField = settings.hasSortedFields() && container.getMaxFieldTag() != null ? container.getMaxFieldTag() : Integer.MAX_VALUE;
		CodedInputStream in = value.getRawStream();

		try {
			do {
				int tag = in.readTag(); 
				if (tag == 0) return value;
					
				int fieldId = tag >> 3;
				if (fieldId > maxField) break;
				FieldHandler<PbufFieldValue> handler = container.findFieldHandler(fieldId);
				if (handler == null) {
					in.skipField(tag);
					stats.onFieldSkipped();
					continue;
				}
				stats.onFieldParsed();
				
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
			} while (!context.hasFrameFinalState());
			return null;
		} catch (IOException e) {
			throw new PBufIOException(e);
		}
	}

	@Override public Class<PbufFieldValue> resultType()
	{
		return PbufFieldValue.class;
	}
}

