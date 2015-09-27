package org.cybcode.stix.core.xecutors;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cybcode.stix.api.StiXecutor;
import org.cybcode.stix.api.StiXtractor.Parameter;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

class XecutorContextRunner implements Function<Object, Object>, StiXecutorContextInspector
{
	private final Supplier<StiXpressionSequencer> sequencerSupplier;
	private final XecutorContextRunnerNode[] nodes;
	private final RunnerFrame outerFrame;

	private StiXecutorStatsCollector stats;
	
	private int nextIndex;

	public XecutorContextRunner(Collection<? extends XecutorContextNode> contextNodes, Supplier<StiXpressionSequencer> sequencerSupplier)
	{
		int nodeCount = contextNodes.size();
		if (nodeCount <= 0) throw new IllegalArgumentException();
		this.sequencerSupplier = sequencerSupplier;
		this.nodes = new XecutorContextRunnerNode[nodeCount];
		this.outerFrame = new RunnerFrame(nodeCount, sequencerSupplier.get());
		createInitialState(contextNodes);
	}

	public void setStatsCollector(StiXecutorStatsCollector stats)
	{
		this.stats = stats == null ? StiXecutorStatsCollector.NULL : stats;
	}
	
	private void resetContext(Object rootValue)
	{
		if (rootValue == null) throw new NullPointerException();
		stats.resetStats(nodes.length);

		outerFrame.enterFrame(outerFrame);
		XecutorContextRunnerFramedNode rootNode = (XecutorContextRunnerFramedNode) nodes[0]; 
		rootNode.init(rootValue);
		stats.onEvaluated(rootNode.getXpressionNode(), rootValue);
		nextIndex = 1;
	}
	
	private void createInitialState(Collection<? extends XecutorContextNode> contextNodes)
	{
		XecutorConstructionContext context = new XecutorConstructionContext();
		Iterator<? extends XecutorContextNode> iter = contextNodes.iterator();
		XecutorContextNode contextNode = iter.next();

		if (contextNode.getXpressionNode().createXecutor(context) != null) {
			throw new IllegalArgumentException("First node must be a final value node");
		}
		if (contextNode.getIndex() != 0) {
			throw new IllegalArgumentException("Nodes must be ordered by index");
		}
		XecutorContextRunnerNode node = new XecutorContextRunnerRootNode(contextNode, outerFrame); 
		nodes[0] = node; 
		
		int index = 0;
		while (iter.hasNext()) {
			index++;
			contextNode = iter.next();
			if (contextNode.getIndex() != index) {
				throw new IllegalArgumentException("Nodes must be ordered by index");
			}
			if (nodes[index] != null) throw new IllegalStateException("Duplicate index=" + index);
			node = createInitialState(context, contextNode, node, !iter.hasNext());
			nodes[index] = node;
		}
	}

	private XecutorContextRunnerNode createInitialState(XecutorConstructionContext context, XecutorContextNode contextNode, 
		XecutorContextRunnerNode prevNode, boolean isLast)
	{
		StiXpressionNode xpressionNode = contextNode.getXpressionNode();
		context.setNode(xpressionNode);
		StiXecutor xecutor = xpressionNode.createXecutor(context);

		RunnerFrame frame = prevNode.getFrame();
		if (xecutor == null) {
			if (isLast) {
				return new XecutorContextRunnerResultNode(contextNode, frame, xecutor);
			} 
			return new XecutorContextRunnerStatelessNode(contextNode, frame);
		} 

		int nodeIndex = contextNode.getIndex();
		
		if (xecutor == DefaultXecutors.FRAME_START) {
			if (isLast) throw new IllegalStateException("Frame result node is missing");
			if (xpressionNode.getFrameOwnerIndex() != nodeIndex) {
				throw new IllegalStateException("Node is not an frame entry, index=" + nodeIndex);
			}
			RunnerFrame innerFrame = new RunnerFrame(nodeIndex, frame, sequencerSupplier.get());
			return new FrameStartNode(contextNode, innerFrame);
		} 

		if (!prevNode.isValidFrameOfNextNode(xpressionNode.getFrameOwnerIndex())) {
			throw new IllegalStateException("Inconsistent frame sequence, currentFrame=" + frame.getStartIndex() +
					", nodeFrame=" + xpressionNode.getFrameOwnerIndex() + ", nodeIndex=" + nodeIndex);
		}
		
		if (xecutor == DefaultXecutors.FRAME_RESULT) {
			RunnerFrame.ResultMarker frameResult = frame.registerFrameResult(nodeIndex);
			return new FrameResultNode(contextNode, frameResult);
		} 
		
		if (isLast) {
			return new XecutorContextRunnerResultNode(contextNode, frame, xecutor);
		} 
		return new XecutorContextRunnerStatefulNode(contextNode, frame, xecutor);
	}

	private abstract class AbstractFrameNode extends XecutorContextRunnerFramedNode 
	{
		public AbstractFrameNode(XecutorContextNode contextNode, RunnerFrame frame) { super(contextNode, frame); }

		@Override public StiXecutor getInitialState()
		{
			return null;
		}

		@Override public void setNextState(StiXecutor xecutor)
		{
			throw new IllegalStateException();
		}

		public boolean isValidFrameOfNextNode(int frameOwnerIndex)
		{
			if (super.isValidFrameOfNextNode(frameOwnerIndex)) return true;
			RunnerFrame outerFrame = getFrame().getOuterFrame();
			return outerFrame != null && outerFrame.getStartIndex() == frameOwnerIndex;
		}
	}
	
	private class FrameResultNode extends AbstractFrameNode
	{
		private final RunnerFrame.ResultMarker frameResult;
		
		public FrameResultNode(XecutorContextNode contextNode, RunnerFrame.ResultMarker frameResult)
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

	private class FrameStartNode extends AbstractFrameNode
	{
		public FrameStartNode(XecutorContextNode contextNode, RunnerFrame frame)
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
		
		@Override public Object evaluatePush(XecutorContextRunnerNode originNode, Parameter<?> targetParam, Object pushedValue)
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
		XecutorContextRunnerNode node = nodes[index];
		if (node.isFinalState()) {
			node.validatePushFinalState();
			return false;
		}

		XecutorContextRunnerNode originNode = nodes[target.getValueIndex()];
		Object pushedValue = originNode.getPushOrFinalValue();
		stats.onPushAttempt(node.getXpressionNode(), target.getXtractorParam(), pushedValue);
		if (pushedValue == null) return false;
		Object resultValue = node.evaluatePush(originNode, target.getXtractorParam(), pushedValue);
		if (resultValue == null) return false;
		stats.onPushEvaluated(node.getXpressionNode(), resultValue);
		return true;
	}

	public void evaluateDirectPush(int originIndex, List<StiXpressionNode.PushTarget> targets, Object pushedValue)
	{
		if (targets.isEmpty() || pushedValue == null) return;
		XecutorContextRunnerNode node = nodes[originIndex];
		node.getSequencer().addImmediateTargets(targets);
		executePostponedTargets(node);		
	}
	
//	private boolean executeImmediateTargets(XecutorContextRunnerNode node)
//	{
//		StiXpressionSequencer sequencer = node.getSequencer();
//		StiXpressionNode.PushTarget pushTarget;
//		while ((pushTarget = sequencer.nextImmediateTarget()) != null) {
//			if (!evaluatePush(pushTarget)) continue;
//			if (node.hasFrameFinalState()) return true;
//		}
//		return false;
//	}
	
	private boolean executePostponedTargets(XecutorContextRunnerNode node)
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
			XecutorContextRunnerNode node = nodes[currentIndex];
			
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

