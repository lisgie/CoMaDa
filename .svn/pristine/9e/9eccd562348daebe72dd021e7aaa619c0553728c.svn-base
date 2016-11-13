package de.tum.in.net.WSNDataFramework.Event;

import java.util.concurrent.LinkedBlockingQueue;


/**
 * EventBuffer. Thread-Safe queue for buffering events. Accessible as LinkedBlockingQueue.
 * EventBuffer.take() returns the oldest Event and blocks if no Events are available.
 * 
 * @author André Freitag
 */
public class EventBuffer extends LinkedBlockingQueue<Event> {

	//
	//	/**
	//	 * Subscribes for event at specific provider.
	//	 * If the provider fires that event it is added to the tail of the queue.
	//	 * @param event
	//	 * @param provider
	//	 * @return
	//	 */
	//	public boolean subscribeTo(Class<? extends Event> event, EventProvider provider) {
	//
	//
	//		return provider.subscribeToEvent(event, new Object() {
	//
	//			@SuppressWarnings("unused")
	//			public void eventOccurred(Event eve) {
	//
	//				_eventFired(eve);
	//
	//			}
	//
	//		}, "eventOccurred");
	//
	//
	//	}
	//
	//
	//
	//	/**
	//	 * intern event handler
	//	 * @param eve
	//	 */
	//	protected final void _eventFired(Event eve) {
	//		this.add(eve);
	//	}



	/**
	 * serialization id
	 */
	private static final long serialVersionUID = 9089745796583087305L;
}
