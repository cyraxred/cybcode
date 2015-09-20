package org.cybcode.stix.core.compiler;

import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.api.StiXtractor.Parameter;

public class SlotLink
{
	public final RegularParserSlot target;
	public final StiXtractor.Parameter<?> parameter;

	public SlotLink(RegularParserSlot target, Parameter<?> parameter)
	{
		this.target = target;
		this.parameter = parameter;
	}
	
	@Override public String toString()
	{
		if (target == null) return "RESULT";
		return parameter.toNameString() + "=" + target.toNameString();
	}
}