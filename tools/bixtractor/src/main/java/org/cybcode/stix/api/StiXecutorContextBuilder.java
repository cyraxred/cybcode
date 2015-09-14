package org.cybcode.stix.api;


public interface StiXecutorContextBuilder
{
	interface NodeDetails {
		int getXtractorIndex();
		StiXtractor<?> getXtractor();
		int getParamCount();
		int getParamXtractorIndex(int index);
		
		int getTargetCount();
		NodeDetails getTargetXtractor(int index);
		StiXtractor.Parameter<?> getTargetParameter(int index);
		boolean isNotifyOnRegularTarget(int index);
	}
	
	void setCapacities(int nodesCount, int paramsCount, int linksCount);
	void addNode(NodeDetails node);
	void getNodeTargets();
	
	StiXecutorContext build();
}