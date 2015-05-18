package org.cybcode.tools.bixtractor.api;

public interface BiXource extends BiXtractor<BiXource.Result>
{
	enum Result { STOP, CONTINUE; }
	
	BiXourceContext buildContext(BiXourceLink[] receivers);
}