package org.cybcode.tools.bixtractor.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XpressionConfiguration;
import org.cybcode.tools.bixtractor.core.BiXpression;
import org.cybcode.tools.bixtractor.core.BiXpressionRunStats;
import org.cybcode.tools.bixtractor.pbuf.Binary;
import org.cybcode.tools.bixtractor.pbuf.PbufFieldConverter;
import org.cybcode.tools.bixtractor.pbuf.PbufXtractorField;
import org.cybcode.tools.bixtractor.pbuf.PbufXource;
import org.junit.Test;

import com.google.protobuf.CodedOutputStream;

public class ExpTest
{
	private BiXpressionRunStats	lastStats;

	@Test public void test_const()
	{
		assertEquals(1.1d, E(1, new ConstXtractor<>(1.1d)));
		assertEquals(lastStats.getTotalNodeCount(), lastStats.getEvaluatedNodeCount()); 
	}
	
	@Test public void test_add()
	{
		assertEquals(2.1d, E(1, new AddXtractor(L(C(1.1d), C(1d), C(0d)))));
		assertEquals(lastStats.getTotalNodeCount(), lastStats.getEvaluatedNodeCount()); 
	}

	@Test public void test_add_neg()
	{
		assertEquals(2d, E(1, new AddXtractor(L(C(3d), new NegXtractor(C(1d)), C(0d)))));
		assertEquals(lastStats.getTotalNodeCount(), lastStats.getEvaluatedNodeCount()); 
	}
	
	@Test public void test_eq()
	{
		assertEquals(true, E(1, new CmpXtractor(CmpXtractor.CmpType.EQ, C(1.1d), C(1.1d))));
		assertEquals(1, lastStats.getEvaluatedNodeCount()); 
	}
	
	@Test public void test_eq_not()
	{
		assertEquals(false, E(1, new CmpXtractor(CmpXtractor.CmpType.EQ, C(1.1d), C(1d))));
		assertEquals(2, lastStats.getEvaluatedNodeCount()); 
	}
	
	@Test public void test_and()
	{
		assertEquals(true, E(1, new AndXtractor(L(C(true))))); 
		assertEquals(lastStats.getTotalNodeCount(), lastStats.getEvaluatedNodeCount());
		
		assertEquals(false, E(1, new AndXtractor(L(C(false))))); 
		assertEquals(1, lastStats.getEvaluatedNodeCount()); 

		assertEquals(false, E(1, new AndXtractor(L(C(true), C(false))))); 
		assertTrue(lastStats.getEvaluatedNodeCount() <= 2); 

		assertEquals(true, E(1, new AndXtractor(L(C(true), C(true))))); 
		assertEquals(lastStats.getTotalNodeCount(), lastStats.getEvaluatedNodeCount()); 
	}
	
	@Test public void test_and_fast()
	{
		assertEquals(false, E(1, new AndXtractor(L(new CmpXtractor(CmpXtractor.CmpType.EQ, C(1.1d), C(1.1d)), C(false))))); 
		assertEquals(1, lastStats.getEvaluatedNodeCount()); 
	}

	@Test public void test_verbatim()
	{
		assertEquals(1.1f, E(1.1f, RootXtractor.getInstance())); 
	}
	
	@Test public void test_source() throws Exception
	{
		assertEquals(true, E(buildPbuf(),
			new CmpXtractor(CmpXtractor.CmpType.EQ, C("TEST"), 
				new PbufXtractorField<>(20, PbufFieldConverter.STRING,
					new PbufXource(RootXtractor.<Binary>getInstance(), false)))));
		assertEquals(2, lastStats.getEvaluatedNodeCount()); //resolved by evaluation of Const, Verbatim 
	}
	
	@Test public void test_source_lazy() throws Exception
	{
		assertEquals(true, E(buildPbuf(),
			new CmpXtractor(CmpXtractor.CmpType.EQ, C("TEST"), 
				new PbufXtractorField<>(20, PbufFieldConverter.STRING,
					new PbufXource(RootXtractor.<Binary>getInstance(), true)))));
		assertEquals(3, lastStats.getEvaluatedNodeCount()); //resolved by evaluation of Const, Verbatim, Source
	}
	
	@Test public void test_source_no_push() throws Exception
	{
		assertEquals(true, ENP(buildPbuf(),
			new CmpXtractor(CmpXtractor.CmpType.EQ, C("TEST"), 
				new PbufXtractorField<>(20, PbufFieldConverter.STRING,
					new PbufXource(RootXtractor.<Binary>getInstance(), false)))));
		assertEquals(lastStats.getTotalNodeCount(), lastStats.getEvaluatedNodeCount());
	}

	@Test public void test_source_lazy_no_push() throws Exception
	{
		assertEquals(true, ENP(buildPbuf(),
			new CmpXtractor(CmpXtractor.CmpType.EQ, C("TEST"), 
				new PbufXtractorField<>(20, PbufFieldConverter.STRING,
					new PbufXource(RootXtractor.<Binary>getInstance(), true)))));
		assertEquals(lastStats.getTotalNodeCount(), lastStats.getEvaluatedNodeCount());
	}
	
	
	private static Binary buildPbuf() throws IOException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream(32);
		CodedOutputStream cos = CodedOutputStream.newInstance(os);
		
		cos.writeString(20, "TEST");
		cos.writeInt32(21, 21);
		cos.writeFixed32(22, 22);
		cos.writeFixed64(23, 23);
		cos.writeDouble(24, 1.5d);
		cos.writeFloat(25, 2.5f);
		
		cos.flush();		
		return new Binary(os.toByteArray());
	}

	private Object E(Object value, BiXtractor<?> x)
	{
		lastStats = new BiXpressionRunStats();
		return BiXpression.flatten(x).compile().apply(value, lastStats);
	}
	
	private Object ENP(Object value, BiXtractor<?> x)
	{
		lastStats = new BiXpressionRunStats();
		return BiXpression.flatten(x).setConfiguration(XpressionConfiguration.getSafe()).compile().apply(value, lastStats);
	}
	
	private static <T> BiXtractor<T> C(T value)
	{
		return new ConstXtractor<T>(value);
	}
	
	@SafeVarargs
	private static <T> List<T> L(T... values)
	{
		return Arrays.asList(values);
	}
}
