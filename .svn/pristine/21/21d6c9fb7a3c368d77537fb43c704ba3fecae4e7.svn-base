package de.tum.in.net.WSNDataFramework.Events;

import de.tum.in.net.WSNDataFramework.Node.NodeID;

/**
 * Event that gets fired each time a Node was removed from the WSN.
 * 
 * @author André Freitag
 */
public class WSNNodeRemovedEvent extends WSNEvent {

	/* constructors */
	/**
	 * constructor.
	 * 
	 * @param nodeID ID of the removed node
	 */
	public WSNNodeRemovedEvent(NodeID nodeID) {
		_nodeID = nodeID;
	}



	/* public methods */
	/**
	 * @return {@link NodeID} of the removed node
	 */
	public NodeID getNodeID() {
		return _nodeID;
	}



	/* protected member */
	/**
	 * a copy of the node that was removed.
	 */
	protected NodeID _nodeID;
}
