package org.cybcode.stix;

import org.cybcode.stix.api.StiXecutorContextBuilder;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.compiler.StiXpressionRecursiveParser;
import org.cybcode.stix.core.xecutors.XecutorContextBuilder;
import org.cybcode.stix.core.xecutors.XpressionRunnerBuilder;
import org.cybcode.stix.core.xecutors.XpressionRunnerBuilder.ContextInspector;

import com.google.common.base.Function;

public class TestBase
{
	protected final StatsCollector stats = new StatsCollector();
	
	protected XpressionRunnerBuilder.ContextInspector B(StiXtractor<?> expression)
	{
		XecutorContextBuilder<XpressionRunnerBuilder.ContextInspector> builder = new XpressionRunnerBuilder().
			setStats(stats).createContextBuilder();
		
		B(expression, builder);
		return builder.build();
	}
	
	private static void B(StiXtractor<?> expression, StiXecutorContextBuilder builder)
	{
		StiXpressionRecursiveParser parser = new StiXpressionRecursiveParser();
		parser.step1_buildTree(expression);
		parser.step2_optimizeTree();
		parser.step3_linkTree();
		parser.step4_optimizeLinkedTree();
		parser.print();
		parser.step5_flattenTree(builder);
		parser.print();
	}

	private <T> T E(boolean regularAsNotify, Object rootValue, StiXtractor<T> expression)
	{
		XecutorContextBuilder<Function<Object, Object>> builder = new XpressionRunnerBuilder().setStats(stats).createRunnerBuilder();
		
		B(expression, builder);
		Function<Object, Object> fn = builder.build();
		@SuppressWarnings("unchecked") T result = (T) fn.apply(rootValue);
		return result;
	}

	protected <T> T E(Object rootValue, StiXtractor<T> expression)
	{
		return E(false, rootValue, expression);
	}

	protected <T> T EP(Object rootValue, StiXtractor<T> expression)
	{
		return E(true, rootValue, expression);
	}
}