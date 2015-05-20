package org.cybcode.tools.bixtractor.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

import org.cybcode.tools.bixtractor.api.BiXtractor;
import org.cybcode.tools.bixtractor.api.XpressionConfiguration;

import com.google.common.base.Function;

public class BiXpression<T> extends AbstractList<BiXtractor<?>> implements RandomAccess, Function<Object, T>
{
	private List<OpNode> opNet;
	private XpressionConfiguration configuration;

	public static <T> BiXpression<T> flatten(BiXtractor<T> op)
	{
		BiXpression<T> result = new BiXpression<>(new ArrayList<OpNode>());
		BiXpressionBuilder.flatten(op, result.opNet);
		return result;
	}
	
	public static <T> BiXpression<T> flatten(BiXtractor<T> op, int capacity)
	{
		BiXpression<T> result = new BiXpression<>(new ArrayList<OpNode>(capacity));
		BiXpressionBuilder.flatten(op, result.opNet);
		return result;
	}
	
	BiXpression(List<OpNode> opNet)
	{
		this.opNet = opNet;
	}

	@Override public BiXtractor<?> get(int index)
	{
		return opNet.get(index).op;
	}

	@Override public int size()
	{
		return opNet.size();
	}
	
	public boolean isCompiled()
	{
		return opNet.isEmpty() || opNet.get(0).isCompiled();
	}

	public BiXpression<T> compile(BiXpressionOptimizer optimizer)
	{
		if (isCompiled()) throw new IllegalStateException();
		if (optimizer != null) {
			opNet = optimizer.optimize(opNet);
		}
		XpressionConfiguration ñfg = getConfiguration();
		for (OpNode node : opNet) {
			node.compile(ñfg);
		}
		return this;
	}
	
	private static final BiXpressionOptimizer DEFAULT_COMPILER = BiXpressionDeduplicator.getInstance();
	
	public BiXpression<T> compile()
	{
		return compile(DEFAULT_COMPILER);
	}
	
	@SuppressWarnings("unchecked")
	public T apply(Object rootValue, BiXpressionRunStats stats)
	{
		int count = opNet.size();
		stats.setTotalNodeCount(count);
		InternalXecutionContext context = new InternalXecutionContext(rootValue, count);
		
		count = 0;
		for (OpNode node : opNet) {
			count++;
			if (node.evaluate(context)) break;
		}
		stats.setEvaluatedNodeCount(count);
		return (T) context.getFinalResultValue();
	}
	
	private static final BiXpressionRunStats DUMMY = new BiXpressionRunStats();
		
	@Override public T apply(Object input)
	{
		return apply(input, DUMMY);
	}

	public XpressionConfiguration getConfiguration()
	{
		return configuration != null ? configuration : XpressionConfiguration.getDefault();
	}

	public BiXpression<T> setConfiguration(XpressionConfiguration configuration)
	{
		this.configuration = configuration;
		return this;
	}
	
	@Override public String toString()
	{
		if (isEmpty()) return "[]";
		return "[" + get(size() - 1) + "]";
	}
}