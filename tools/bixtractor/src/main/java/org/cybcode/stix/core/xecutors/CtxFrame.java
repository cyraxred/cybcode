package org.cybcode.stix.core.xecutors;

import java.util.List;

class CtxFrame
{
	private final int frameLevel;
	private int rootIndex;
	private int resultIndex;
	private boolean hasResult;
	
	private CtxFrame(int rootIndex, int resultIndex, CtxFrame outerFrame)
	{
		this.rootIndex = rootIndex;
		this.resultIndex = resultIndex;
		this.frameLevel = outerFrame.frameLevel + 1;
	}
	
	CtxFrame(int rootIndex, int resultIndex)
	{
		this.rootIndex = rootIndex;
		this.resultIndex = resultIndex;
		this.frameLevel = 0;
	}
	
	public int getRootIndex()
	{
		if (rootIndex == Integer.MAX_VALUE) throw new IllegalStateException();
		return rootIndex;
	}

	public int getResultIndex()
	{
		if (rootIndex == Integer.MAX_VALUE) throw new IllegalStateException();
		return resultIndex;
	}

	public CtxFrame createInner(int rootIndex, int resultIndex, List<CtxFrame> stack)
	{
		if (rootIndex <= this.rootIndex || resultIndex >= this.resultIndex || rootIndex > resultIndex) throw new IllegalArgumentException();

		if (frameLevel == stack.size() - 1) {
			CtxFrame innerFrame = new CtxFrame(rootIndex, resultIndex, this);
			stack.add(innerFrame);
			return innerFrame;
		}
		
		CtxFrame innerFrame = stack.get(frameLevel + 1);
		if (rootIndex != Integer.MAX_VALUE) throw new IllegalStateException();
		innerFrame.rootIndex = rootIndex;
		innerFrame.resultIndex = resultIndex;
		
		return innerFrame;
	}
	
	public void closeFrame()
	{
		if (rootIndex == Integer.MAX_VALUE) throw new IllegalStateException();
		if (frameLevel == 0) throw new IllegalStateException();
		cleanState();
	}

	private void cleanState()
	{
		rootIndex = Integer.MAX_VALUE;
		resultIndex = Integer.MIN_VALUE;
		hasResult = false;
	}

	public boolean setHasResult(int xtractorIndex)
	{
		if (xtractorIndex != resultIndex) return false;
		hasResult = true;
		return true;
	}
	
	public static CtxFrame closeAllFrames(List<CtxFrame> stack)
	{
		for (int i = stack.size() - 1; i >= 1; i--) {
			stack.get(i).cleanState();
		}
		CtxFrame result = stack.get(0);
		result.hasResult = false;
		return result;
	}
	
	public boolean hasResult()
	{
		return hasResult;
	}
	
	public boolean isInnerFrame()
	{
		return frameLevel > 0;
	}
	
	public boolean isEndOfFrame(int xtractorIndex)
	{
		return resultIndex == xtractorIndex;
	}
}