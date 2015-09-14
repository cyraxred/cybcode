package org.cybcode.stix.api;

import java.util.List;

public interface StiXecutorConstructionContext
{
	List<StiXourceNestedXecutor<?>> getNestedXources();
	StiXecutor createFrameXecutor();
}