package de.tum.in.net.WSNDataFramework.Requests;

import de.tum.in.net.WSNDataFramework.Topology;

public class RenewTopologyRequest extends WSNRequest {

	public RenewTopologyRequest() {
		super();
	}

	public static class WSNTopologyRenewedEvent extends WSNRequestEvent {
		public Topology topology;
	}
}
