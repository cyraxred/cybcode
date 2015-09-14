package org.cybcode.stix;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.compiler.StiXpressionRecursiveParser;
import org.cybcode.stix.core.xecutors.StiXecutorDefaultContext;
import org.cybcode.stix.core.xecutors.StiXecutorDefaultContextBuilder;
import org.cybcode.stix.core.xecutors.StiXpressionSequencer;
import org.cybcode.stix.ops.StiX_Ops;

import static org.cybcode.stix.ops.StiX_Ops.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class StiXExpressionsTest
{
	private StatsCollector stats = new StatsCollector();
	
	@Test public void test_const()
	{
		assertEquals((Long) 1L, E(2L, constOf(1)));
		assertEquals(-(Long) 1L, E(2L, neg(constOf(1))));
		assertEquals((Long) 4L, E(2L, add(constOf(1), constOf(3))));
	}

	@Test public void test_root()
	{
		assertEquals((Long) 2L, E(2L, root()));
		assertEquals((Long) 3L, E(2L, add(constOf(1), StiX_Ops.<Long>root())));
		assertEquals((Long) 4L, E(2L, add(StiX_Ops.<Long>root(), StiX_Ops.<Long>root())));
	}

	@Test public void test_aggregate()
	{
		assertEquals((Long) 2L, E(2L, addA(StiX_Ops.<Long>root())));
		assertEquals((Long) 2L, E(2L, addA(mux(StiX_Ops.<Long>root()))));
		assertEquals((Long) 3L, E(2L, addA(mux(StiX_Ops.<Long>root(), constOf(1)))));
		assertEquals((Long) 5L, E(2L, addA(mux(StiX_Ops.<Long>root(), constOf(1), StiX_Ops.<Long>root()))));
	}

	@Test public void test_and_full()
	{
		assertEquals(Boolean.FALSE, E(2L, and(constOf(true), constOf(false))));
		assertEquals(4, stats.nodeCount);
		assertEquals(4, stats.evaluateCount);
		assertEquals(0, stats.pushAttemptCount);
		assertEquals(0, stats.pushEvaluateCount);
		
		assertEquals(Boolean.FALSE, EP(2L, and(constOf(true), constOf(false))));
		assertEquals(4, stats.nodeCount);
		assertEquals(4, stats.evaluateCount);
		assertEquals(0, stats.pushAttemptCount);
		assertEquals(0, stats.pushEvaluateCount);
	}

	@Test public void test_and_early()
	{
		assertEquals(Boolean.FALSE, E(2L, andIfNull(constOf(false), constOf(true), true)));
		assertEquals(4, stats.nodeCount);
		assertEquals(4, stats.evaluateCount);
		assertEquals(0, stats.pushAttemptCount);
		assertEquals(0, stats.pushEvaluateCount);
		
		assertEquals(Boolean.FALSE, EP(2L, andIfNull(constOf(false), constOf(true), true)));
		assertEquals(4, stats.nodeCount);
		assertEquals(2, stats.evaluateCount);
		assertEquals(1, stats.pushAttemptCount);
		assertEquals(1, stats.pushEvaluateCount);

		assertEquals(Boolean.FALSE, EP(2L, andIfNull(constOf(true), constOf(false), true)));
		assertEquals(4, stats.nodeCount);
		assertEquals(3, stats.evaluateCount);
		assertEquals(2, stats.pushAttemptCount);
		assertEquals(1, stats.pushEvaluateCount);
	}

	@Test public void test_and_aggregate()
	{
		assertEquals(Boolean.FALSE, E(2L, andA(mux(constOf(false), constOf(true)))));
		assertEquals(5, stats.nodeCount);
		assertEquals(2, stats.evaluateCount);
		assertEquals(2, stats.pushAttemptCount);
		assertEquals(2, stats.pushEvaluateCount);
		
		assertEquals(Boolean.FALSE, EP(2L, andA(mux(constOf(false), constOf(true)))));
		assertEquals(5, stats.nodeCount);
		assertEquals(2, stats.evaluateCount);
		assertEquals(2, stats.pushAttemptCount);
		assertEquals(2, stats.pushEvaluateCount);

		assertEquals(Boolean.FALSE, EP(2L, andA(mux(constOf(true), constOf(false)))));
		assertEquals(5, stats.nodeCount);
		assertEquals(3, stats.evaluateCount);
		assertEquals(4, stats.pushAttemptCount);
		assertEquals(3, stats.pushEvaluateCount);
	}

	private static <T> StiXecutorDefaultContext B(StiXtractor<T> expression)
	{
		StiXpressionRecursiveParser parser = new StiXpressionRecursiveParser();
		parser.step1_buildTree(expression);
		parser.step2_optimizeTree();
		parser.step3_linkTree();
		parser.step4_optimizeLinkedTree();
		StiXecutorDefaultContextBuilder builder = new StiXecutorDefaultContextBuilder();
		parser.step5_flattenTree(builder);
		return builder.build();
	}

	private <T> T E(boolean enableNotify, Object rootValue, StiXtractor<T> expression)
	{
		StiXecutorDefaultContext context = B(expression);
		context.setStatsCollector(stats);
		StiXpressionSequencer sequencer = new SimpleXpressionSequencer(enableNotify);
		@SuppressWarnings("unchecked") T result = (T) context.evaluateExpression(sequencer, rootValue);
		return result;
	}

	private <T> T E(Object rootValue, StiXtractor<T> expression)
	{
		return E(false, rootValue, expression);
	}

	private <T> T EP(Object rootValue, StiXtractor<T> expression)
	{
		return E(true, rootValue, expression);
	}
}
