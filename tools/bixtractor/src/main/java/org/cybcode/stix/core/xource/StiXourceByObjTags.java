package org.cybcode.stix.core.xource;

import java.util.Map;
import java.util.Set;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXtractor;

public abstract class StiXourceByObjTags<S, F, V extends StiXourceByTags.FieldValue<F>> 
	extends StiXourceByTags<S, StiXourceByTags.FieldContainer<F, V>, F, V>
{
	public StiXourceByObjTags(StiXourceByObjTags<S, F, V> p0, F fieldDetails)
	{
		super(p0, fieldDetails);
	}

	public StiXourceByObjTags(StiXtractor<? extends S> p0, StiXFunction<? super S, V> fn)
	{
		super(p0, fn);
	}
	
	protected F getMaxFieldTag(Set<F> tags)
	{
		return null;
	}

	@Override protected FieldContainer<F, V> createFieldContainer(Map<F, ? extends SingleFieldHandler<V>> callbackMap, Settings settings)
	{
		if (callbackMap.size() == 1) {
			SingleFieldHandler<V> handler = callbackMap.values().iterator().next();
			return new SingletonFieldContainer<F, V>(handler);
		}

		F max = getMaxFieldTag(callbackMap.keySet());
		return new MappedFieldContainer<F, V>(max, callbackMap);
	}
}