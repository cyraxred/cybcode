package org.cybcode.stix;

import java.util.LinkedList;
import java.util.List;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.compiler.StiXpressionRecursiveParser;
import org.cybcode.stix.core.xecutors.StiXecutorDefaultContext;
import org.cybcode.stix.core.xecutors.StiXecutorDefaultContextBuilder;
import org.cybcode.stix.core.xecutors.StiXpressionNode;
import org.cybcode.stix.core.xecutors.StiXpressionNode.PushTarget;
import org.cybcode.stix.core.xecutors.StiXpressionSequencer;
import org.cybcode.stix.ops.StiX_Ops;

import static org.cybcode.stix.ops.StiX_Ops.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class StiXExpressionsTest
{
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

	private static <T> T E(Object rootValue, StiXtractor<T> expression)
	{
		StiXecutorDefaultContext context = B(expression);
		StiXpressionSequencer sequencer = new SimpleXpressionSequencer();
		@SuppressWarnings("unchecked") T result = (T) context.evaluateExpression(sequencer, rootValue);
		return result;
	}
}

class SimpleXpressionSequencer implements StiXpressionSequencer
{
	private final LinkedList<PushTarget> queue = new LinkedList<>();

	@Override public void addPushTargets(List<PushTarget> targets)
	{
		queue.addAll(targets);
	}

	@Override public void addNotifyTargets(List<PushTarget> targets)
	{
		//ignore
	}

	@Override public PushTarget nextTargetBefore(StiXpressionNode node)
	{
		if (queue.isEmpty()) return null;
		return queue.removeLast();
	}

	@Override public void resetSequencer()
	{
		queue.clear();
	}
}