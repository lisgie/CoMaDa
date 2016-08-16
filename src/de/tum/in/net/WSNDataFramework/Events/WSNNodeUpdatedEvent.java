package de.tum.in.net.WSNDataFramework.Events;

import de.tum.in.net.WSNDataFramework.Node;

/**
 * Event that gets fired each time a Node was updated.
 * 
 * @author André Freitag
 */
public class WSNNodeUpdatedEvent extends WSNEvent {


	/* constructors */
	/**
	 * constructor.
	 * @param node
	 */
	public WSNNodeUpdatedEvent(Node node) {
		_node = node;
	}



	/* public methods */
	/**
	 * Returns the updated {@link Node}
	 * 
	 * @return {@link Node} || NULL
	 */
	public Node node() {
		return _node;
	}



	/* protected member */
	protected Node _node=null;
}
