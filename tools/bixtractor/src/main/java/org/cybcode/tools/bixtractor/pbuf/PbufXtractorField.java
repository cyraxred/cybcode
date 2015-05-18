package org.cybcode.tools.bixtractor.pbuf;

import org.cybcode.tools.bixtractor.core.AbstractBiXtractorField;

import com.google.common.base.Function;

public class PbufXtractorField<T> extends AbstractBiXtractorField<T> implements PbufXtractorFieldInfo
{
	private final Token<T> token;

	public PbufXtractorField(int fieldId, Function<PbufFieldValue, T> converter, PbufXtractorSource p0)
	{
		super(p0);
		this.token = new Token<>(fieldId, converter);
	}
	
	@Override public Object getOperationToken()
	{
		return token;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_OPERATION;
	}

	@Override public int fieldId()
	{
		return token.fieldId;
	}

	@Override protected T evaluate(Object value)
	{
		return token.converter.apply((PbufFieldValue) value);
	}
	
	private static class Token<T>
	{
		private final int fieldId;
		private final Function<PbufFieldValue, T> converter;
		
		private Token(int fieldId, Function<PbufFieldValue, T> converter)
		{
			super();
			this.fieldId = fieldId;
			this.converter = converter;
		}

		@Override public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((converter == null) ? 0 : converter.hashCode());
			result = prime * result + fieldId;
			return result;
		}

		@Override public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Token<?> other = (Token<?>) obj;
			if (converter == null)
			{
				if (other.converter != null) return false;
			} else if (!converter.equals(other.converter)) return false;
			if (fieldId != other.fieldId) return false;
			return true;
		}
	}
}