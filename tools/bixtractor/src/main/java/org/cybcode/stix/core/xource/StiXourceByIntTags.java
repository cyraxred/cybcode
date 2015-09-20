package org.cybcode.stix.core.xource;

import java.util.Iterator;
import java.util.Map;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXource;
import org.cybcode.stix.api.StiXtractor;

public abstract class StiXourceByIntTags<S, V extends StiXourceByTags.FieldValue<Integer>> 
	extends StiXourceByTags<S, StiXourceByTags.FieldContainer<Integer, V>, Integer, V>
{
	public StiXourceByIntTags(StiXource<?, ?, Integer, V> p0, Integer fieldDetails)
	{
		super(p0, fieldDetails);
	}

	public StiXourceByIntTags(StiXtractor<? extends S> p0, StiXFunction<? super S, V> fn)
	{
		super(p0, fn);
	}

	@Override protected FieldContainer<Integer, V> createFieldContainer(Map<Integer, ? extends SingleFieldHandler<V>> callbackMap, Settings settings)
	{
		switch (callbackMap.size()) {
			case 0: return null;
			case 1: {
				SingleFieldHandler<V> handler = callbackMap.values().iterator().next();
				return new SingletonFieldContainer<Integer, V>(handler);
			}
		}

		Iterator<Integer> iter = callbackMap.keySet().iterator();
		int min = iter.next().intValue();
		int max = min;
		while (iter.hasNext()) {
			int tag = iter.next().intValue();
			min = Math.min(min, tag);
			max = Math.max(max, tag);
		}
		int rangeWidth = max - min + 1;

		if (callbackMap.size() < (rangeWidth >> 1)) {
			//field numbers are not compact, use map
			return new MappedFieldContainer<Integer, V>(max, callbackMap);
		}

		return new ArrayFieldContainer<V>(min, max, callbackMap);
	}
}

