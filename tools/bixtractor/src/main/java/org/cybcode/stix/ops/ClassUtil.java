package org.cybcode.stix.ops;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassUtil
{
	private ClassUtil() {}

	public static Class<?> findNearestSupertype(Class<?> t1, Class<?> t2) 
	{
		if (t2.getSuperclass() == t1) return t1;
		if (t1.getSuperclass() == t2) return t2; 
		
		Set<Class<?>> parents = new HashSet<>(16);
		while (t1 != null) {
			parents.add(t1);
			t1 = t1.getSuperclass();
		}
		
		while (t2 != null) {
			if (parents.contains(t2)) return t2;
			t2 = t2.getSuperclass();
		}
		
		return null; //is this possible for primitives?
	}

	public static Class<?> findNearestSupertype(Class<?> t1, Class<?>... ts) 
	{
		Map<Class<?>, Integer> parents = new HashMap<>(16);
		int distance = 0;
		while (t1 != null) {
			parents.put(t1, distance++);
			t1 = t1.getSuperclass();
		}

		Class<?> result = null;
		distance = -1;
		
		for (Class<?> t2 : ts) {
			while (t2 != null) {
				Integer foundDistance = parents.get(t2);
				if (foundDistance != null) {
					if (result == null || distance < foundDistance) {
						distance = foundDistance;
						result = t2;
					}
					break;
				}
				t2 = t2.getSuperclass();
			}
		}
		
		return result; //is this possible for primitives?
	}
}
