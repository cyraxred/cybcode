package org.cybcode.stix.core.xource;

import java.util.Iterator;
import java.util.Map;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXource;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.Multiplicity;

public abstract class StiXourceByIntTags<S, V extends StiXourceByTags.FieldValue<Integer>> 
	extends StiXourceByTags<S, StiXourceByTags.FieldContainer<Integer, V>, Integer, V>
{
	public StiXourceByIntTags(StiXource<?, ?, Integer, V> p0, Integer fieldDetails, Multiplicity limitMode)
	{
		super(p0, fieldDetails, limitMode);
	}

	public StiXourceByIntTags(StiXtractor<? extends S> p0, StiXFunction<? super S, V> fn, Multiplicity limitMode)
	{
		super(p0, fn, limitMode);
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

