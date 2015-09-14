package org.cybcode.stix.core.xource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXource;
import org.cybcode.stix.api.StiXourceNestedXecutor;
import org.cybcode.stix.api.StiXtractor;

public abstract class StiXNumberedFieldXource<S, V extends StiXNumberedFieldXource.FieldValue> extends StiXource<S, StiXNumberedFieldXource.FieldContainer, Integer, V>
{
	public interface FieldHandler
	{
		boolean isMultiple();
		void process(StiXecutorContext context, FieldValue value);
	}

	public interface FieldContainer
	{
		FieldHandler findFieldHandler(int fieldId);
		int getMaxFieldId();
	}
	
	public interface FieldValue
	{
		int fieldId();
	}
	
	protected final Integer fieldId;

	public StiXNumberedFieldXource(StiXtractor<? extends S> p0, boolean repeatable)
	{
		super(p0, repeatable);
		this.fieldId = null;
	}
	
	public StiXNumberedFieldXource(StiXource<?, ?, Integer, V> p0, boolean repeatable, int fieldId)
	{
		super(p0, repeatable);
		this.fieldId = fieldId;
	}

	@Override protected FieldContainer createFieldContainer(List<StiXourceNestedXecutor<Integer>> receivers)
	{
		switch (receivers.size()) {
			case 0: 
				throw new IllegalArgumentException("Must have receivers: source=" + this);
			case 1: {
				StiXourceNestedXecutor<Integer> receiver = receivers.get(0);
				return new SingletonFieldHandler(receiver);
			}
				
		}
		
		int minFieldId = Integer.MAX_VALUE;
		int maxFieldId = Integer.MIN_VALUE;
		
		Map<Integer, StackedFieldValueHandler> receiversMap = new HashMap<>(receivers.size(), 1f);
		for (StiXourceNestedXecutor<Integer> receiver : receivers) {
			int fieldId = validateReceiver(receiver);
			minFieldId = Math.min(minFieldId, fieldId);
			maxFieldId = Math.max(maxFieldId, fieldId);
			
			StackedFieldValueHandler fieldRecevier = new StackedFieldValueHandler(receiver);
			fieldRecevier.merge(receiversMap.put(fieldId, fieldRecevier));
		}
		
		int fieldRangeSize = maxFieldId - minFieldId + 1;
		if (receiversMap.size() < (fieldRangeSize >> 1)) {
			//field numbers are not compact, use map
			return new MappedFieldHandler(maxFieldId, receiversMap);
		}
		
		return new ArrayFieldHandler(minFieldId, maxFieldId, receiversMap);
	}
	
	private int validateReceiver(StiXourceNestedXecutor<Integer> receiver)
	{
		Integer fieldId = receiver.getFieldDetails();
		if (fieldId == null || fieldId <= 0) throw new IllegalArgumentException("Invalid field id: id=" + fieldId + ", receiver=" + receiver); 

		return fieldId;
	}
	
	@Override protected Integer getFieldDetails()
	{
		return fieldId;
	}
	
	public boolean isRoot()
	{
		return fieldId == null;
	}
	
	public int fieldId()
	{
		return fieldId;
	}
}

