package de.tum.in.net.WSNDataFramework.CUSTOM;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.tum.in.net.WSNDataFramework.AggregationNode;
import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.Crypto.ControlHandler;
import de.tum.in.net.WSNDataFramework.NodeAction.NodeAction;

public class TUMI8AggregationNode extends AggregationNode {

	private static final long serialVersionUID = -6902751605621331081L;

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends NodeAction>[] getOfferedActions() {
		return new Class[]{SetAggregationDegreeAction.class};
	}
	@Override
	public void performAction(NodeAction action) throws ActionNotImplementedException {
		if (action instanceof SetAggregationDegreeAction) {
			// DUMMY; TODO: implement aggregation degree setting
			InetAddress nodeAddress;
			try {
				nodeAddress = InetAddress.getByName("fec0:0:0:0:0:0:0:7");

				ControlHandler.setDoa(((SetAggregationDegreeAction) action).targetDegree, nodeAddress);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (action.callback != null)
				action.callback.finished(action, true);
		} else {
			super.performAction(action);
		}
	}

	/**
	 * constructor.
	 * 
	 * @param nodeID
	 */
//	public TUMI8AggregationNode(String nodeID) {
//		super(nodeID);
//	}

	/**
	 * copy constructor.
	 * creates a deep copy (also copies its Fields).
	 * Field.value is not cloned, so be careful using mutable types as Field.values.
	 * 
	 * @param node
	 * @throws CloneNotSupportedException
	 */
	public TUMI8AggregationNode(Node node) throws CloneNotSupportedException {
		super(node);
	}

	/**
	 * copy constructor.
	 * creates a deep copy (also copies its Fields + aggregatedNodes).
	 * Field.value is not cloned, so be careful using mutable types as Field.values.
	 * 
	 * @param node
	 * @throws CloneNotSupportedException if node couldn't be cloned correctly
	 */
	public TUMI8AggregationNode(AggregationNode node) throws CloneNotSupportedException {
		super(node);
		for (Node aggregatedNode: node.aggregatedNodes) {
			this.aggregatedNodes.add(aggregatedNode);
		}
	}

}
