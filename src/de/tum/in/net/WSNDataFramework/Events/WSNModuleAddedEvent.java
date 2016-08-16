package de.tum.in.net.WSNDataFramework.Events;

import de.tum.in.net.WSNDataFramework.WSNModule;



/**
 * Event that gets fired each time a module is added to the WSN
 * @author André Freitag
 *
 */
public class WSNModuleAddedEvent extends WSNEvent {

	public WSNModule module;

	public WSNModuleAddedEvent(WSNModule module) {
		this.module = module;
	}

}
