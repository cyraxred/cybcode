package org.cybcode.stix;

import static org.cybcode.stix.ops.StiX_Ops.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXourceField;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.compiler.StiXpressionRecursiveParser;
import org.cybcode.stix.core.xecutors.StiXecutorDefaultContext;
import org.cybcode.stix.core.xecutors.StiXecutorDefaultContextBuilder;
import org.cybcode.stix.core.xecutors.StiXpressionSequencer;
import org.cybcode.stix.ops.StiX_Ops;
import org.cybcode.stix.xrc.pbuf.PbufFieldValue;
import org.cybcode.stix.xrc.pbuf.PbufFields;
import org.cybcode.stix.xrc.pbuf.PbufXource;
import org.junit.Test;

import com.google.protobuf.CodedOutputStream;

public class StiXourceTest
{
	private StatsCollector stats = new StatsCollector();
	
	@Test public void test_pbuf_last() throws IOException
	{
		assertEquals((Long) 21L, E(buildPbuf(), last(pbuf(StiX_Ops.<Binary>root(), PbufFields.INT, 21))));

		assertEquals(5, stats.nodeCount);
		assertEquals(2, stats.evaluateCount);
		assertEquals(8, stats.fieldCount);
		assertEquals(10, stats.pushAttemptCount);
		assertEquals(7, stats.pushEvaluateCount);
	}
	
	@Test public void test_pbuf_first() throws IOException
	{
		assertEquals((Long) 19L, E(buildPbuf(), first(pbuf(StiX_Ops.<Binary>root(), PbufFields.INT, 21))));

		assertEquals(5, stats.nodeCount);
		assertEquals(1, stats.evaluateCount);
		assertEquals(2, stats.fieldCount);
		assertEquals(4, stats.pushAttemptCount);
		assertEquals(4, stats.pushEvaluateCount);
	}

	@Test public void test_pbuf_sum() throws IOException
	{
		assertEquals((Long) 60L, E(buildPbuf(), addA(pbuf(StiX_Ops.<Binary>root(), PbufFields.INT, 21))));

		assertEquals(5, stats.nodeCount);
		assertEquals(2, stats.evaluateCount);
		assertEquals(8, stats.fieldCount);
		assertEquals(10, stats.pushAttemptCount);
		assertEquals(7, stats.pushEvaluateCount);
	}

	private static <T> StiXtractor<T> pbuf(StiXtractor<Binary> p0, StiXFunction<PbufFieldValue, T> fn, int... path)
	{
		PbufXource<Binary> result = new PbufXource<Binary>(p0, Binary.PREPARE, PbufXource.ValueLimit.ALL);
		for (int id : path) {
			result = new PbufXource<Binary>(result, id, PbufXource.ValueLimit.ALL);
		}
		return StiXourceField.newRepeatedValue(result, fn);
	}

	private static Binary buildPbuf() throws IOException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream(32);
		CodedOutputStream cos = CodedOutputStream.newInstance(os);
		
		cos.writeString(20, "TEST");
		cos.writeInt32(21, 19);
		cos.writeInt32(21, 20);
		cos.writeInt32(21, 21);
		cos.writeFixed32(22, 22);
		cos.writeFixed64(23, 23);
		cos.writeDouble(24, 1.5d);
		cos.writeFloat(25, 2.5f);
		
		cos.flush();		
		return new Binary(os.toByteArray());
	}

	//behavior on no-push

	static <T> StiXecutorDefaultContext B(StiXtractor<T> expression)
	{
		return B(false, expression);
	}
	
	private static <T> StiXecutorDefaultContext B(boolean regularAsNotify, StiXtractor<T> expression)
	{
		StiXpressionRecursiveParser parser = new StiXpressionRecursiveParser();
		parser.step1_buildTree(expression);
		parser.step2_optimizeTree();
		parser.step3_linkTree();
		parser.step4_optimizeLinkedTree();
		StiXecutorDefaultContextBuilder builder = new StiXecutorDefaultContextBuilder();
		builder.setRegularAsNotify(regularAsNotify);
		parser.step5_flattenTree(builder);
		return builder.build();
	}

	private <T> T E(boolean regularAsNotify, Object rootValue, StiXtractor<T> expression)
	{
		StiXecutorDefaultContext context = B(regularAsNotify, expression);
		context.setStatsCollector(stats);
		StiXpressionSequencer sequencer = new SimpleXpressionSequencer();
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