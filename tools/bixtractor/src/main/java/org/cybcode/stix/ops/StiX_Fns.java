package org.cybcode.stix.ops;

import org.cybcode.stix.core.FunctionWithComplexity;

public class StiX_Fns
{
	private StiX_Fns() {}
	
	private enum FnType {
		NOT(2), NEG(5), AS_INT(50), AS_FLOAT(50), AS_BOOL(50), AS_STRING(50);

		private final int complexity;
		private FnType(int complexity) { this.complexity = complexity; }
		public int getOperationComplexity() { return complexity; }
	}
	
	public static FunctionWithComplexity<Boolean, Boolean> NOT = new Fn<Boolean, Boolean>()
	{
		@Override public Boolean apply(Boolean p0)
		{
			if (p0 == null) return null;
			return !p0.booleanValue();
		}

		@Override protected FnType typeToken()
		{
			return FnType.NOT;
		}
	};

	public static FunctionWithComplexity<Number, Number> NEG = new Fn<Number, Number>()
	{
		@Override public Number apply(Number p0)
		{
			if (p0 instanceof Long) {
				long v0 = p0.longValue();
				if (v0 == 0) return p0;
				return -v0;
			} else {
				double v0 = p0.doubleValue();
				if (Double.isNaN(v0)) return null;
				if (v0 == 0.0d) return p0;
				return -v0;
			}
		}

		@Override protected FnType typeToken()
		{
			return FnType.NEG;
		}
	};

	public static FunctionWithComplexity<Object, Long> AS_INT = new Fn<Object, Long>()
	{
		@Override public Long apply(Object p0)
		{
			if (p0 instanceof Long) {
				return (Long) p0;
			} else if (p0 instanceof Boolean) {
				return ((Boolean) p0).booleanValue() ? 1L : 0L;
			} else if (p0 instanceof String) {
				return Long.valueOf((String) p0);
			} else {
				return ((Number) p0).longValue();
			}
		}

		@Override protected FnType typeToken()
		{
			return FnType.AS_INT;
		}
	};

	public static FunctionWithComplexity<Object, Double> AS_FLOAT = new Fn<Object, Double>()
	{
		@Override public Double apply(Object p0)
		{
			if (p0 instanceof Double) {
				return (Double) p0;
			} else if (p0 instanceof Boolean) {
				return ((Boolean) p0).booleanValue() ? 1d : 0d;
			} else if (p0 instanceof String) {
				return Double.valueOf((String) p0);
			} else {
				return ((Number) p0).doubleValue();
			}
		}

		@Override protected FnType typeToken()
		{
			return FnType.AS_FLOAT;
		}
	};

	public static FunctionWithComplexity<Object, Boolean> AS_BOOL = new Fn<Object, Boolean>()
	{
		@Override public Boolean apply(Object p0)
		{
			if (p0 instanceof Boolean) {
				return (Boolean) p0;
			} else if (p0 instanceof String) {
				return Boolean.valueOf((String) p0);
			} else {
				return ((Number) p0).longValue() != 0;
			}
		}

		@Override protected FnType typeToken()
		{
			return FnType.AS_BOOL;
		}
	};

	public static FunctionWithComplexity<Object, String> AS_STRING = new Fn<Object, String>()
	{
		@Override public String apply(Object p0)
		{
			if (p0 instanceof Boolean) {
				return p0.toString();
			} else if (p0 instanceof Number) {
				return p0.toString();
			} else {
				return (String) p0;
			}
		}

		@Override protected FnType typeToken()
		{
			return FnType.AS_STRING;
		}
	};

	private abstract static class Fn<P0, T> implements FunctionWithComplexity<P0, T>
	{
		@Override public int getOperationComplexity()
		{
			return typeToken().getOperationComplexity();
		}
		
		protected abstract FnType typeToken();
		
		@Override public String toString()
		{
			return typeToken().toString();
		}
		
		@Override public int hashCode()
		{
			return typeToken().hashCode();
		}
		
		@Override public boolean equals(Object obj)
		{
			if (!(obj instanceof Fn)) return false;
			return typeToken().equals(((Fn<?, ?>) obj).typeToken());			
		}
	}
}
