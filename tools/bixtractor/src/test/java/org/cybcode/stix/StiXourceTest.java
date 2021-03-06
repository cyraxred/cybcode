package org.cybcode.stix;

import static org.cybcode.stix.ops.StiX_Ops.addA;
import static org.cybcode.stix.ops.StiX_Ops.constOf;
import static org.cybcode.stix.ops.StiX_Ops.eq;
import static org.cybcode.stix.ops.StiX_Ops.first;
import static org.cybcode.stix.ops.StiX_Ops.ifNull;
import static org.cybcode.stix.ops.StiX_Ops.last;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXourceField;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.Multiplicity;
import org.cybcode.stix.core.xecutors.XecutorFailException;
import org.cybcode.stix.ops.StiX_Ops;
import org.cybcode.stix.xrc.pbuf.PbufFieldValue;
import org.cybcode.stix.xrc.pbuf.PbufFields;
import org.cybcode.stix.xrc.pbuf.PbufXource;
import org.junit.Test;

import com.google.protobuf.CodedOutputStream;

public class StiXourceTest extends TestBase
{
	@Test public void test_pbuf_first() throws IOException
	{
		assertEquals((Long) 19L, E(buildPbuf(), first(pbuf(StiX_Ops.<Binary>root(), PbufFields.INT, 21))));

		assertEquals(5, stats.nodeCount);
		assertEquals(1, stats.evaluateCount);
		assertEquals(2, stats.fieldCount);
		assertEquals(4, stats.pushAttemptCount);
		assertEquals(3, stats.pushEvaluateCount);

		assertEquals((Long) 19L, E(buildPbuf(), pbufFirst(StiX_Ops.<Binary>root(), PbufFields.INT, 21)));

		assertEquals(4, stats.nodeCount);
		assertEquals(1, stats.evaluateCount);
		assertEquals(2, stats.fieldCount);
		assertEquals(3, stats.pushAttemptCount);
		assertEquals(2, stats.pushEvaluateCount);
	}

	@Test public void test_pbuf_last() throws IOException
	{
		assertEquals((Long) 21L, E(buildPbuf(), last(pbuf(StiX_Ops.<Binary>root(), PbufFields.INT, 21))));

		assertEquals(5, stats.nodeCount);
		assertEquals(5, stats.evaluateCount);
		assertEquals(8, stats.fieldCount);
		assertEquals(10, stats.pushAttemptCount);
		assertEquals(6, stats.pushEvaluateCount);

		assertEquals((Long) 21L, E(buildPbuf(), pbufLast(StiX_Ops.<Binary>root(), PbufFields.INT, 21)));

		assertEquals(4, stats.nodeCount);
		assertEquals(4, stats.evaluateCount);
		assertEquals(8, stats.fieldCount);
		assertEquals(7, stats.pushAttemptCount);
		assertEquals(3, stats.pushEvaluateCount);
	}
	
	@Test public void test_pbuf_sum() throws IOException
	{
		assertEquals((Long) 60L, E(buildPbuf(), addA(pbuf(StiX_Ops.<Binary>root(), PbufFields.INT, 21))));

		assertEquals(5, stats.nodeCount);
		assertEquals(5, stats.evaluateCount);
		assertEquals(8, stats.fieldCount);
		assertEquals(10, stats.pushAttemptCount);
		assertEquals(6, stats.pushEvaluateCount);
	}

	@Test public void test_pbuf_no_value() throws IOException
	{
		assertEquals(null, E(buildPbuf(), first(pbuf(StiX_Ops.<Binary>root(), PbufFields.INT, 1))));
		assertEquals(null, E(buildPbuf(), last(pbuf(StiX_Ops.<Binary>root(), PbufFields.INT, 1))));
		assertEquals(null, E(buildPbuf(), addA(pbuf(StiX_Ops.<Binary>root(), PbufFields.INT, 1))));
	}
	
	@Test public void test_pbuf_only_value() throws IOException
	{
		assertEquals((Long) 22L, E(buildPbuf(), pbufOnly(StiX_Ops.<Binary>root(), PbufFields.INT, 22)));
		assertTrue(E(buildPbuf(), eq(constOf(22), pbufOnly(StiX_Ops.<Binary>root(), PbufFields.INT, 22))));
		assertEquals(null, E(buildPbuf(), pbufOnly(StiX_Ops.<Binary>root(), PbufFields.INT, 2)));
		assertEquals(null, E(buildPbuf(), eq(constOf(22), pbufOnly(StiX_Ops.<Binary>root(), PbufFields.INT, 2))));
	}

	
	@Test(expected = XecutorFailException.class) 
	public void test_pbuf_only_value_exception() throws IOException
	{
		assertEquals((Long) 19L, E(buildPbuf(), pbufOnly(StiX_Ops.<Binary>root(), PbufFields.INT, 21)));
	}
	
	@Test public void test_pbuf_ifNull() throws IOException
	{
		assertEquals((Long) 1L, E(buildPbuf(), ifNull(first(pbuf(StiX_Ops.<Binary>root(), PbufFields.INT, 1)), constOf(1))));
	}
	
	private static <T> StiXtractor<T> pbuf(StiXtractor<Binary> p0, StiXFunction<PbufFieldValue, T> fn, int... path)
	{
		PbufXource<Binary> result = StiX_Ops.pbuf(p0, Binary.PREPARE, path);
		return StiX_Ops.pbufRepeatedValue(result, fn);
	}

	private static <T> StiXtractor<T> pbufFirst(StiXtractor<Binary> p0, StiXFunction<PbufFieldValue, T> fn, int... path)
	{
		PbufXource<Binary> result = StiX_Ops.pbuf(p0, Binary.PREPARE, path);
		return StiX_Ops.pbufFirstValue(result, fn);
	}

	private static <T> StiXtractor<T> pbufLast(StiXtractor<Binary> p0, StiXFunction<PbufFieldValue, T> fn, int... path)
	{
		PbufXource<Binary> result = StiX_Ops.pbuf(p0, Binary.PREPARE, path);
		return StiX_Ops.pbufLastValue(result, fn);
	}

	private static <T> StiXtractor<T> pbufOnly(StiXtractor<Binary> p0, StiXFunction<PbufFieldValue, T> fn, int... path)
	{
		PbufXource<Binary> result = StiX_Ops.pbuf(p0, Binary.PREPARE, path);
		return new StiXourceField<>(result, Multiplicity.ONLY, fn);
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
}
