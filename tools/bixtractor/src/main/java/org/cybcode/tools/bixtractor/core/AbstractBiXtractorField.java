package org.cybcode.tools.bixtractor.core;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.MonoPushParameter;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;
import org.cybcode.tools.bixtractor.pbuf.PbufXtractorSource;

public abstract class AbstractBiXtractorField<T> implements BiXtractor<T>
{
	private final FieldParameter<?> p0;

	public AbstractBiXtractorField(PbufXtractorSource p0)
	{
		this.p0 = new FieldParameter<>(p0);
	}
	
	class FieldParameter<P> extends MonoPushParameter<P>
	{
		public FieldParameter(BiXtractor<? extends P> extractor)
		{
			super(extractor);
		}

		@Override public boolean pushValue(XecutionContext context, Object value)
		{
			if (value != null) {
				value = evaluate(value);
			}
			context.setLocalValue(value);
			return true;
		}
		
		@Override public P get(XecutionContext context)
		{
			throw new UnsupportedOperationException();
		}
	}

	protected abstract T evaluate(Object value);
	
	@SuppressWarnings("unchecked") @Override public T evaluate(XecutionContext context)
	{
		return (T) context.getLocalValue();
	}

	@Override public final void visit(XpressionRegistrator visitor)
	{
		visitor.registerParameter(p0);
	}
}