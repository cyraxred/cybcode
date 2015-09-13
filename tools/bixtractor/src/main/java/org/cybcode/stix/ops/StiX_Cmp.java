package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.StiXtractorDuo;

public class StiX_Cmp<T extends Comparable<?>> extends StiXtractorDuo<T, T, Boolean>
{
	private Mode	mode;

	public enum Mode
	{
		GT { @Override public boolean toBoolean(int compareResult) { return compareResult > 0; } }, 
		GE { @Override public boolean toBoolean(int compareResult) { return compareResult >= 0; } },
		LT { @Override public boolean toBoolean(int compareResult) { return compareResult < 0; } },
		LE { @Override public boolean toBoolean(int compareResult) { return compareResult <= 0; } };
		
		public abstract boolean toBoolean(int compareResult); 
	}
	
	public StiX_Cmp(StiXtractor<? extends T> p0, StiXtractor<? extends T> p1, Mode mode)
	{
		super(p0, p1);
		if (mode == null) throw new NullPointerException();
		this.mode = mode;
	}

	@Override public Object getOperationToken()
	{
		return mode;
	}

	@Override public int getOperationComplexity()
	{
		return 20;
	}

	@Override protected Boolean calculate(T p0, T p1)
	{
		@SuppressWarnings("unchecked") Comparable<Object> pp0 = (Comparable<Object>) p0;
		int result;
		try {
			result = pp0.compareTo(p1);
		} catch (ClassCastException e) {
			return null;
		}
		return mode.toBoolean(result);
	}
}