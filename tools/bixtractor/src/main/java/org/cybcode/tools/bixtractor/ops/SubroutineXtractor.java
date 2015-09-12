package org.cybcode.tools.bixtractor.ops;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.MonoParameter;
import org.cybcode.tools.bixtractor.api.XecutionContext;
import org.cybcode.tools.bixtractor.api.XpressionRegistrator;
import org.cybcode.tools.bixtractor.core.Parameter;

public final class SubroutineXtractor<T> implements BiXtractor<T>, BiXtractor.Repeatable
{
	private final MonoParameter<T> p0;
	private final Parameter<?> p1;

	public SubroutineXtractor(BiXtractor<?> jointSource, BiXtractor<T> jointResult)
	{
		p0 = new MonoParameter<>(jointResult);
		p1 = new NullParameter<>(jointSource); //this parameter is only to pass the source extractor 
	}
	
	@Override public T evaluate(XecutionContext context)
	{
		return p0.get(context);
	}

	@Override public void visit(XpressionRegistrator visitor)
	{
		visitor.registerParameter(p0);
		visitor.registerParameter(p1);
	}

	@Override public Object getOperationToken()
	{
		return null;
	}

	@Override public int getOperationComplexity()
	{
		return COMPLEXITY_JUNCTION;
	}

	private static class NullParameter<T> extends Parameter<T>
	{
		public NullParameter(BiXtractor<? extends T> extractor)
		{
			super(extractor);
		}
		
		@Override protected Object get(XecutionContext context)
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override public String toString()
	{
		return "SUB(" + p0.toString("VAR") + ", " + p1.toString("result") + ")";
	}

	@Override public boolean isRepeated()
	{
		return true;
	}
}