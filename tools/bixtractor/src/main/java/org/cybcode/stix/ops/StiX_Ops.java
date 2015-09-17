package org.cybcode.stix.ops;

import java.util.Arrays;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXtractor;

import com.google.common.base.Function;

public class StiX_Ops
{
	private StiX_Ops() {}

	public static StiX_Const<Long> constOf(long value) { return StiX_Const.of(value); }
	public static StiX_Const<Long> constOf(int value) { return StiX_Const.of((long) value); }
	public static StiX_Const<Double> constOf(double value) { return StiX_Const.of(value); }
	public static StiX_Const<Double> constOf(float value) { return StiX_Const.of((double) value); }
	public static StiX_Const<Boolean> constOf(boolean value) { return StiX_Const.of(value); }

	public static <T> StiX_Const<T> constOf(T value)
	{
		if (value == null) throw new NullPointerException();
		return StiX_Const.of(value);
	}
	
	public static <T> StiX_Root<T> root() { return StiX_Root.getInstance(); } 

	public static <T> StiXtractor<T> mux(StiXtractor<? extends T> p0) { return new StiX_Mux<T>(p0); } 
	public static <T> StiXtractor<T> mux(StiXtractor<? extends T> p0, StiXtractor<? extends T> p1) { return new StiX_Mux<T>(p0, p1); } 
	@SafeVarargs public static <T> StiXtractor<T> mux(StiXtractor<? extends T>... ps) { return new StiX_Mux<T>(Arrays.asList(ps)); } 
	
	public static StiXtractor<Number> neg(StiXtractor<? extends Number> p0) { return new StiX_Fn<>(p0, StiX_Fns.NEG); } 
	public static StiXtractor<Number> add(StiXtractor<? extends Number> p0, StiXtractor<? extends Number> p1) { return new StiX_Add(p0, p1); } 
	public static StiXtractor<Number> addA(StiXtractor<? extends Number> p0) { return new StiX_AddA(p0); } 
	public static StiXtractor<Number> sub(StiXtractor<? extends Number> p0, StiXtractor<? extends Number> p1) { return new StiX_Add(p0, neg(p1)); }
	public static StiXtractor<Number> mul(StiXtractor<? extends Number> p0, StiXtractor<? extends Number> p1) { return new StiX_Mul(p0, p1); }
	public static StiXtractor<Number> mulA(StiXtractor<? extends Number> p0) { return new StiX_MulA(p0); }
	public static StiXtractor<Number> div(StiXtractor<? extends Number> p0, StiXtractor<? extends Number> p1) { return new StiX_Div(p0, p1); }
	public static StiXtractor<Number> rem(StiXtractor<? extends Number> p0, StiXtractor<? extends Number> p1) { return new StiX_Rem(p0, p1); }

	public static StiXtractor<Boolean> not(StiXtractor<? extends Boolean> p0) { return new StiX_Fn<>(p0, StiX_Fns.NOT); } 
	public static StiXtractor<Boolean> and(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1) { return new StiX_And(p0, p1); } 
	public static StiXtractor<Boolean> andA(StiXtractor<? extends Boolean> p0) { return new StiX_AndA(p0); } 
	public static StiXtractor<Boolean> andIfNull(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1, boolean nullValue) { return new StiX_And(p0, p1, nullValue); } 
	public static StiXtractor<Boolean> or(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1) { return new StiX_Or(p0, p1); }
	public static StiXtractor<Boolean> orA(StiXtractor<? extends Boolean> p0) { return new StiX_OrA(p0); }
	public static StiXtractor<Boolean> orIfNull(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1, boolean nullValue) { return new StiX_Or(p0, p1, nullValue); } 
	public static StiXtractor<Boolean> xor(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1) { return new StiX_Xor(p0, p1); }

	public static <T> StiXtractor<T> last(StiXtractor<T> p0) { return new StiX_Last<>(p0); } 
	public static <T> StiXtractor<T> first(StiXtractor<T> p0) { return new StiX_First<>(p0); } 
	
	public static StiXtractor<Long> asInt(StiXtractor<?> p0) { return new StiX_Fn<>(p0, StiX_Fns.AS_INT); } 
	public static StiXtractor<Double> asFloat(StiXtractor<?> p0) { return new StiX_Fn<>(p0, StiX_Fns.AS_FLOAT); } 
	public static StiXtractor<String> asString(StiXtractor<?> p0) { return new StiX_Fn<>(p0, StiX_Fns.AS_STRING); } 
	public static StiXtractor<Boolean> asBool(StiXtractor<?> p0) { return new StiX_Fn<>(p0, StiX_Fns.AS_BOOL); } 
	
	public static <T extends Comparable<?>> StiXtractor<Boolean> gt(StiXtractor<? extends T> p0, StiXtractor<? extends T> p1) { return new StiX_Cmp<T>(p0, p1, StiX_Cmp.Mode.GT); }
	public static <T extends Comparable<?>> StiXtractor<Boolean> ge(StiXtractor<? extends T> p0, StiXtractor<? extends T> p1) { return new StiX_Cmp<T>(p0, p1, StiX_Cmp.Mode.GE); }
	public static <T extends Comparable<?>> StiXtractor<Boolean> lt(StiXtractor<? extends T> p0, StiXtractor<? extends T> p1) { return new StiX_Cmp<T>(p0, p1, StiX_Cmp.Mode.LT); }
	public static <T extends Comparable<?>> StiXtractor<Boolean> le(StiXtractor<? extends T> p0, StiXtractor<? extends T> p1) { return new StiX_Cmp<T>(p0, p1, StiX_Cmp.Mode.LE); }
	public static StiXtractor<Boolean> eq(StiXtractor<?> p0, StiXtractor<?> p1) { return new StiX_Eq(p0, p1); }
	public static StiXtractor<Boolean> ne(StiXtractor<?> p0, StiXtractor<?> p1) { return not(eq(p0, p1)); }
	
	public static <T> StiXtractor<T> nullIf(StiXtractor<? extends T> pValue, StiXtractor<? extends T> pValueForNull) { return new StiX_NullIf<T>(pValue, pValueForNull); } 
	public static <T> StiXtractor<T> ifNull(StiXtractor<? extends T> pValue, StiXtractor<? extends T> pValueForNull) { return new StiX_IfNull<T>(pValue, pValueForNull); } 

	public static StiXtractor<Boolean> isNull(StiXtractor<?> p0) { return new StiX_Fn<>(p0, StiX_Fns.IS_NULL); }
	public static StiXtractor<Boolean> isNotNull(StiXtractor<?> p0) { return not(isNull(p0)); } 

	public static <P, T> StiXtractor<T> fn(StiXtractor<P> p0, StiXFunction<P, T> fn) { return new StiX_Fn<P, T>(p0, fn); } 
	public static <P, T> StiXtractor<T> fn(StiXtractor<P> p0, Function<P, T> fn, int complexity, Class<T> resultType, Object token) { return new StiX_Fn<P, T>(p0, StiX_Fns.of(fn, complexity, resultType, token)); }
	
	public static StiXtractor<Long> count(StiXtractor<?> p0) { return new StiX_Count(p0); } 
	
	//in
	//intersect
	//anyIn
	//allIn
	//listOfConsts
	//listOfValues
	//listOfValuesA
	//count/length
}
