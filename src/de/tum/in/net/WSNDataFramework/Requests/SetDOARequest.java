package de.tum.in.net.WSNDataFramework.Requests;


public class SetDOARequest extends WSNRequest {
	String nodeID;
	int DOA;

	public SetDOARequest() {
		super();
	}

	public SetDOARequest(String nodeID, int degreeOfAggregation) {
		this();
		this.nodeID = nodeID;
		this.DOA = degreeOfAggregation;
	}

	public static class DOASetEvent extends WSNRequestEvent {
		String nodeID;
		int DOA;
	}
}
