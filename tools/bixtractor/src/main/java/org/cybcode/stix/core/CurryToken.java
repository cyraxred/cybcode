package org.cybcode.stix.core;

import org.cybcode.stix.api.StiXtractor;

class CurryToken<O extends StiXtractor<?>, T>
{
	private static final Object NULL = new byte[0]; 
	protected final O outer;
	protected final T	value;

	public CurryToken(O outer, T value)
	{
		this.outer = outer;
		this.value = value;			
	}

	public T getValue()
	{
		return value;
	}

	public O getOuter()
	{
		return outer;
	}
	
	private Object outerToken()
	{
		return outer == null ? NULL : outer.getOperationToken();
	}

	@Override public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + outerToken().hashCode();
		return result;
	}

	@Override public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CurryToken<?, ?> other = (CurryToken<?, ?>) obj;
		if (value == null)
		{
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		if (!outerToken().equals(other.outerToken())) return false;
		return true;
	}

	@Override public String toString()
	{
		return "CurryToken[outer=" + outer + ", value=" + value + "]";
	}	
}