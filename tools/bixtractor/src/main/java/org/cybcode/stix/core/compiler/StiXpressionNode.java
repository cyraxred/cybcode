package org.cybcode.stix.core.compiler;

import org.cybcode.stix.api.StiXtractor;

public class StiXpressionNode
{
	private final StiXtractor<?> op;
	private final int distanceFromResult;
//	private final double complexity

	public StiXpressionNode(StiXtractor<?> op, StiXpressionNode nextToResult)
	{
		this.op = op;
		this.distanceFromResult = nextToResult.distanceFromResult + 1;
	}

}
