package org.cybcode.stix.core.xecutors;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXecutorContextInspector;
import org.cybcode.stix.api.StiXecutorStatsCollector;
import org.cybcode.stix.api.StiXpressionNode;
import org.cybcode.stix.api.StiXpressionSequencer;
import org.cybcode.stix.api.StiXpressionNode.PushTarget;
import org.cybcode.stix.api.StiXtractor.Parameter;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

class XecutorContextRunner implements Function<Object, Object>, StiXecutorContextInspector
{
	private final Supplier<StiXpressionSequencer> sequencerSupplier;
	private final XecutorRunnerNode[] nodes;
	private final XecutorRunnerFrame outerFrame;
	private final StiXecutorStatsCollector stats;
	
	private int nextIndex;

	public XecutorContextRunner(Collection<? extends XecutorContextNode> contextNodes, Supplier<StiXpressionSequencer> sequencerSupplier, StiXecutorStatsCollector stats)
	{
		int nodeCount = contextNodes.size();
		if (nodeCount <= 0) throw new IllegalArgumentException();
		this.sequencerSupplier = sequencerSupplier;
		this.nodes = new XecutorRunnerNode[nodeCount];
		this.outerFrame = new XecutorRunnerFrame(nodeCount, sequencerSupplier.get());
		this.stats = stats == null ? StiXecutorStatsCollector.NULL : stats;
		createInitialState(contextNodes);
	}

	private void resetContext(Object rootValue)
	{
		if (rootValue == null) throw new NullPointerException();
		stats.resetStats(nodes.length);

		/* Imitates execution of the root node  */
		outerFrame.enterFrame(outerFrame);
		XecutorRunnerFramedNode rootNode = (XecutorRunnerFramedNode) nodes[0]; 
		rootNode.init(rootValue);
		stats.onEvaluated(rootNode.getXpressionNode(), rootValue);
		nextIndex = 1;
	}
	
	private void createInitialState(Collection<? extends XecutorContextNode> contextNodes)
	{
		XecutorConstructionContext context = new XecutorConstructionContext(stats);
		Iterator<? extends XecutorContextNode> iter = contextNodes.iterator();
		XecutorContextNode contextNode = iter.next();

		if (contextNode.getXpressionNode().createXecutor(context) != null) {
			throw new IllegalArgumentException("First node must be a final value node");
		}
		if (contextNode.getIndex() != 0) {
			throw new IllegalArgumentException("Nodes must be ordered by index");
		}
		XecutorRunnerNode node = new XecutorRunnerRootNode(contextNode, outerFrame); 
		nodes[0] = node; 
		
		int index = 0;
		while (iter.hasNext()) {
			index++;
			contextNode = iter.next();
			if (contextNode.getIndex() != index) {
				throw new IllegalArgumentException("Nodes must be ordered by index");
			}
			if (nodes[index] != null) throw new IllegalStateException("Duplicate index=" + index);
			StiXpressionNode xpressionNode = contextNode.getXpressionNode();
			context.setNode(xpressionNode);
			StiXecutor xecutor = xpressionNode.createXecutor(context);
			if (xecutor == null) {
				node = createRunnerNode(contextNode, null, node.getFrame(), !iter.hasNext());
			} else {
				node = createRunnerNode(contextNode, node, xecutor, !iter.hasNext());
			}
			nodes[index] = node;
		}
	}

	private XecutorRunnerNode createRunnerNode(XecutorContextNode contextNode, XecutorRunnerNode prevNode, StiXecutor xecutor, boolean isLast)
	{
		XecutorRunnerFrame frame = prevNode.getFrame();

		int nodeIndex = contextNode.getIndex();
		
		StiXpressionNode xpressionNode = contextNode.getXpressionNode();
		
		if (xecutor == DefaultXecutors.FRAME_START) {
			if (isLast) throw new IllegalStateException("Frame result node is missing");
			if (xpressionNode.getFrameOwnerIndex() != nodeIndex) {
				throw new IllegalStateException("Node is not an frame entry, index=" + nodeIndex);
			}
			XecutorRunnerFrame innerFrame = new XecutorRunnerFrame(nodeIndex, frame, sequencerSupplier.get());
			return new FrameStartNode(contextNode, innerFrame);
		} 

		if (!prevNode.isValidFrameOfNextNode(xpressionNode.getFrameOwnerIndex())) {
			throw new IllegalStateException("Inconsistent frame sequence, currentFrame=" + frame.getStartIndex() +
					", nodeFrame=" + xpressionNode.getFrameOwnerIndex() + ", nodeIndex=" + nodeIndex);
		}
		
		if (xecutor == DefaultXecutors.FRAME_RESULT) {
			XecutorRunnerFrame.ResultMarker frameResult = frame.registerFrameResult(nodeIndex);
			return new FrameResultNode(contextNode, frameResult);
		} 
		
		return createRunnerNode(contextNode, xecutor, frame, isLast);
	}

	private XecutorRunnerNode createRunnerNode(XecutorContextNode contextNode, StiXecutor xecutor, XecutorRunnerFrame frame, boolean isLast)
	{
		boolean hasCallbacks = !contextNode.getXpressionNode().getCallbackTargets().isEmpty();
		
		if (isLast) {
			if (hasCallbacks) throw new IllegalStateException("Xource can't be an expression result");
			return new XecutorRunnerResultNode(contextNode, frame, xecutor);
		}
		if (hasCallbacks) {
			if (xecutor == null) throw new IllegalStateException("Xource must define a Xecutor");
			return new XourceNode(contextNode, frame, xecutor);
		}
		
		return new XecutorRunnerFramedNode(contextNode, frame, xecutor);
	}

	private class XourceNode extends XecutorRunnerFramedNode 
	{
		public XourceNode(XecutorContextNode contextNode, XecutorRunnerFrame frame, StiXecutor initialState)
		{
			super(contextNode, frame, initialState);
		}

		@Override public void evaluateDirectPush(List<PushTarget> targets, Object pushedValue)
		{
			if (targets.isEmpty() || pushedValue == null) return;
			for (PushTarget target : targets) {
				XecutorContextRunner.this.evaluatePush(this, target, pushedValue);
			}
			executePostponedTargets(this);		
		}
	
		@Override public void evaluateDirectPush(PushTarget target, Object pushedValue)
		{
			if (pushedValue == null) return;
			XecutorContextRunner.this.evaluatePush(this, target, pushedValue);
			executePostponedTargets(this);		
		}
	}
	
	private abstract class FrameNode extends XecutorRunnerFramedNode 
	{
		public FrameNode(XecutorContextNode contextNode, XecutorRunnerFrame frame) { super(contextNode, frame, null); }

		@Override public void setNextState(StiXecutor xecutor)
		{
			throw new IllegalStateException();
		}

		public boolean isValidFrameOfNextNode(int frameOwnerIndex)
		{
			if (super.isValidFrameOfNextNode(frameOwnerIndex)) return true;
			XecutorRunnerFrame outerFrame = getFrame().getOuterFrame();
			return outerFrame != null && outerFrame.getStartIndex() == frameOwnerIndex;
		}
	}
	
	private class FrameResultNode extends FrameNode
	{
		private final XecutorRunnerFrame.ResultMarker frameResult;
		
		public FrameResultNode(XecutorContextNode contextNode, XecutorRunnerFrame.ResultMarker frameResult)
		{
			super(contextNode, frameResult.getFrame());
			this.frameResult = frameResult;
		}

		@Override protected Object internalEvaluateFinal()
		{
			evaluatePush(false);
			return super.evaluateFinalState();
		}
		
		@Override protected Object internalEvaluatePush(Parameter<?> targetParam, Object pushedValue)
		{
			evaluatePush(targetParam.isRepeatable());
			return pushedValue;
		}
		
		private void evaluatePush(boolean repeatable)
		{
			int startIndex = frame.getStartIndex();
			if (!frameResult.setFinalState()) {
				stats.onFrameResolve(startIndex, false);
				return;
			}
			//TODO set final on all inner frames when an outer one is resolved

			storage.clear(startIndex, repeatable ? getInitialState() : DefaultXecutors.FINAL);
			stats.onFrameResolve(startIndex, true);
			int returnPos = frame.getReturnPosition();
			if (returnPos == 0) return;
			jumpTo(returnPos);
			frame.setReturnPosition(0);
		}
	}

//	private void resolveInnerFrames(RunnerFrame resolvedFrame)
//	{
//		RunnerFrame frame = currentFrame;
//		while (frame != resolvedFrame) {
//			frame = frame.getOuterFrame();
//			if (frame == outerFrame) {
//				throw new IllegalStateException("Inactive frame was resolved");
//			}
//		}
//	}

	private class FrameStartNode extends FrameNode
	{
		public FrameStartNode(XecutorContextNode contextNode, XecutorRunnerFrame frame)
		{
			super(contextNode, frame);
		}
		
		@Override public void validatePushFinalState()
		{
			throw new IllegalStateException("An incomplete or finalized frame is reentered");
		}
		
		@Override public Object evaluateFinalState()
		{
			int startIndex = frame.getStartIndex(); 
			int endIndex = frame.getEndIndex();
			stats.onFrameSkip(startIndex);
			storage.clearRange(startIndex, endIndex, DefaultXecutors.FINAL);
			jumpTo(endIndex);
			return null;
		}
		
		@Override public Object evaluatePush(XecutorRunnerNode originNode, Parameter<?> targetParam, Object pushedValue)
		{
			frame.enterFrame(originNode.getFrame());
			return super.evaluatePush(originNode, targetParam, pushedValue);
		}

		@Override protected Object internalEvaluatePush(Parameter<?> targetParam, Object pushedValue)
		{
			int startIndex = frame.getStartIndex(); 
			int endIndex = frame.getEndIndex();
			stats.onFrameEnter(startIndex);
			getSequencer().resetSequencer();
			storage.clearRange(startIndex, endIndex, null);
			setFinalValue(pushedValue);
			int returnPos = jumpTo(endIndex);
			frame.setReturnPosition(returnPos);
			return pushedValue;
		}
	}
	
	public boolean evaluatePush(StiXpressionNode.PushTarget target)
	{
		int index = target.getXtractorIndex();
		XecutorRunnerNode node = nodes[index];
		if (node.isFinalState()) {
			node.validatePushFinalState();
			return false;
		}

		XecutorRunnerNode originNode = nodes[target.getValueIndex()];
		Object pushedValue = originNode.getPushOrFinalValue();
		stats.onPushAttempt(node.getXpressionNode(), target.getXtractorParam(), pushedValue);
		if (pushedValue == null) return false;
		Object resultValue = node.evaluatePush(originNode, target.getXtractorParam(), pushedValue);
		if (resultValue == null) return false;
		stats.onPushEvaluated(node.getXpressionNode(), resultValue);
		return true;
	}

	public boolean evaluatePush(XecutorRunnerNode originNode, StiXpressionNode.PushTarget target, Object pushedValue)
	{
		int index = target.getXtractorIndex();
		XecutorRunnerNode node = nodes[index];
		if (node.isFinalState()) {
			node.validatePushFinalState();
			return false;
		}

		stats.onPushAttempt(node.getXpressionNode(), target.getXtractorParam(), pushedValue);
		if (pushedValue == null) return false;
		Object resultValue = node.evaluatePush(originNode, target.getXtractorParam(), pushedValue);
		if (resultValue == null) return false;
		stats.onPushEvaluated(node.getXpressionNode(), resultValue);
		return true;
	}

	public boolean executePostponedTargets(XecutorRunnerNode node)
	{
		StiXpressionSequencer sequencer = node.getSequencer();
		StiXpressionNode xpressionNode = node.getXpressionNode();
		int nodeIndex = node.index;

		while (!node.hasFrameFinalState()) {
			StiXpressionNode.PushTarget pushTarget = sequencer.nextImmediateTarget();
			if (pushTarget == null) {
				while (true) {
					pushTarget = sequencer.nextPostponedTargetBefore(xpressionNode);
					if (pushTarget == null) return false;
					if (pushTarget.getXtractorIndex() > nodeIndex || pushTarget.getXtractorParam().getBehavior().isMandatory()) break;
				}
			}
			evaluatePush(pushTarget);
		}
		return true;
	}

	public Object apply(Object rootValue)
	{
		resetContext(rootValue);
		runExpression();
		return nodes[nodes.length - 1].getFinalValue();
	}

	private void runExpression()
	{
		while (nextIndex < nodes.length && !outerFrame.hasFinalState()) {
			int currentIndex = nextIndex++;
			XecutorRunnerNode node = nodes[currentIndex];
			
			executePostponedTargets(node);
			if (node.isFinalState()) continue;
			if (outerFrame.hasFinalState()) break;
			Object finalValue = node.evaluateFinalState();
			stats.onEvaluated(node.getXpressionNode(), finalValue);
		}
	}
	
	private int jumpTo(int index)
	{
		if (index <= 0) throw new IllegalArgumentException();
		if (nextIndex == index) return -1;
		int result = nextIndex; 
		nextIndex = index;
		return result;
	}

	@Override public int getNodeCount()
	{
		return nodes.length;
	}

	@Override public StiXpressionNode getNode(int xtractorIndex)
	{
		return nodes[xtractorIndex].getXpressionNode();
	}
}

