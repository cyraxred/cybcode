package org.cybcode.tools.bixtractor.core;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.MonoPushParameter;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;
import org.cybcode.tools.bixtractor.ops.XtractorFormatter;
import org.cybcode.tools.bixtractor.pbuf.PbufXource;

public abstract class AbstractBiXtractorField<T> implements BiXtractor<T>
{
	private final FieldParameter<?> p0;
	private final boolean isRepeated;

	public AbstractBiXtractorField(PbufXource p0, boolean isRepeated)
	{
		this.p0 = new FieldParameter<>(p0);
		this.isRepeated = isRepeated;
	}
	
	class FieldParameter<P> extends MonoPushParameter<P>
	{
		public FieldParameter(BiXtractor<? extends P> extractor)
		{
			super(extractor);
		}

		@Override public boolean pushValue(XecutionContext context, Object value)
		{
			if (!isRepeated && has(context)) return false;
			
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
	
	@Override public boolean isRepeatable()
	{
		return isRepeated;
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

	@Override public String toString()
	{
		return XtractorFormatter.appendName(this, new StringBuilder()).append(getDescription()).append(", ").
			append(p0.toString("src")).append(')').toString();
	}

	protected abstract String getDescription();
}