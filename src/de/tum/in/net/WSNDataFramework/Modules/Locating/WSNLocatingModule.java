package de.tum.in.net.WSNDataFramework.Modules.Locating;

import java.util.HashMap;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.WSNModule;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.WSNHTTPServerModule;


public class WSNLocatingModule extends WSNModule {


	/**
	 * returns the saved location assignments of the WSN's nodes.
	 * 
	 * @return nodeID=>Location
	 */
	public Map<String,Location> getLocations() {
		return _locations;
	}

	/**
	 * gets the saved location assignment of a specific node.
	 * 
	 * @param nodeID
	 */
	public Location getLocation(String nodeID) {
		return _locations.get(nodeID);
	}

	/**
	 * assigns a node a specific location
	 * 
	 * @param nodeID
	 * @param left
	 * @param top
	 */
	public void setLocation(String nodeID, double x, double y) {
		_locations.put(nodeID, new Location(x,y));
	}


	/** overriden **/
	@Override
	protected void _init() {
		_moduleDependent(WSNHTTPServerModule.class, "_moduleDep", "_moduleRem");

		_setRunning("up and running");
	}

	protected void _moduleDep(WSNHTTPServerModule m) {

		try {
			m.registerController("locating", HTTPController.class, this)
			.getServerModule().registerLink(new String[]{"Visualisation", "Location"}, "/locating");
		} catch (InstantiationException e) {
		}

	}



	/** protected member **/
	protected Map<String,Location> _locations=new HashMap<String,Location>();


	public static class Location {
		public Double x;
		public Double y;

		public Location(Double x, Double y) {
			this.x = x;
			this.y = y;
		}
	}
}
