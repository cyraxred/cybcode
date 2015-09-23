package org.cybcode.stix.ops;

import org.cybcode.stix.api.OutputMode;
import org.cybcode.stix.api.StiXComplexityHelper;
import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorConstructionContext;
import org.cybcode.stix.api.StiXecutorContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.AbstractXtractor;

public final class StiX_SubroutineRoot<T> extends AbstractXtractor<T>
{
	private static StiX_SubroutineRoot<?> INSTANCE = new StiX_SubroutineRoot<Object>(0);
	
	@SuppressWarnings("unchecked") public static <T> StiX_SubroutineRoot<T> getInstance()
	{
		return (StiX_SubroutineRoot<T>) INSTANCE;
	}

	private final int subroutineLevel;

	public StiX_SubroutineRoot(int subroutineLevel)
	{
		if (subroutineLevel < 0) throw new IllegalArgumentException();
		this.subroutineLevel = subroutineLevel;
	}

	@Override public StiXecutor createXecutor(StiXecutorConstructionContext context)
	{
		throw new UnsupportedOperationException();
	}

	@Override public T apply(StiXecutorContext context)
	{
		throw new UnsupportedOperationException();
	}

	@Override public void visit(Visitor visitor)
	{
		throw new UnsupportedOperationException();
	}

	@Override public int paramCount()
	{
		return 0;
	}

	@Override public final OutputMode getOutputMode()
	{
		return OutputMode.REGULAR;
	}

	@Override public Class<? extends T> resultType()
	{
		throw new UnsupportedOperationException();
	}

	@Override public Object getOperationToken()
	{
		return subroutineLevel;
	}

	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 1);
	}

	@Override public StiXtractor<? extends T> curry(int parameterIndex, Object value)
	{
		return null;
	}

	public int getSubroutineLevel()
	{
		return subroutineLevel;
	}
}
