package org.cybcode.stix.core.xecutors;

class ContextFrame
{
	private final ContextFrame outerFrame;
	private int rootIndex;
	private int resultIndex;
	private ContextFrame innerFrame;
	private boolean hasResult;
	
	ContextFrame(int rootIndex, int resultIndex, ContextFrame outerFrame)
	{
		this.rootIndex = rootIndex;
		this.resultIndex = resultIndex;
		this.outerFrame = outerFrame;
	}
	
	public int getRootIndex()
	{
		if (rootIndex == Integer.MAX_VALUE) throw new IllegalStateException();
		return rootIndex;
	}

	public int getResultIndex()
	{
		if (resultIndex == Integer.MIN_VALUE) throw new IllegalStateException();
		return resultIndex;
	}

	public ContextFrame createInner(int rootIndex, int resultIndex)
	{
		if (rootIndex <= this.rootIndex || resultIndex >= this.resultIndex || rootIndex > resultIndex) throw new IllegalArgumentException();
		
		if (innerFrame == null) {
			ContextFrame result = new ContextFrame(rootIndex, resultIndex, this);
			this.innerFrame = result;
			return result;
		}
		innerFrame.rootIndex = rootIndex;
		innerFrame.resultIndex = resultIndex;
		
		return innerFrame;
	}
	
	public ContextFrame closeFrame()
	{
		if (rootIndex < 0) throw new IllegalStateException();
		if (outerFrame == null || outerFrame.innerFrame != this) throw new IllegalStateException();
		cleanState();
		return outerFrame;
	}

	private void cleanState()
	{
		rootIndex = Integer.MAX_VALUE;
		resultIndex = Integer.MIN_VALUE;
	}

	public void setFinalIfResult(int xtractorIndex)
	{
		if (xtractorIndex != resultIndex) return;
		hasResult = true;			
	}
	
	public ContextFrame closeAllFrames()
	{
		ContextFrame result = this;
		while (result.outerFrame != null) {
			cleanState();
			result = result.outerFrame;
		}
		return result;
	}
	
	public boolean hasResult()
	{
		return hasResult;
	}
	
	public boolean isInnerFrame()
	{
		return outerFrame != null;
	}
	
	public boolean isEndOfFrame(int xtractorIndex)
	{
		return resultIndex == xtractorIndex;
	}
}