package org.cybcode.stix;

import static org.cybcode.stix.ops.StiX_Ops.add;
import static org.cybcode.stix.ops.StiX_Ops.addA;
import static org.cybcode.stix.ops.StiX_Ops.and;
import static org.cybcode.stix.ops.StiX_Ops.andA;
import static org.cybcode.stix.ops.StiX_Ops.andIfNull;
import static org.cybcode.stix.ops.StiX_Ops.constOf;
import static org.cybcode.stix.ops.StiX_Ops.div;
import static org.cybcode.stix.ops.StiX_Ops.mul;
import static org.cybcode.stix.ops.StiX_Ops.mux;
import static org.cybcode.stix.ops.StiX_Ops.neg;
import static org.cybcode.stix.ops.StiX_Ops.root;
import static org.cybcode.stix.ops.StiX_Ops.subr;
import static org.junit.Assert.assertEquals;

import org.cybcode.stix.ops.StiX_Ops;
import org.junit.Ignore;
import org.junit.Test;

public class StiXpressionsTest extends TestBase
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

	@Test public void test_parse_commutative()
	{
		assertEquals(6, B(add(div(constOf(1), constOf(2)), div(constOf(2), constOf(1)))).getNodeCount());
		assertEquals(5, B(add(mul(constOf(1), constOf(2)), mul(constOf(2), constOf(1)))).getNodeCount());
	}

	@Test public void test_parse_subroutine()
	{
		assertEquals(9, B(addA(subr(mux(constOf(2), constOf(1)), mul(constOf(3), StiX_Ops.<Long>subrRoot())))).getNodeCount());
		assertEquals(12, B(addA(
			subr(mux(constOf(2), constOf(1)), 
				subr(add(StiX_Ops.<Long>subrRoot(), constOf(3)), 
					mul(StiX_Ops.<Long>subrRoot(1), StiX_Ops.<Long>subrRoot()))))
		).getNodeCount());
	}

	@Test public void test_parse_subroutine_dedup()
	{
		assertEquals(13, B(addA(
			subr(mux(constOf(2), constOf(1)), 
				add(
					subr(add(StiX_Ops.<Long>subrRoot(), constOf(3)), mul(StiX_Ops.<Long>subrRoot(1), StiX_Ops.<Long>subrRoot())),
					subr(add(StiX_Ops.<Long>subrRoot(), constOf(3)), mul(StiX_Ops.<Long>subrRoot(1), StiX_Ops.<Long>subrRoot()))
				)
		))).getNodeCount());
	}

	@Ignore
	@Test public void test_subroutine()
	{
		assertEquals(13L, E(1L, addA(
			subr(mux(constOf(2), constOf(1)), 
				add(
					subr(add(StiX_Ops.<Long>subrRoot(), constOf(3)), mul(StiX_Ops.<Long>subrRoot(1), StiX_Ops.<Long>subrRoot())),
					subr(add(StiX_Ops.<Long>subrRoot(), constOf(3)), mul(StiX_Ops.<Long>subrRoot(1), StiX_Ops.<Long>subrRoot()))
				)
		))));
	}

	@Test public void test_aggregate()
	{
		assertEquals((Long) 2L, E(2L, addA(StiX_Ops.<Long>root())));
		assertEquals((Long) 2L, E(2L, addA(mux(StiX_Ops.<Long>root()))));
		assertEquals((Long) 3L, E(2L, addA(mux(StiX_Ops.<Long>root(), constOf(1)))));
		assertEquals((Long) 5L, E(2L, addA(mux(StiX_Ops.<Long>root(), constOf(1), StiX_Ops.<Long>root()))));
	}

	@Test public void test_full_AND()
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

	@Test public void test_early_AND()
	{
		assertEquals(Boolean.FALSE, E(2L, andIfNull(constOf(false), constOf(true), true)));
		assertEquals(4, stats.nodeCount);
		assertEquals(2, stats.evaluateCount);
		assertEquals(1, stats.pushAttemptCount);
		assertEquals(1, stats.pushEvaluateCount);
		
		assertEquals(Boolean.FALSE, EP(2L, andIfNull(constOf(false), constOf(true), true)));
		assertEquals(4, stats.nodeCount);
		assertEquals(2, stats.evaluateCount);
		assertEquals(1, stats.pushAttemptCount);
		assertEquals(1, stats.pushEvaluateCount);

		assertEquals(Boolean.FALSE, EP(2L, andIfNull(constOf(true), constOf(false), true)));
		assertEquals(4, stats.nodeCount);
		assertEquals(4, stats.evaluateCount);
		assertEquals(1, stats.pushAttemptCount);
		assertEquals(0, stats.pushEvaluateCount);
	}

	@Test public void test_aggregating_AND()
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

	//behavior on no-push
}