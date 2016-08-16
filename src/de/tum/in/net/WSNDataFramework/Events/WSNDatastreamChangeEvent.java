package de.tum.in.net.WSNDataFramework.Events;

import java.util.Set;

public class WSNDatastreamChangeEvent extends WSNEvent {

	protected Set<String> _feeds;

	public WSNDatastreamChangeEvent(Set<String> _activeFeeds) {
		this._feeds = _activeFeeds;
	}
	
	public Set<String> activeFeeds() {
		return _feeds;
	}
}
