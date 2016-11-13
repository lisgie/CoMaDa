package de.tum.in.net.WSNDataFramework.Requests;

import java.util.Date;

import de.tum.in.net.WSNDataFramework.WSNModule;

public abstract class WSNRequest {
	public int ID;
	public Date requestTime;
	public WSNModule requester;
	public WSNRequestEvent answerEvent;

	public WSNRequest() {
		this.ID = _counter++;
	}

	protected static int _counter=0;


	public static abstract class Event{};
}
