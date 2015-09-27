package org.cybcode.stix.core.xecutors;

import java.util.BitSet;

import org.cybcode.stix.api.StiXpressionSequencer;

class XecutorRunnerFrame
{
	class ResultMarker
	{
		private final int resultIndex;
		private final int endIndex;

		private ResultMarker(int resultIndex, int endIndex)
		{
			this.resultIndex = resultIndex;
			this.endIndex = endIndex;
		}

		public int getEndIndex()
		{
			return endIndex;
		}
		
		public boolean setFinalState()
		{
			if (!results.get(resultIndex)) return false;
			results.clear(resultIndex);
			if (!results.isEmpty()) return false;
			isFinal = true;
			return true;
		}

		public XecutorRunnerFrame getFrame()
		{
			return XecutorRunnerFrame.this;
		}
	}
	
	private final XecutorRunnerFrame outerFrame;
	private final StiXpressionSequencer sequencer;
	private final int frameLevel;
	private final int startIndex;
	private int endIndex;
	
	private BitSet results;
	private int resultCount;
	
	private boolean isFinal;
	private int	returnPosition;
	
	XecutorRunnerFrame(int startIndex, XecutorRunnerFrame outerFrame, StiXpressionSequencer sequencer)
	{
		if (startIndex <= 0) throw new IllegalArgumentException();
		this.frameLevel = outerFrame.frameLevel + 1;
		this.outerFrame = outerFrame;
		this.startIndex = startIndex;
		this.sequencer = sequencer;
	}
	
	XecutorRunnerFrame(int endIndex, StiXpressionSequencer sequencer)
	{
		if (endIndex < 0) throw new IllegalArgumentException();
		this.frameLevel = 0;
		this.outerFrame = null;
		this.startIndex = 0;
		this.endIndex = endIndex;
		this.sequencer = sequencer;
	}
	
	public int getStartIndex()
	{
		return startIndex;
	}

	public int getEndIndex()
	{
		if (endIndex == 0) throw new IllegalStateException();
		return endIndex;
	}
	
	public ResultMarker registerFrameResult(int resultIndex)
	{
		if (results != null) throw new IllegalStateException();
		if (resultIndex < startIndex) throw new IllegalArgumentException();
		resultIndex++;
		endIndex = Math.max(endIndex, resultIndex);
		return new ResultMarker(resultCount++, resultIndex);
	}

	private void enterFrame()
	{
		sequencer.resetSequencer();
		if (results == null) {
			if (resultCount == 0) throw new IllegalStateException();
			results = new BitSet(resultCount);
		} else {
			results.set(0, resultCount);
		}
		isFinal = false;
		returnPosition = 0;
	}
	
	public void resetFrame()
	{
		resetFrame(false);
	}
	
	public void resetFrame(boolean finalState)
	{
		if (results == null) throw new IllegalStateException();
		results.clear();
		isFinal = finalState;
		returnPosition = 0;
	}
	
	public boolean hasFinalState()
	{
		return isFinal;
	}
	
	public boolean isInnerFrame()
	{
		return frameLevel > 0;
	}
	
	public XecutorRunnerFrame getOuterFrame()
	{
		return outerFrame;
	}

	public boolean isInsideFrame(int xtractorIndex)
	{
		return xtractorIndex >= startIndex && xtractorIndex < endIndex;
	}

	public XecutorRunnerFrame enterFrame(XecutorRunnerFrame frame)
	{
		if (frame == this) {
			//self entry - is only allowed for outer frame
			if (isInnerFrame()) throw new IllegalStateException();
			if (resultCount == 0) {
				resultCount++;
			}
		} else {
			if (frame.getOuterFrame() != this) throw new IllegalStateException();
		}
		if (results != null && !results.isEmpty()) throw new IllegalStateException();
		frame.enterFrame();
		return frame;
	}

	public void setReturnPosition(int position)
	{
		returnPosition = position;
	}

	public int getReturnPosition()
	{
		return returnPosition;
	}

	public StiXpressionSequencer getSequencer()
	{
		return sequencer;
	}
}

