package org.cybcode.stix.api;

public final class TokenPair<P0, P1>
{
	private final P0 p0;
	private final P1 p1;

	public static <P0, P1> TokenPair<P0, P1> of(P0 p0, P1 p1)
	{
		return new TokenPair<P0, P1>(p0, p1);
	}
	
	public TokenPair(P0 p0, P1 p1)
	{
		this.p0 = p0;
		this.p1 = p1;
	}

	@Override public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((p0 == null) ? 0 : p0.hashCode());
		result = prime * result + ((p1 == null) ? 0 : p1.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TokenPair<?, ?> other = (TokenPair<?, ?>) obj;
		if (p0 == null)
		{
			if (other.p0 != null) return false;
		} else if (!p0.equals(other.p0)) return false;
		if (p1 == null)
		{
			if (other.p1 != null) return false;
		} else if (!p1.equals(other.p1)) return false;
		return true;
	}

	@Override public String toString()
	{
		return "TokenPair[p0=" + p0 + ", p1=" + p1 + "]";
	}
}