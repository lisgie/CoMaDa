package de.tum.in.net.WSNDataFramework.Events;

import de.tum.in.net.WSNDataFramework.Node;

/**
 * Event that gets fired each time a new node is added to the WSN.<br/>
 * It is an extension of the WSNNodeUpdatedEvent.
 * 
 * @see WSNNodeUpdatedEvent
 * @author André Freitag
 */
public class WSNNodeAddedEvent extends WSNNodeUpdatedEvent {

	/**
	 * constructor.
	 * @param node
	 */
	public WSNNodeAddedEvent(Node node) {
		super(node);
	}

}
