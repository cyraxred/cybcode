package org.cybcode.stix.api;

import java.util.List;

public interface StiXecutorConstructionContext
{
	List<StiXecutorCallback> getXecutorCallbacks();
	StiXecutorCallback createCallbackGroup(List<StiXecutorCallback> callbacks);
	boolean hasPushTargets();
	boolean hasSortedFields();
	StiXecutor createFrameStartXecutor();
	StiXecutor createFrameResultXecutor();
	StiXecutorStatsCollector getStatsCollector();
}