package org.cybcode.stix.ops;

import org.cybcode.stix.api.StiXFunction;

import com.google.common.base.Function;

public class StiX_Fns
{
	private StiX_Fns() {}
	
	public static <P, T> StiXFunction<P, T> of(final Function<P, T> fn, final int complexity, final Class<T> resultType, final Object token)
	{
		if (fn == null) throw new NullPointerException();
		if (token == null) throw new NullPointerException();
		if (resultType == null) throw new NullPointerException();
		if (complexity <= 0) throw new IllegalArgumentException();
		
		return new StiXFunction<P, T>() 
		{
			@Override public T apply(P input) { return fn.apply(input); }
			@Override public Object getOperationToken() { return token; }
			@Override public int getOperationComplexity() { return complexity; }
			@Override public Class<? extends T> resultType() { return resultType; }
			@Override public String toString() { return token.toString(); }
		};
	}
	
	private enum FnType {
		NOT(2), NEG(5), AS_INT(50), AS_FLOAT(50), AS_BOOL(50), AS_STRING(50), IS_NULL(10);

		private final int complexity;
		private FnType(int complexity) { this.complexity = complexity; }
		public int getOperationComplexity() { return complexity; }
	}
	
	public static StiXFunction<Boolean, Boolean> NOT = new Fn<Boolean, Boolean>()
	{
		@Override public Boolean apply(Boolean p0)
		{
			if (p0 == null) return null;
			return !p0.booleanValue();
		}

		@Override protected FnType typeToken() { return FnType.NOT; }
		@Override public Class<Boolean> resultType() { return Boolean.class; };
	};

	public static StiXFunction<Number, Number> NEG = new Fn<Number, Number>()
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

		@Override protected FnType typeToken() { return FnType.NEG; }
		@Override public Class<Number> resultType() { return Number.class; };
	};

	public static StiXFunction<Object, Long> AS_INT = new Fn<Object, Long>()
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

		@Override protected FnType typeToken() { return FnType.AS_INT; }
		@Override public Class<Long> resultType() { return Long.class; };
	};

	public static StiXFunction<Object, Double> AS_FLOAT = new Fn<Object, Double>()
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

		@Override protected FnType typeToken() { return FnType.AS_FLOAT; }
		@Override public Class<Double> resultType() { return Double.class; };
	};

	public static StiXFunction<Object, Boolean> AS_BOOL = new Fn<Object, Boolean>()
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

		@Override protected FnType typeToken() { return FnType.AS_BOOL; }
		@Override public Class<Boolean> resultType() { return Boolean.class; };
	};

	public static StiXFunction<Object, String> AS_STRING = new Fn<Object, String>()
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

		@Override protected FnType typeToken() { return FnType.AS_STRING; }
		@Override public Class<String> resultType() { return String.class; }
	};

	public static StiXFunction<Object, Boolean> IS_NULL = new Fn<Object, Boolean>()
	{
		@Override public Boolean apply(Object p0)
		{
			return p0 == null;
		}

		@Override protected FnType typeToken() { return FnType.AS_STRING; }
		@Override public Class<Boolean> resultType() { return Boolean.class; }
	};

	private abstract static class Fn<P0, T> implements StiXFunction<P0, T>
	{
		@Override public int getOperationComplexity()
		{
			return typeToken().getOperationComplexity();
		}
		
		protected abstract FnType typeToken();

		@Override public Object getOperationToken()
		{
			return typeToken();
		};
		
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
