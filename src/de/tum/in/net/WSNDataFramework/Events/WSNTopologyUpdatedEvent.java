package de.tum.in.net.WSNDataFramework.Events;

import de.tum.in.net.WSNDataFramework.Topology;

/**
 * Event that gets fired each time the WSN's topology was updated.
 * 
 * @author André Freitag
 */
public class WSNTopologyUpdatedEvent extends WSNEvent {

	/* constructors */
	/**
	 * constructor
	 * 
	 * @param topology
	 */
	public WSNTopologyUpdatedEvent(Topology topology) {
		_topology = topology;
	}



	/* public methods */
	public Topology topology() {
		return _topology;
	}



	/* protected member */
	protected Topology _topology=null;
}
