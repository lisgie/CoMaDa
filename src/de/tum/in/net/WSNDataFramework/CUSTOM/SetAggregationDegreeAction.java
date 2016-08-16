package de.tum.in.net.WSNDataFramework.CUSTOM;

import de.tum.in.net.WSNDataFramework.NodeAction.NodeAction;
import de.tum.in.net.WSNDataFramework.NodeAction.NodeActionParam;
import de.tum.in.net.WSNDataFramework.NodeAction.NodeActionResult;

public class SetAggregationDegreeAction extends NodeAction {
	@NodeActionParam
	public int targetDegree;

	@NodeActionResult
	public int newDegree;
}
