package de.tum.in.net.WSNDataFramework.Event;

import java.util.Arrays;
import java.util.HashSet;



/**
 * Extension of EventProvider with public fire() method.
 * May be used for event handling within a specific closure (since fire() is public an EventProviderObject instance may be controlled from the surrounding closure).
 * @author André Freitag
 * @see EventProvider
 */
public class EventProviderObject extends EventProvider {


	/** list of provided events **/
	@SuppressWarnings("unchecked")
	protected Class<? extends Event>[] providedEvents = new Class[]{};


	/**
	 * fire event
	 */
	@Override
	public EventProvider fireEvent(Event eve) {
		return super.fireEvent(eve);
	}


	/**
	 * add a event to the provider
	 * @param event
	 */
	public void addProvidedEvent(Class<? extends Event> event) {
		HashSet<Class<? extends Event>> providedEvents = new HashSet<Class<? extends Event>>(Arrays.asList(this.getProvidedEvents()));
		providedEvents.add(event);

		try {
			@SuppressWarnings("unchecked")
			Class<? extends Event>[] temp = new Class[providedEvents.size()];
			providedEvents.toArray(temp);

			this.providedEvents = this.providedEvents.getClass().cast(temp);
		} catch (Exception e) {
		}
	}

	/**
	 * remove event from the provider
	 * @param event
	 */
	public void removeProvidedEvent(Class<? extends Event> event) {
		HashSet<Class<? extends Event>> providedEvents = new HashSet<Class<? extends Event>>(Arrays.asList(this.getProvidedEvents()));
		providedEvents.remove(event);

		try {
			@SuppressWarnings("unchecked")
			Class<? extends Event>[] temp = new Class[providedEvents.size()];
			providedEvents.toArray(temp);

			this.providedEvents = this.providedEvents.getClass().cast(temp);
		} catch (Exception e) {
		}
	}
}
