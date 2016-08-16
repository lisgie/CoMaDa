package de.tum.in.net.WSNDataFramework.Event;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * basic EventProvider.
 * Offers an interface for firing and subscribing to events.
 * @author André Freitag
 *
 */
public abstract class EventProvider {


	/*
	 * to ovewrite
	 */
	/**
	 * list of provided events.
	 * since member can't be overwritten in subclasses use getProvidedEvents() to retrieve actual list.
	 **/
	@SuppressWarnings("unchecked")
	protected Class<? extends Event>[] providedEvents = new Class[]{};





	/*
	 * implementation
	 */
	/**
	 * Subscribes to a specific event.
	 * Adds an instance of the fired event to eventBuffer each time it is fired.
	 * 
	 * @param eventClass
	 * @param eventBuffer
	 */
	public boolean subscribeEvent(Class<? extends Event> eventClass, EventBuffer eventBuffer) {
		if (!_eventRegistrationAllowed(eventClass))  // do not subscribe if requested event is not in our list of provided events
			return false;

		synchronized (_listener) {
			if (!_listener.containsKey(eventClass)) {
				_listener.put(eventClass, new HashSet<EventBuffer>());
			}

			_listener.get(eventClass).add(eventBuffer);
		}

		return true;
	}
	/**
	 * Unsubscribes from a previously subscribed event.
	 * 
	 * @param eventClass
	 * @param eventBuffer
	 */
	public EventProvider unsubscribeEvent(Class<? extends Event> eventClass, EventBuffer eventBuffer) {
		synchronized (_listener) {
			if (_listener.containsKey(eventClass)) {
				if (_listener.get(eventClass).contains(eventBuffer)) {
					_listener.get(eventClass).remove(eventBuffer);
				}
			}
		}

		return this;
	}

	/**
	 * get list of events offered by this provider.
	 * Uses reflection to determine events actually provided by this class.
	 */
	public Class<? extends Event>[] getProvidedEvents() {

		@SuppressWarnings("unchecked")
		Class<? extends Event>[] providedEvents = new Class[0];

		for (Class<?> c=this.getClass();
				c != Object.class;
				c=c.getSuperclass()) { // iterate through inheritance chain (starting with actual class)

			try {
				Field f = c.getDeclaredField("providedEvents");
				f.setAccessible(true);
				Object v = f.get(this);

				providedEvents = this.providedEvents.getClass().cast(v);
				break;
			} catch (NoSuchFieldException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}

		return providedEvents;
	}

	/**
	 * checks if a specific event is provided.
	 * (considers inheritance, if provider provides (TestEvent extends Event) a check for Event would also return true)
	 * @param event
	 */
	public boolean providesEvent(Class<? extends Event> event) {
		for (Class<? extends Event> pEvent: this.getProvidedEvents()) {

			for (Class<?> c=pEvent;
					c != Object.class;
					c=c.getSuperclass()) { // iterate through inheritance chain (starting with actual class)

				if (event == c)
					return true;

			}
		}

		return false;
	}

	/**
	 * fire specific event.
	 * Adds the fired event to all EventBuffers registered for it or any of its parent classes.
	 * 
	 * @param eve Event to fire
	 * @return this for fluent interface
	 */
	protected EventProvider fireEvent(Event eve) {
		synchronized (_listener) {
			if (_listener.containsKey(eve.getClass())) {

				// set origin
				eve.provider = this;
				// set fire time
				eve.fireTime = new Date();


				// call event handler for all registered listeners
				// if listener subscribed to an parent of this event it will also be notified
				for (Class<?> c=eve.getClass();
						c != Object.class;
						c=c.getSuperclass()) { // iterate through inheritance chain (starting with actual class)

					Set<EventBuffer> listener = _listener.get(c);
					if (listener == null) continue;	// only handle listeners registered for the event beeing fired

					for (EventBuffer buf: listener) {
						buf.add(eve);
					}
				}
			}
		}

		return this;
	}

	/**
	 * checks if this EventProvider shall allow a registration for a specific event.
	 * Just maps to EventProvider.providesEvent() but may be overridden by subclass.
	 * 
	 * @param eventClass
	 */
	protected boolean _eventRegistrationAllowed(Class<? extends Event> eventClass) {
		return this.providesEvent(eventClass);
	}


	/** HashMap containing EventClass->(Listener -> callbackName) associations **/
	protected HashMap<Class<? extends Event>, Set<EventBuffer>> _listener=new HashMap<Class<? extends Event>, Set<EventBuffer>>();
}
