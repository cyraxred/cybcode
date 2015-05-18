package org.cybcode.tools.bixtractor.core;

import java.util.List;

interface BiXpressionOptimizer
{
	List<OpNode> optimize(List<OpNode> opNet);
}
