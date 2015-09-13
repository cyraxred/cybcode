package org.cybcode.stix.core.xecutors;

import java.util.List;

import org.cybcode.stix.api.StiXourceNestedXecutor;
import org.cybcode.stix.api.StiXpressionContext;

public class StiXpressionDefaultContext implements StiXpressionContext
{
	public StiXpressionDefaultContext()
	{
		// TODO Auto-generated constructor stub
	}

	@Override public List<StiXourceNestedXecutor<?>> getNestedXources()
	{
		throw new UnsupportedOperationException();
	}
}