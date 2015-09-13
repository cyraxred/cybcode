package org.cybcode.stix.ops;

import java.util.Arrays;

import org.cybcode.stix.api.StiXtractor;

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
	public static StiXtractor<Number> div(StiXtractor<? extends Number> p0, StiXtractor<? extends Number> p1) { return new StiX_Div(p0, p1); }
	public static StiXtractor<Number> rem(StiXtractor<? extends Number> p0, StiXtractor<? extends Number> p1) { return new StiX_Rem(p0, p1); }

	public static StiXtractor<Boolean> not(StiXtractor<? extends Boolean> p0) { return new StiX_Fn<>(p0, StiX_Fns.NOT); } 
	public static StiXtractor<Boolean> and(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1) { return new StiX_And(p0, p1, null); } 
	public static StiXtractor<Boolean> or(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1) { return new StiX_Or(p0, p1, null); }
	public static StiXtractor<Boolean> xor(StiXtractor<? extends Boolean> p0, StiXtractor<? extends Boolean> p1) { return new StiX_Xor(p0, p1); }
	
	
}
