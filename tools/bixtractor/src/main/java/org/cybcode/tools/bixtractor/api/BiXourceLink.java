package org.cybcode.tools.bixtractor.api;

public interface BiXourceLink
{
	BiXtractor<?> getReceiver();
	boolean pushAndEvaluate(XecutionContext context, Object value);
}