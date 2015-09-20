package org.cybcode.stix.core.xecutors;


public class StiXecutorDefaultContextBuilder extends XecutorContextBuilder<StiXecutorDefaultContext>
{
	@Override protected StiXecutorDefaultContext build(StiXpressionNode[] nodes)
	{
		return new StiXecutorDefaultContext(nodes);
	}
}
