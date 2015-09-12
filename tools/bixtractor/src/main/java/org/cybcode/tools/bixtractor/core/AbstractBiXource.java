package org.cybcode.tools.bixtractor.core;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.BiXource;
import org.cybcode.tools.bixtractor.api.MonoParameter;
import org.cybcode.tools.bixtractor.api.BiXourceContext;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.BiXourceLink;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;
import org.cybcode.tools.bixtractor.ops.XtractorFormatter;

public abstract class AbstractBiXource<Context extends BiXourceContext> implements BiXource
{
	private final Parameter<?> source;

	public AbstractBiXource(BiXtractor<?> source, boolean lazy)
	{
		if (!lazy || source instanceof BiXource) {
			this.source = new SourceParameter(source);
		} else {
			this.source = new MonoParameter<>(source);
		}
	}

	@Override public Result evaluate(XecutionContext context)
	{
		Object value = source.get(context);
		if (evaluate(context, value)) return Result.STOP;
		return Result.CONTINUE;
	}

	@SuppressWarnings("unchecked") private boolean evaluate(XecutionContext context, Object value)
	{
		if (value == null) return false;
		return processValue(context, (Context) context.getSourceContext(), value);
	}
	
	/**
	 * NB! This method reposition context! Do not use context after it. 
	 */
	protected abstract boolean processValue(XecutionContext context, Context sourceContext, Object value);
	
	@Override public void visit(XpressionRegistrator visitor)
	{
		visitor.registerParameter(source);
	}
	
	@Override public abstract Context buildContext(BiXourceLink[] receivers);

	private class SourceParameter extends Parameter<Object> implements PushParameter
	{
		public SourceParameter(BiXtractor<?> extractor)
		{
			super(extractor);
		}

		@Override public boolean pushValue(XecutionContext context, Object value)
		{
			return AbstractBiXource.this.evaluate(context, value);
		}
		
		@Override protected Object get(XecutionContext context)
		{
			return null;
		}

		@Override public boolean isMultiValue(XecutionContext context)
		{
			return super.isMultiValue(context);
		}
	}
	
	@Override public String toString()
	{
		return XtractorFormatter.toString(this, source);
	}
}