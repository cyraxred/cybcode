package org.cybcode.tools.bixtractor.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

import org.cybcode.tools.bixtractor.api.BiXtractor;

import com.google.common.base.Function;

public class BiXpression<T> extends AbstractList<BiXtractor<?>> implements RandomAccess, Function<Object, T>
{
	private List<OpNode> opNet;
	private boolean enableEarlyCompletion;

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
		} else {
			Collections.reverse(opNet);
			int sz = opNet.size() - 1;
			for (OpNode node : opNet) {
				node.nodeIndex = sz - node.nodeIndex;
			}
		}
		for (OpNode node : opNet) {
			node.compile();
		}
		return this;
	}
	
	private static final BiXpressionOptimizer DEFAULT_COMPILER = new BiXpressionOptimizer()
	{
		@Override public List<OpNode> optimize(List<OpNode> opNet)
		{
			return new OpNetOptimizer(opNet).getResult();
		}
	};			
	
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

	public boolean isEnableEarlyCompletion()
	{
		return enableEarlyCompletion;
	}

	public void setEnableEarlyCompletion(boolean enableEarlyCompletion)
	{
		this.enableEarlyCompletion = enableEarlyCompletion;
	}
}