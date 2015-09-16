package org.cybcode.stix.core.xource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXecutorCallback;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXource;
import org.cybcode.stix.api.StiXtractor;

public abstract class StiXourceByTags<S, C extends StiXourceByTags.FieldContainer<FieldTag, V>, FieldTag, V extends StiXourceByTags.FieldValue<? extends FieldTag>> 
	extends StiXource<S, C, FieldTag, V>
{
	public interface FieldHandler<V>
	{
		void process(StiXecutorContext context, V value);
		boolean isMultiple();
	}

	public interface FieldContainer<FieldTag, V>
	{
		FieldHandler<V> findFieldHandler(FieldTag fieldId);
		FieldTag getMaxFieldTag();
	}
	
	public interface FieldValue<FieldTag>
	{
		FieldTag fieldTag();
		FieldValue<FieldTag> enableMultipleUse();
	}
	
	public StiXourceByTags(StiXource<?, ?, FieldTag, V> p0, FieldTag fieldDetails, ValueLimit limitMode)
	{
		super(p0, fieldDetails, limitMode);
	}

	public StiXourceByTags(StiXtractor<? extends S> p0, StiXFunction<? super S, V> fn, ValueLimit limitMode)
	{
		super(p0, fn, limitMode);
	}

	@Override protected C createFieldContainer(List<StiXecutorCallback> callbacks, Settings settings)
	{
		if (callbacks.isEmpty()) {
			if (!settings.hasPushTargets()) throw new IllegalArgumentException("Must have fields: source=" + this);
			return createFieldContainer(Collections.<FieldTag, SingleFieldHandler<V>>emptyMap(), settings);
		}
		
		Map<FieldTag, StackedFieldHandler<V>> callbackMap = new HashMap<>(callbacks.size(), 1f);
		
		for (StiXecutorCallback callback : callbacks) {
			StackedFieldHandler<V> handler = new StackedFieldHandler<V>(callback);
			@SuppressWarnings("unchecked") FieldTag fieldTag = (FieldTag) handler.getFieldDetails();
			handler.merge(callbackMap.put(fieldTag, handler));
		}
		
		return createFieldContainer(callbackMap, settings);
	}

	protected abstract C createFieldContainer(Map<FieldTag, ? extends SingleFieldHandler<V>> callbackMap, Settings settings);
}