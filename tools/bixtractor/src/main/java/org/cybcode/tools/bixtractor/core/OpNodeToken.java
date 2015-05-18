package org.cybcode.tools.bixtractor.core;

import java.util.Arrays;

final class OpNodeToken
{
	final Class<?> opType;
	final Object opToken;
	final int[] params;
	
	OpNodeToken(Class<?> opType, Object opToken, int[] params)
	{
		this.opType = opType;
		this.opToken = opToken;
		this.params = params;
	}

	@Override public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + opType.hashCode();
		result = prime * result + Arrays.hashCode(params);
		result = prime * result + ((opToken == null) ? 0 : opToken.hashCode());
		return result;
	}

	@Override public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		OpNodeToken other = (OpNodeToken) obj;
		if (!opType.equals(other.opType)) return false;
		if (!Arrays.equals(params, other.params)) return false;
		
		if (opToken == null){
			if (other.opToken != null) return false;
		} else if (!opToken.equals(other.opToken)) return false;
		
		return true;
	}
}