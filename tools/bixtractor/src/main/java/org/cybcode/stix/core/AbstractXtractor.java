package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXtractor;

public abstract class AbstractXtractor<T> implements StiXtractor<T>
{
	@Override public String toString()
	{
		StringBuilder result = new StringBuilder();
		
		String name = getClass().getName();
		int sep = name.indexOf('_');
		if (sep > 0) {
			result.append(name, sep + 1, name.length());
		} else if (name.startsWith("StiX")) {
			result.append(name, 5, name.length());
		} else {
			result.append(name);
		}
		Object token = getOperationToken();
		if (token != null) {
			result.append('{').append(token).append('}');
		}
		return result.toString();
	}
}
