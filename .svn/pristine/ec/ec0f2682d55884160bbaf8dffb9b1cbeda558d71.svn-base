package de.tum.in.net.WSNDataFramework.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadedEventSubscriber {

	public ThreadedEventSubscriber() {
		this("ThreadedEventSubscriber Worker Thread");
	}
	public ThreadedEventSubscriber(String threadName) {
		/* events thread */
		if (_thread == null) { // don't start more than one thread
			// create anonymous thread calling final method _eventsThread()
			_thread = new Thread(new Runnable() {

				@Override
				public void run() {
					_thread();
				}

			});
			_thread.setName(threadName);
			_thread.start();
		}
	}

	/**
	 * Subscribes to a specific event on a specific event provider.
	 * Calls the method called `callbackName` with the fired event instance as argument on Object whenever the specific event was fired.
	 * All methods named `callbackName` that fit the parameter are called, the most specific ones first.
	 * This allows to execute tasks for the general Event Type as well as for the specific Event.
	 * 
	 * @param eventClass
	 * @param provider
	 * @param listener
	 * @param callbackName
	 * @return
	 */
	public boolean subscribeTo(Class<? extends Event> eventClass, EventProvider provider, Object listener, String callbackName) {
		if (provider==null) return false;

		boolean success = provider.subscribeEvent(eventClass, _eventBuffer);
		if (success) {
			synchronized (_listener) {

				// determine all fitting methods
				HashMap<Integer,Method> unsortedMethods = new HashMap<Integer,Method>();

				Method[] availableMethods = listener.getClass().getDeclaredMethods(); // use getDeclaredMethods() instead of Methods() to also get private ones..
				for (Method m: availableMethods) { // iterate through all available methods

					Type[] parameters = m.getGenericParameterTypes();
					if (m.getName() != callbackName
							|| parameters.length != 1) continue; // we are only interested in methods with 1 argument and called >callbackName<



					Type t = parameters[0];

					// compare the type of the event as well as the types of all its superclasses to the parameter of the method
					// if a match is found save it with the depth it was found at (direct match-> depth:0; superclass match-> depth:1;..)
					int i=0;
					for (Class<?> c=eventClass;
							c != Object.class;
							c=c.getSuperclass()) {

						if (t.equals(c)) { // method fits
							unsortedMethods.put(i, m); // use depth as index as the depth is unique (there is only 1 fitting method with 1 argument being the same type as the event)
						}
						i++;
					}
				}


				// add listener
				if (unsortedMethods.size() > 0) {

					// sort methods (more specific ones first)
					Method[] methods = new Method[unsortedMethods.size()];

					List<Integer> sortedKeys = new ArrayList<Integer>();
					sortedKeys.addAll(unsortedMethods.keySet());
					Collections.sort(sortedKeys); // sort: more specific first

					int i=0;
					for (Integer key: sortedKeys) {
						methods[i++] = unsortedMethods.get(key);
					}





					EventProviderPair epp = new EventProviderPair(eventClass, provider);

					// create new entry for specific event if none exist
					if (_listener.get(epp) == null) {
						_listener.put(epp, new HashMap<Object,Method[]>());
					}

					// add listener
					_listener.get(epp).put(listener,  methods);
				}
			}
		}

		return success;
	}

	/**
	 * unsubscribes a previously subscribed event.
	 * 
	 * @param eventClass
	 * @param provider
	 * @return
	 */
	public ThreadedEventSubscriber unsubscribe(Class<? extends Event> eventClass, EventProvider provider) {
		synchronized (_listener) {
			EventProviderPair epp = new EventProviderPair(eventClass, provider);

			if (_listener.containsKey(epp)) {
				if (epp.provider != null) {
					epp.provider.unsubscribeEvent(epp.eventClass, _eventBuffer);
				}

				_listener.remove(epp);
			}
		}

		return this;
	}

	/**
	 * shuts down ThreadedEventSubscriber.
	 * Terminates its thread and unsubscribes all events.
	 * 
	 * @return this for fluent interface
	 */
	public ThreadedEventSubscriber shutdown() {
		if (this.isShutdown()) return this;

		_shutdown = true;

		if (_thread != null) {
			_thread.interrupt();
		}

		synchronized (_listener) {
			for (EventProviderPair epp: _listener.keySet()) {
				if (epp.provider != null) {
					epp.provider.unsubscribeEvent(epp.eventClass, _eventBuffer);
				}
			}

			_listener = new HashMap<EventProviderPair, HashMap<Object,Method[]>>();
		}


		return this;
	}

	/**
	 * determines whether this ThreadEventSubscriber was shut down or not.
	 * 
	 * @return true if shut down | false otherwise
	 */
	public boolean isShutdown() {
		return _shutdown;
	}




	/**
	 * thread for handling events
	 */
	protected final void _thread() {

		// run
		try {
			Event eve=null;
			while (true) {

				// retrieve event (blocks until event available)
				eve = _eventBuffer.take();

				// call event handler for all registered listeners
				// if listener subscribed to an parent of this event it also will be notified
				for (Class<?> c=eve.getClass();
						c != Object.class;
						c=c.getSuperclass()) { // iterate through inheritance chain (starting with actual class)

					@SuppressWarnings("unchecked")
					HashMap<Object,Method[]> listener = _listener.get(new EventProviderPair((Class<? extends Event>)c,eve.provider));

					if (listener == null) continue;	// only handle listeners registered for the event beeing fired

					for (Map.Entry<Object,Method[]> entry: listener.entrySet()) {

						Object l  = entry.getKey();
						Method[] cbs = entry.getValue();

						for (Method cb: cbs) {
							try {
								cb.setAccessible(true);
								cb.invoke(l, eve);
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						}
					}
				}

			}
		} catch (InterruptedException e) {
		}


		// finished -> set thread=null
		_thread = null;
	}




	/** generic eventBuffer for this ThreadedEventSubscriber */
	protected EventBuffer _eventBuffer=new EventBuffer();
	/** HashMap containing EventClass->(Listener -> callbackName) associations **/
	protected HashMap<EventProviderPair, HashMap<Object,Method[]>> _listener=new HashMap<EventProviderPair, HashMap<Object,Method[]>>();
	/** holding current running event thread **/
	protected Thread _thread=null;
	/** determining whether this was shut down or not */
	protected boolean _shutdown=false;







	/**
	 * Pair of Class<? extends Event>, EventProvider.
	 * Overriding equals() and hashCode() for easy in use hash based collections.
	 * 
	 * @author André Freitag
	 */
	protected static class EventProviderPair {
		public Class<? extends Event> eventClass;
		public EventProvider provider;

		public EventProviderPair(Class<? extends Event> eventClass, EventProvider provider) {
			this.eventClass = eventClass;
			this.provider = provider;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EventProviderPair) {
				EventProviderPair epp = (EventProviderPair) obj;

				boolean equals=true;
				equals = equals && epp.eventClass!=null ? epp.eventClass.equals(this.eventClass) : epp.eventClass==this.eventClass;
				equals = equals && epp.provider!=null ? epp.provider.equals(this.provider) : epp.provider==this.provider;

				return equals;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return new String(
					this.eventClass!=null ? this.eventClass.getCanonicalName() : ""
							+ this.provider!=null ? this.provider.getClass().getCanonicalName() : ""
					).hashCode();
		}
	}
}
