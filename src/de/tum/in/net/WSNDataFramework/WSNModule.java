package de.tum.in.net.WSNDataFramework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.tum.in.net.WSNDataFramework.Event.Event;
import de.tum.in.net.WSNDataFramework.Event.EventProvider;
import de.tum.in.net.WSNDataFramework.Event.ThreadedEventSubscriber;
import de.tum.in.net.WSNDataFramework.Events.WSNModuleAddedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNModuleRemovedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNModuleStatusChangedEvent;


/**
 * abstract parent class for WSN-Modules
 * @author André Freitag
 *
 */
public abstract class WSNModule extends EventProvider {


	/*
	 * to override
	 */
	/** list of provided events **/
	@SuppressWarnings("unchecked")
	protected Class<? extends Event>[] providedEvents = new Class[]{};

	/** handler that is called everytime the module is connected to a new WSN */
	protected void _init (){}
	/** worker method (WSNModule worker thread, remember it must be interruptible! Otherwise the Module won't shut down correctly. */
	protected void _run() throws Exception {}
	/** handler that is called everytime the module gets shutdown */
	protected void _shutdown(){}
	/** handler that is called everytime the module was shutdown properly */
	protected void _postShutdown() {}









	/*
	 * implementation
	 */
	/**
	 * Attaches this module to a specific WSN and runs it runner thread..
	 * This method implicitly calls shutdown() if the module is already attached to a WSN.
	 * Has the same effect as calling wsn.addModule(this).
	 * 
	 * @param wsn WSN to attach to
	 * @return this for fluent interface
	 */
	public final synchronized WSNModule setWSN(WSNApp wsn) {
		if (_wsn == wsn) return this; // abort if we are already attached to the given WSN


		/*** detach current WSN ***/
		if (!_shutdown) {
			this.shutdown();
		}
		_shutdown = false;
		_shuttingDown = false;
		/**************************/



		/*** attach WSN and Module to each other ***/
		// attach WSN
		_wsn = wsn;

		// WSN::addModule does the same as WSNModule::setWSN -> they call each other
		wsn.addModule(this);
		/*******************************************/


		/*** start runner thread ***/
		// call init handler
		this._init();

		// start worker thread
		this._startThreads();
		/***************************/

		return this;
	}
	/**
	 * Gets the currently attached WSN
	 * @return currently attached WSN
	 */
	public WSNApp app() {
		return _wsn;
	}

	/**
	 * Gets the name of this module.
	 */
	public String getName() {
		return this.getClass().getName();
	}
	/**
	 * Gets current Status.
	 */
	public WSNModuleStatus getStatus() {
		return _status;
	}

	/**
	 * Shuts module down. stops runner thread and detaches the module from its WSN.
	 * Has the same effect as calling WSN.removeModule(this).
	 * 
	 * @return this for fluent interface
	 */
	public synchronized WSNModule shutdown() {
		if (_wsn == null) return this; // abort if we are not connected to a WSN

		this._shuttingDown = true; // set "currently shutting down" state

		// call shutdown handler
		_shutdown();


		if (this._workerThread == null) {
			this._cleanups(); // do cleanups
		}


		// stop threads
		this._stopThreads();


		return this;
	}
	/**
	 * checks if WSNModule is shut down
	 * @return true if WSNModule is shut down
	 */
	public boolean isShutdown() {
		return _shutdown;
	}
	/**
	 * checks if WSNModule is currently shutting down
	 * @return true if WSNModule is shutting down
	 */
	public boolean isShuttingDown() {
		return _shuttingDown;
	}
	/**
	 * blocks until module is shut down
	 * 
	 * @throws InterruptedException
	 * @return this for fluent interface
	 */
	public WSNModule waitForShutdown() throws InterruptedException {
		synchronized(_shutdown) {
			try {
				while (!_shutdown) {
					_shutdown.wait();
				}
			} catch (Exception e) {
			}
		}

		return this;
	}


	/**
	 * represents the status of a module
	 * 
	 * @author André Freitag
	 */
	public static class WSNModuleStatus {
		public enum STATUS {
			RUNNING, IDLING, ERROR
		}

		/**
		 * constructor
		 * 
		 * @param status
		 * @param message
		 */
		public WSNModuleStatus(WSNModuleStatus.STATUS status, String message) {
			_status = status;
			_message = message;
		}

		/**
		 * gets WSNModuleStatus.STATUS.
		 */
		public STATUS getStatus() {
			return _status;
		}
		/**
		 * gets attached message.
		 */
		public String getMessage() {
			return _message;
		}

		/* protected member */
		protected STATUS _status=null;
		protected String _message="";
	}


	/**
	 * subscribe to specific event. (listens to WSN and WSNModule events).
	 * Events are handled in an own thread so they won't block the WSN.
	 * 
	 * @param event
	 * @param cbName
	 */
	protected boolean _subscribeTo(Class<? extends Event> event, String cbName) {
		if (this.app() != null) {
			return _eventSubscriber.subscribeTo(event, this.app(), this, cbName);
		}

		return false;
	}

	/**
	 * Execute module dependent code. addCallback will be called when the module is loaded, remCallback when it is removed.
	 * This method may only be called when connected to a WSN => call in _init()!
	 * 
	 * Callbacks are called with an instance of the given module.
	 * (but they may be declared without parameters).
	 * If more than one fitting method is found (e.g. a method taking the actual module as argument as well as a method taking a WSNModule as argument) all methods are called starting with the most specific one.
	 * 
	 * @param module
	 * @param addCb
	 * @return this for fluent interface
	 */
	protected WSNModule _moduleDependent(Class<? extends WSNModule> moduleClass, String addCallback) {
		return _moduleDependent(moduleClass, addCallback, null);
	}

	/**
	 * Execute module dependent code. addCallback will be called when the module is loaded, remCallback when it is removed.
	 * This method may only be called when connected to a WSN => call in _init()!
	 * 
	 * Callbacks are called with an instance of the given module.
	 * (but they may be declared without parameters).
	 * If more than one fitting method is found (e.g. a method taking the actual module as argument as well as a method taking a WSNModule as argument) all methods are called starting with the most specific one.
	 * 
	 * @param module
	 * @param addCb
	 * @param remCb
	 * @return this for fluent interface
	 */
	protected WSNModule _moduleDependent(final Class<? extends WSNModule> moduleClass, final String addCallback, final String remCallback) {
		if (this.app() == null) return this;

		WSNModule m = this.app().module(moduleClass);
		if (m != null) {
			_callModuleDependentCb(m, addCallback);
		} else {
			_eventSubscriber.subscribeTo(WSNModuleAddedEvent.class, this.app(),	new Object() {
				@SuppressWarnings("unused")
				public void eventOccurred(WSNModuleAddedEvent eve) {
					if (eve.module.getClass().equals(moduleClass)) {
						_callModuleDependentCb(eve.module, addCallback);
					}
				}
			}, "eventOccurred");
		}

		if (remCallback != null) {
			_eventSubscriber.subscribeTo(WSNModuleRemovedEvent.class, this.app(), new Object() {
				@SuppressWarnings("unused")
				public void eventOccurred(WSNModuleRemovedEvent eve) {
					if (eve.module.getClass().equals(moduleClass)) {
						_callModuleDependentCb(eve.module, remCallback);
					}
				}
			}, "eventOccurred");
		}

		return this;
	}
	/**
	 * Returns a FileStorage handler for this WSNModule's configuration data.
	 * FileStorage links to a file within the WSN's file directory.
	 * Filename: "moduleconfig.moduleName.moduleClassHash".
	 * 
	 * @return FileStorage for configuration data | NULL if not connected to a WSN
	 */
	protected FileStorage _getConfig() {
		if (this.app() != null) {
			return new FileStorage(this.app().getFile("moduleconfig."+this.getName()+"."+this.getClass().getName().hashCode()));
		} else {
			return null;
		}
	}
	/**
	 * sets this module's status
	 * 
	 * @param status
	 * @return this for fluent interface
	 */
	protected WSNModule _setStatus(WSNModuleStatus.STATUS status) {
		return _setStatus(status,"");
	}
	/**
	 * sets this module's status
	 * 
	 * @param status
	 * @param message
	 * @return this for fluent interface
	 */
	protected WSNModule _setStatus(WSNModuleStatus.STATUS status, String message) {
		// set status
		_status = new WSNModuleStatus(status, message);

		// fire event
		this.fireEvent(new WSNModuleStatusChangedEvent(this, _status));

		return this;
	}
	/**
	 * set this module's status to RUNNING with the given status message.
	 * 
	 * @param message
	 * @return this for fluent interface
	 */
	protected WSNModule _setRunning(String message) {
		return _setStatus(WSNModuleStatus.STATUS.RUNNING, message);
	}
	/**
	 * set this module's status to ERROR with the given status message.
	 * 
	 * @param message
	 * @return this for fluent interface
	 */
	protected WSNModule _setError(String message) {
		return _setStatus(WSNModuleStatus.STATUS.ERROR, message);
	}
	/**
	 * set this module's status to IDLING with the given status message.
	 * 
	 * @param message
	 * @return this for fluent interface
	 */
	protected WSNModule _setIdling(String message) {
		return _setStatus(WSNModuleStatus.STATUS.IDLING, message);
	}



	/* private helper */
	/**
	 * call a module dependent add callback
	 * 
	 * @param module
	 * @param callbackName
	 */
	private final void _callModuleDependentCb(WSNModule module, String callbackName) {
		/* determine all fitting methods */
		HashMap<Integer,Method> unsortedMethods = new HashMap<Integer,Method>();

		Method[] availableMethods = this.getClass().getDeclaredMethods(); // use getDeclaredMethods() instead of Methods() to also get private ones..
		for (Method m: availableMethods) { // iterate through all available methods

			Type[] parameters = m.getGenericParameterTypes();
			if (m.getName() != callbackName
					|| parameters.length != 1) continue; // we are only interested in methods with 1 argument and called >callbackName<



			Type t = parameters[0];

			// compare the type of the event as well as the types of all its superclasses to the parameter of the method
			// if a match is found save it with the depth it was found at (direct match-> depth:0; superclass match-> depth:1;..)
			int i=0;
			for (Class<?> c=module.getClass();
					c != Object.class;
					c=c.getSuperclass()) {

				if (t.equals(c)) { // method fits
					unsortedMethods.put(i, m); // use depth as index as the depth is unique (there is only 1 fitting method with 1 argument being the same type as the event)
				}
				i++;
			}
		}

		// sort methods (more specific ones first)
		Method[] methods = new Method[unsortedMethods.size()];

		List<Integer> sortedKeys = new ArrayList<Integer>();
		sortedKeys.addAll(unsortedMethods.keySet());
		Collections.sort(sortedKeys); // sort: more specific first

		int i=0;
		for (Integer key: sortedKeys) {
			methods[i++] = unsortedMethods.get(key);
		}


		/* call callbacks */
		for (i=0; i<methods.length; i++) {
			try {
				methods[i].setAccessible(true);
				methods[i].invoke(this, module);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}





	/**
	 * start worker+event threads
	 */
	private final void _startThreads() {
		/* worker thread */
		if (_workerThread == null) { // don't start more than one thread
			// create anonymous thread calling final method _workerThread
			_workerThread = new Thread(new Runnable() {

				@Override
				public void run() {
					_workerThread();
				}

			});
			_workerThread.setName("Module Worker " + this.getClass().getName());
			_workerThread.start();
		}
	}
	/**
	 * stop runner thread
	 */
	private final void _stopThreads() {
		if (_workerThread != null) {
			_workerThread.interrupt();
		}
		if (_eventSubscriber != null) {
			_eventSubscriber.shutdown();
		}
	}
	/**
	 * worker thread wrapped around _run
	 * override _run!
	 */
	private final void _workerThread() {
		// run worker method
		try {
			_run();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// finished -> set thread=null
		_workerThread = null;

		// do cleanups
		_cleanups();
	}
	/**
	 * does cleanups
	 */
	protected final void _cleanups() {

		// if already shutdown, not shutting down or one of the threads is still running -> dont cleanup
		if (_wsn==null || !this._shuttingDown || _workerThread!=null) return;



		/*** detach WSN and Module from each other ***/
		// detach WSN
		WSNApp wsn = _wsn;
		_wsn = null;

		synchronized(_shutdown) {
			_shutdown.notifyAll();
			_shutdown=true;
		}

		// WSN::removeModule does the same as WSNModule::shutdown -> they call each other
		wsn.removeModule(this);
		/*********************************************/


		// call post shutdown handler
		_postShutdown();
	}

	/** points to currently attached wsn, null if none attached */
	private WSNApp _wsn=null;
	/** represnting current status */
	private WSNModuleStatus _status=new WSNModuleStatus(WSNModuleStatus.STATUS.IDLING,"");
	/** holding current running worker thread **/
	private Thread _workerThread=null;
	/** true if WSNModule is in shut down state **/
	private Boolean _shutdown=false;
	/** true if WSNModule is currently shutting down */
	private boolean _shuttingDown = false;

	/** event subscriber (handling occured events in a seperate thread) */
	protected ThreadedEventSubscriber _eventSubscriber=new ThreadedEventSubscriber("Module Events " + this.getClass().getName());
}
