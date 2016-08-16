package de.tum.in.net.WSNDataFramework.Event;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * Class representing an occurred event, may be extended.
 * @author André Freitag
 *
 */
public abstract class Event extends Object{


	/**
	 * represents EventProvider this Event origins from.
	 */
	public EventProvider provider=null;

	/**
	 * the time the event was fired.
	 */
	public Date fireTime=null;

	/**
	 * constructor
	 */
	public Event() {
	}
	/**
	 * copy constructor
	 * 
	 * @param eve
	 */
	public Event(Event eve) {

	}


	/**
	 * clones an Event using the copy constructor of the actual class.
	 */
	@Override
	public final Event clone() {
		Object clone=null;

		try {
			Constructor<?> constr = this.getClass().getConstructor(this.getClass());
			clone = constr.newInstance(this.getClass().cast(this));
		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}

		return (Event) clone;
	}
}
