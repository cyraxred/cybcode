package org.cybcode.stix.api;

import java.util.List;

public interface StiXecutorConstructionContext
{
	List<StiXecutorCallback> getXecutorCallbacks();
	boolean hasPushTargets();
	boolean hasSortedFields();
	StiXecutor createFrameXecutor();
}