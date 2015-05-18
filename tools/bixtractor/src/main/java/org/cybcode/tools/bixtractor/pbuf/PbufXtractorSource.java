package org.cybcode.tools.bixtractor.pbuf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.BiXource;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.BiXourceLink;
import org.cybcode.tools.bixtractor.core.AbstractBiXource;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

public class PbufXtractorSource extends AbstractBiXource<PbufContext> implements PbufXtractorFieldInfo
{
	private final Integer fieldId;

	public PbufXtractorSource(BiXtractor<Binary> source, boolean lazy)
	{
		super(source, lazy);
		if (((BiXtractor<?>) source) instanceof BiXource) throw new IllegalArgumentException();
		this.fieldId = 0;
	}

	public PbufXtractorSource(PbufXtractorSource source, int fieldId)
	{
		super(source, false);
		this.fieldId = fieldId; //TODO Arg.positive
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_SOURCE;
	}

	@Override protected boolean processValue(XecutionContext context, PbufContext sourceContext, Object value)
	{
		CodedInputStream in;
		try {
			in = prepareCodedStream(fieldId != 0, value);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Unsupported value for " + getClass().getSimpleName() + ": value=" + value.getClass().getName(), e);
		}
		
		return processCodedStream(context, sourceContext, in);
	}
	
	protected CodedInputStream prepareCodedStream(boolean chained, Object value) throws ClassCastException
	{
		if (fieldId == 0) return CodedInputStream.newInstance(((Binary) value).getBytes());
		return (CodedInputStream) value;
	}
	
	protected boolean processCodedStream(XecutionContext context, PbufContext sourceContext, CodedInputStream in)
	{
		int maxField = Integer.MAX_VALUE; // sourceContext.getMaxField();

		try {
			int tag;
			while ((tag = in.readTag()) != 0) {
				int fieldId = tag >> 3;
				if (fieldId > maxField) break;
				PbufFieldReceiver receiver = sourceContext.getFieldReceiver(fieldId);
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
						if (receiver.pushFieldValue(context, new PbufFieldValue(fieldId, in))) return true;
						in.skipRawBytes(in.getBytesUntilLimit());
						in.popLimit(oldLimit);
						continue;
					}
					default:
						in.skipField(tag);
						continue;
				}
				if (receiver.pushFieldValue(context, new PbufFieldValue(fieldId, wireType, rawValue))) return true;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return false;
	}

	@Override public PbufContext buildContext(BiXourceLink[] receivers)
	{
		if (receivers.length == 0) throw new IllegalArgumentException("Must have receivers: source=" + this);
		
		if (receivers.length == 1) {
			BiXourceLink receiver = receivers[0];
			int fieldId = validateReceiver(receiver);
			return new PbufSingletonContext(fieldId, new PbufStackedFieldReceiver(receiver));
		}
		
		int minFieldId = Integer.MAX_VALUE;
		int maxFieldId = Integer.MIN_VALUE;
		
		Map<Integer, PbufStackedFieldReceiver> receiversMap = new HashMap<>(receivers.length, 1f);
		for (BiXourceLink receiver : receivers) {
			int fieldId = validateReceiver(receiver);
			minFieldId = Math.min(minFieldId, fieldId);
			maxFieldId = Math.max(maxFieldId, fieldId);
			
			PbufStackedFieldReceiver fieldRecevier = new PbufStackedFieldReceiver(receiver);
			fieldRecevier.merge(receiversMap.put(fieldId, fieldRecevier));
		}
		
		int fieldRangeSize = maxFieldId - minFieldId + 1;
		if (receiversMap.size() < (fieldRangeSize >> 1)) {
			//field numbers are not compact, use map
			return new PbufMapContext(maxFieldId, receiversMap);
		}
		
		return new PbufArrayContext(minFieldId, maxFieldId, receiversMap);
	}

	private int validateReceiver(BiXourceLink receiverLink)
	{
		BiXtractor<?> receiver = receiverLink.getReceiver();
		
		if (!(receiver instanceof PbufXtractorFieldInfo)) {
			throw new IllegalArgumentException("Must implement PbufXtractorField: receiver=" + receiver);
		}
		
		PbufXtractorFieldInfo fieldInfo = (PbufXtractorFieldInfo) receiver;
		int fieldId = fieldInfo.fieldId();
		if (fieldId > 0) return fieldId;
		
		throw new IllegalArgumentException("Invalid field id: id=" + fieldId + ", receiver=" + receiver);
	}

	@Override public Object getOperationToken()
	{
		return fieldId;
	}

	@Override public int fieldId()
	{
		return fieldId;
	}
}

