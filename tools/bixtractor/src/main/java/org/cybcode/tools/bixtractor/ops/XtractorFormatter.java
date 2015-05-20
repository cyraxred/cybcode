package org.cybcode.tools.bixtractor.ops;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.core.Parameter;

public class XtractorFormatter
{
	private XtractorFormatter() {}
	
	public static StringBuilder nameOf(BiXtractor<?> op, StringBuilder out)
	{
		if (op == null) {
			out.append((Object) null);
			return out;
		}
		
		String name = op.getClass().getSimpleName();
		
		if (name.endsWith("Xtractor")) {
			out.append(name, 0, name.length() - "Xtractor".length());
		} else if (name.endsWith("Extractor")) {
			out.append(name, 0, name.length() - "Extractor".length());
		} else if (name.endsWith("Xource")) {
			out.append(name, 0, name.length() - "Xource".length());
		} else if (name.endsWith("Source")) {
			out.append(name, 0, name.length() - "Source".length());
		} else if (name.endsWith("XtractorField")) {
			out.append(name, 0, name.length() - "XtractorField".length());
			out.append("Field");
		} else if (name.endsWith("ExtractorField")) {
			out.append(name, 0, name.length() - "ExtractorField".length());
			out.append("Field");
		} else {
			out.append(name);
		}
		return out;
	}

	public static String nameOf(BiXtractor<?> op)
	{
		return nameOf(op, new StringBuilder()).toString();
	}

	public static StringBuilder appendName(BiXtractor<?> op, StringBuilder out)
	{
		return nameOf(op, out).append('(');
	}
	
	public static String toString(BiXtractor<?> op)
	{
		return appendName(op, new StringBuilder()).append(')').toString();
	}

	public static String toString(BiXtractor<?> op, Parameter<?> p0)
	{
		return appendName(op, new StringBuilder()).append(p0).append(')').toString();
	}

	public static String toString(BiXtractor<?> op, Parameter<?> p0, Parameter<?> p1)
	{
		return appendName(op, new StringBuilder()).append(p0).append(", ").append(p1).append(')').toString();
	}

	public static String toString(BiXtractor<?> op, Parameter<?>... p)
	{
		return toString(op, Arrays.asList(p));
	}

	public static String toString(BiXtractor<?> op, Collection<? extends Parameter<?>> p)
	{
		StringBuilder result = appendName(op, new StringBuilder());
		if (!p.isEmpty()) {
			Iterator<? extends Parameter<?>> iter = p.iterator();
			result.append(iter.next());
			while (iter.hasNext()) {
				result.append(", ");
				result.append(iter.next());
			}
		}
		result.append(')');
		return result.toString();
	}
}