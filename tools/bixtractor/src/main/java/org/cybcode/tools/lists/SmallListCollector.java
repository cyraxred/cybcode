package org.cybcode.tools.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmallListCollector<T>
{
	private int count;
	private T value0;
	private Object value1;

	@SuppressWarnings("unchecked") public void add(T param)
	{
		switch (count++) {
			case 0: value0 = param; break;
			case 1: value1 = param; break;
			case 2: {
				List<T> list = new ArrayList<>(4);
				list.add(value0);
				list.add((T) value1);
				list.add(param);
				value1 = list;
				break;
			}
			default: {
				((List<T>) value1).add(param);
			}
		}
	}
	
	public int size()
	{
//		if (count < 0 || count > 2) return ((List<?>) value1).size(); 
		return count;
	}
	
	public T getFirstValueOrNull()
	{
		return value0; 
	}
	
	public void clear()
	{
		count = 0;
		value0 = null;
		value1 = null;
	}
	
	@SuppressWarnings("unchecked") public T get(int index) 
	{
		switch (count) {
			case 0: return Collections.<T>emptyList().get(index);
			case 2: if (index == 1) return (T) value1;
			case 1: if (index == 0) return value0;
				throw new IndexOutOfBoundsException("Index: " + index);
			default: return ((List<T>) value1).get(index);
		}
	}
	
	@SuppressWarnings("unchecked") public List<T> asList()
	{
		switch (count) {
			case 0: return Collections.emptyList();
			case 1: return Collections.singletonList(value0);
			case 2: return Arrays.asList(value0, (T) value1);
			default: return (List<T>) value1;
		}
	}
	
	@Override public String toString()
	{
		switch (count) {
			case 0: return Collections.emptyList().toString();
			case 1: return "[" + value0 + "]";
			case 2: return "[" + value0 + "," + value1 + "]";
			default: return value1.toString();
		}
	}
}