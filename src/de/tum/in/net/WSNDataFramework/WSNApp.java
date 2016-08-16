package de.tum.in.net.WSNDataFramework;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.tum.in.net.WSNDataFramework.Event.Event;
import de.tum.in.net.WSNDataFramework.Event.EventBuffer;
import de.tum.in.net.WSNDataFramework.Event.EventProvider;
import de.tum.in.net.WSNDataFramework.Event.ThreadedEventSubscriber;
import de.tum.in.net.WSNDataFramework.Events.WSNDatastreamChangeEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNModuleAddedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNModuleRemovedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNNodeUpdatedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNProtocolPacketProcessedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNProtocolRawPacketReceivedEvent;
import de.tum.in.net.WSNDataFramework.MetaData.WSNMetaData;
import de.tum.in.net.WSNDataFramework.Requests.WSNRequest;


/**
 * WSN Manager
 * @author André Freitag
 *
 */
public class WSNApp extends EventProvider {

	/** exported events **/
	@SuppressWarnings("unchecked")
	protected Class<? extends Event>[] providedEvents = new Class[]{WSNDatastreamChangeEvent.class, WSNModuleAddedEvent.class, WSNModuleRemovedEvent.class, WSNNodeUpdatedEvent.class};


	/* constructors */
	/**
	 * uses the "`current working directory`/wsnconfig" as fileStorageDirectory. "`current working directory`/wsnconfig/tmp" as temporaryFilesDirectory.
	 * uses "conf.xml" as configFile.
	 * 
	 * @throws WSNException
	 * @see WSNApp#WSN(String fileStorageDirectory, String temporaryFilesDirectory)
	 */
	public WSNApp() throws WSNException {
		this("wsnconfig", "wsnconfig/tmp");
	}
	/**
	 * Creates a WSN instance using the given directories as file storage.
	 * uses "conf.xml" as configFile.
	 * 
	 * @param fileStorageDirectory directory where to store files created by this WSN and its modules.
	 * @param temporaryFilesDirectory directory where to store temporary files created by this WSN and its modules.
	 * @throws WSNException
	 */
	public WSNApp(String fileStorageDirectory, String temporaryFilesDirectory) throws WSNException {
		this(fileStorageDirectory, temporaryFilesDirectory, "conf/conf.xml");
	}
	/**
	 * Creates a WSN instance using the given directories as file storage and a path to a config file to use.
	 * 
	 * @param fileStorageDirectory directory where to store files created by this WSN and its modules.
	 * @param temporaryFilesDirectory directory where to store temporary files created by this WSN and its modules.
	 * @param configFile path to the configuration file to be used
	 * @throws WSNException
	 */
	public WSNApp(String fileStorageDirectory, String temporaryFilesDirectory, String configFile) throws WSNException {
		this._dynamicProvidedEvents = this.providedEvents;

		// set file storage path
		this.setFilesDirectory(fileStorageDirectory);
		this.setTemporaryFilesDirectory(temporaryFilesDirectory);

		// load configuration
		if (configFile != null) {
			_loadConfiguration(new File(configFile));
		}
	}

	public WSNApp setDriver(WSNDriver driver) {
		if (_driver != null) {
			this.removeModule(_driver);
		}
		this.addModule(driver);
		_driver = driver;

		return this;
	}
	
	public WSNDriver getDriver() {
		return _driver;
	}

	public WSNApp setProtocolStack(WSNProtocol ... protocols) {
		for (WSNProtocol p: protocols) {
			p.setApp(this);
		}
		_protocols = protocols;
		return this;
	}
	public WSNProtocol[] getProtocolStack() {
		return _protocols;
	}


	/* public getters */
	public WSN wsn() {
		return _wsn;
	}

	/**
	 * Gets the Identity of this WSN.
	 * May be used to compare two WSN identities to recognize a WSN and load WSN specific configurations etc..
	 * Can perfectly be serialized!
	 * 
	 * @return WSN.Identity
	 */
	public Identity getIdentity() {
		return new Identity(this);
	}
	/**
	 * Returns a specific loaded module.
	 * 
	 * @param moduleClass
	 * @return module instance | NULL if not found
	 */
	public <T extends WSNModule> T module(Class<T> moduleClass) {

		// 1. look for an direct instance of the given class
		if (_modules.containsKey(moduleClass)) {
			try {
				return moduleClass.cast(_modules.get(moduleClass));
			} catch (Exception e) {
				return null;
			}
		} else { // 2. if direct instance doesn't exist look for a "child" class

			for (Class<? extends WSNModule> c: _modules.keySet()) {
				// TODO: attention: picks the first fitting class -> is there a better way?
				if (moduleClass.isAssignableFrom(c)) {
					return moduleClass.cast(_modules.get(c));
				}
			}

			return null;
		}
	}
	/**
	 * Returns a list of all loaded modules.
	 */
	public WSNModule[] modules() {
		WSNModule[] modules = new WSNModule[_modules.size()];

		int i=0;
		for (WSNModule m: _modules.values()) {
			modules[i++] = m;
		}

		return modules;
	}

	/**
	 * Propagates an Event to all subscribed parties.
	 */
	@Override
	public EventProvider fireEvent(Event eve) {
		return super.fireEvent(eve);
	}

	/**
	 * returns meta data of a specific kind
	 * 
	 * @param informationType
	 * @return
	 */
	public <T extends WSNMetaData> T getMetaData(Class<T> informationType) {
		for (WSNMetaData data: _metaData.values()) {
			if (data.getClass().equals(informationType)) {
				return informationType.cast(data);
			}
		}

		return null;
	}

	/**
	 * @return all available Unit-Translations
	 */
	public Set<MeasurementUnitTranslation> getUnitTranslations() {
		Set<MeasurementUnitTranslation> translations = new HashSet<MeasurementUnitTranslation>();

		for (String unitType: _unitTranslations.keySet()) {
			for (String originalUnit: _unitTranslations.get(unitType).keySet()) {
				translations.addAll(_unitTranslations.get(unitType).get(originalUnit));
			}
		}

		return translations;
	}
	/**
	 * @return all currently active Unit-Translations
	 */
	public Set<MeasurementUnitTranslation> getUnitTranslationsApplied() {
		return new HashSet<MeasurementUnitTranslation>(_activeUnitTranslations);
	}

	/**
	 * returns events provided by this WSN as well as by all registered modules
	 */
	@Override
	public Class<? extends Event>[] getProvidedEvents() {
		return this._dynamicProvidedEvents;
	}

	/**
	 * Gets the path to the directory where files by this WSN and its modules are stored.
	 */
	public String getFilesDirectory() {
		return _filesPath;
	}
	/**
	 * Gets path to the directory for temporary files.
	 */
	public String getTemporaryFilesDirectory() {

		return _tempFilesPath;
	}
	/**
	 * Gets a handler to the file called `filename` within this.getFilesDirectory().
	 * File gets created if it does not exist.
	 * 
	 * @param filename
	 * @return file handle | NULL
	 */
	public File getFile(String filename) {
		File d = new File(_filesPath);
		if (!d.exists()) {
			d.mkdirs();
		}
		if (!d.isDirectory() || !d.canWrite()) return null;

		File f = new File(_filesPath + filename);
		if (f.getAbsolutePath().startsWith(new File(_filesPath).getAbsolutePath())) { // ensure to only return files within actual _filesPath!
			try {
				if (!f.exists()) {
					if (!f.createNewFile()) {
						return null;
					}
				}
				if (f.exists() && f.canRead())
					return f;
			} catch (IOException e) {
			}
		}

		return null;
	}
	/**
	 * Tries to create a unique temporary file (stored in getTemporaryFilesDirectory()).
	 * be sure to delete the file after it isn't needed anymore.
	 * 
	 * @return file handle
	 */
	public File getTemporaryFile() {
		File d = new File(_tempFilesPath);
		if (!d.exists()) {
			d.mkdirs();
		}
		if (d.isDirectory() && d.canWrite()) {
			for (int i=0; i<256; i++) {
				String tempFileName = UUID.randomUUID().toString();

				File f = new File(_tempFilesPath + tempFileName);
				try {
					if (f.createNewFile()) {
						return f;
					}
				} catch (IOException e) {
				}
			}
		}

		return null;
	}

	/**
	 * checks if WSN is shut down
	 * @return true if WSN is shut down
	 */
	public boolean isShutdown() {
		return _shutdown;
	}
	/**
	 * checks if WSN is currently shutting down
	 * @return true if WSN is shutting down
	 */
	public boolean isShuttingDown() {
		return _shuttingDown;
	}


	/* public methods */
	/**
	 * adds a metadata object.
	 * replaces object of the same class (including super classes except WSNMetaData itself)
	 * 
	 * @param data
	 * @return
	 */
	public <T extends WSNMetaData> WSNApp setMetaData(T data) {
		for (Class<?> c=data.getClass();
				c != WSNMetaData.class;
				c = c.getSuperclass()) {

			if (_metaData.containsKey(c)) {
				_metaData.remove(c);
			}
		}

		_metaData.put(data.getClass(), data);

		return this;
	}

	public WSNRequest request(WSNRequest request, boolean synchronous) {
		return request;
	}
	public WSNRequest request(WSNRequest request) {
		return request;
	}

	/**
	 * Adds a new module.
	 * Has the same effect as calling m.setWSN(this).
	 * 
	 * @param m module
	 * @uses WSNModule::setWSN
	 * @return this for fluent interface
	 */
	public synchronized WSNApp addModule(WSNModule m) {
		if (_shuttingDown) return this; // abort if WSN was shut down or is shutting down
		if (_modules.containsKey(m.getClass())) return this; // don't add one module class twice..


		/**
		 *  proxy all events provided by this module
		 *  (a WSN should offer all events provided by its modules)
		 **/
		// add provided events
		_addModuleEvents(m);
		// subscribe to all events (= subscribe to super event Event)
		_eventSubscriber.subscribeTo(Event.class, m, this, "_moduleEventOccurred");


		/*** attach WSN and Module to each other ***/
		// add module to modules map
		synchronized(_modules) {
			_modules.put(m.getClass(), m);
		}

		// WSN::addModule does the same as WSNModule::setWSN -> they call each other
		m.setWSN(this);
		/*******************************************/

		// fire event
		this.fireEvent(new WSNModuleAddedEvent(m));
		return this;
	}
	/**
	 * Removes a module from the WSN.
	 * Has the same effect as calling m.shutdown()
	 * 
	 * @param m module
	 * @return this for fluent interface
	 */
	public synchronized WSNApp removeModule(WSNModule m) {
		if (!_modules.containsKey(m.getClass())) return this; // abort if module isn't connected to this WSN

		/*** detach WSN and Module from each other ***/
		// WSN::removeModule does the same as WSNModule::shutdown -> they call each other
		m.shutdown();


		// detach module
		synchronized (_modules) {
			_modules.remove(m.getClass());
		}
		/*********************************************/

		System.out.println("removed Module " + m.getClass().getName());

		// do shutdown stuff
		if (_shuttingDown && _modules.size() <= 0) {
			synchronized (_shutdown) {
				_shutdown.notifyAll();
				_shutdown=true;
			}
		}

		// remove events provided from module from this WSN's provided event list
		_updateDynamicProvidedEvents();

		// fire event
		this.fireEvent(new WSNModuleRemovedEvent(m));
		return this;
	}

	/**
	 * sets the path to the directory where files by this WSN and its modules are stored. Must exist and be writable.
	 * 
	 * @param path
	 * @return this for fluent interface
	 * @throws WSNException if directory not writable!
	 */
	public WSNApp setFilesDirectory(String path) {
		File d = new File(path);
		_filesPath = d.getAbsolutePath();
		if (_filesPath.charAt(_filesPath.length()-1) != '/')
			_filesPath += '/';

		return this;
	}

	/**
	 * Sets the path to the directory for temporary files.
	 * 
	 * @param path
	 * @return this for fluent interface
	 */
	public WSNApp setTemporaryFilesDirectory(String path) throws WSNException {
		File d = new File(path);
		_tempFilesPath = d.getAbsolutePath();
		if (_tempFilesPath.charAt(_tempFilesPath.length()-1) != '/')
			_tempFilesPath += '/';

		return this;
	}

	/**
	 * Tells the WSN to apply a specific UnitTranslation.
	 * 
	 * @param translation
	 * @return this for fluent interface
	 */
	public WSNApp applyUnitTranslation(MeasurementUnitTranslation translation) {

		if (translation==null) return this;

		// deactivate former active unit translation
		this.deactivateUnitTranslation(translation.getUnitType(), translation.getOriginalUnit());

		// add to the active-translations cache to make it being applied whenever a node gets updated
		_activeUnitTranslations.add(translation);

		// apply translation
		for (Node node: this.wsn().nodes().values()) {
			if (translation.apply(node)) {
				this.fireEvent(new WSNNodeUpdatedEvent(node)); // fire updated event
			}
		}

		return this;
	}
	/**
	 * Tells the WSN to apply a specific UnitTranslation.
	 * 
	 * @param unitType
	 * @param originalUnit
	 * @param destinationUnit
	 * @return
	 */
	public WSNApp applyUnitTranslation(String unitType, String originalUnit, String destinationUnit) {

		if (_unitTranslations.containsKey(unitType) && _unitTranslations.get(unitType).containsKey(originalUnit)) {
			for (MeasurementUnitTranslation translation: _unitTranslations.get(unitType).get(originalUnit)) {
				if (translation.getDestinationUnit()!=null ? translation.getDestinationUnit().equals(destinationUnit) : destinationUnit==null) {
					this.applyUnitTranslation(translation);
					break;
				}
			}
		}

		return this;
	}
	/**
	 * applies all active UnitTranslations to a node.
	 * 
	 * @param node
	 */
	public void applyUnitTranslations(Node node) {
		for (MeasurementUnitTranslation translation: _activeUnitTranslations) {
			translation.apply(node);
		}
	}
	/**
	 * Deactivates a former activated Unit-Translation.
	 * 
	 * @param translation
	 * @return this for fluent interface
	 */
	public WSNApp deactivateUnitTranslation(MeasurementUnitTranslation translation) {
		if (translation==null) return this;

		// remove the translation from the active-translations cache
		_activeUnitTranslations.remove(translation);

		return this;
	}
	/**
	 * Deactivates former activated Unit-Translations for a specific Unit.
	 * 
	 * @param translation
	 * @return this for fluent interface
	 */
	public WSNApp deactivateUnitTranslation(String unitType, String originalUnit) {
		Set<MeasurementUnitTranslation> toRemove=new HashSet<MeasurementUnitTranslation>();

		for (MeasurementUnitTranslation translation: _activeUnitTranslations) {
			if ((translation.getUnitType()!=null ? translation.getUnitType().equals(unitType) : unitType==null)
					&& (translation.getOriginalUnit()!=null ? translation.getOriginalUnit().equals(originalUnit) : originalUnit==null)) {
				toRemove.add(translation);
			}
		}

		for (MeasurementUnitTranslation translation: toRemove) {
			this.deactivateUnitTranslation(translation);
		}

		return this;
	}



	/**
	 * shuts the WSN down
	 * a shut down WSN has terminated all its modules and threads and refuses all further interaction (a shut down WSN instance can not be reused)
	 * this method doesn't block. to wait for final shutdown use waitForShutdown()
	 * 
	 * @return this for fluent interface
	 */
	public synchronized WSNApp shutdown() {
		if (_shuttingDown) return this; // abort if WSN is already shutting down

		System.out.println("WSN is shutting down..");

		// set shuttingdown state
		_shuttingDown = true;

		// remove+shut down modules
		synchronized (_modules) {
			// use array of all keys to iterate through _modules since removeModule() modifies the _modules map
			while (!_modules.isEmpty()) {
				WSNModule m = _modules.entrySet().iterator().next().getValue();
				System.out.println("shutting down module: " + m.getClass().getName());
				this.removeModule(m);
			}
		}

		// shut down event subscriber
		_eventSubscriber.shutdown();

		return this;
	}

	/**
	 * blocks until WSN is shut down
	 * @throws InterruptedException
	 * @return this for fluent interface
	 */
	public WSNApp waitForShutdown() throws InterruptedException {
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
	 * subscribes to an event that gets fired by this WSN or any of its modules.
	 * Allows to register for any event so you can register for an event currently not known but provided by a module loaded in future.
	 * 
	 * @see EventProvider#subscribeEvent
	 */
	@Override
	public boolean subscribeEvent(Class<? extends Event> eventClass, EventBuffer eventBuffer) {
		return super.subscribeEvent(eventClass, eventBuffer);
	}



	/* package methods */
	/**
	 * Synchronously pass a WSNProtocolPacket through the protocol stack
	 * @param packet
	 */
	void _processPacket(WSNProtocolPacket packet) {
		this.fireEvent(new WSNProtocolRawPacketReceivedEvent(packet));

		WSNProtocolPacket lastPacket = packet;
		if (_protocols != null) {
			for (WSNProtocol p: _protocols) {
				if (packet == null) // ABORT on NULL packet
					break;

				try {
					packet = p.process(packet);
					if (packet != null) {
						lastPacket = packet;
					}
				} catch (WSNProtocolUnsupportedPacketException e) {
					System.err.println("Unsupported-Packet Protocol Exception: " + p.getName());
					e.printStackTrace();
				} catch (WSNProtocolException e) {
					e.printStackTrace();
				}
			}

			this.fireEvent(new WSNProtocolPacketProcessedEvent(lastPacket));
		}
	}




	/* protected methods */
	/**
	 * parses the configuration file
	 * 
	 * @param configFile
	 */
	protected void _loadConfiguration(File configFile) {
		try {
			// set up xml parse helper..
			DocumentBuilderFactory factory	= DocumentBuilderFactory.newInstance();
			DocumentBuilder builder 		= factory.newDocumentBuilder();
			org.w3c.dom.Document config		= builder.parse(configFile);
			XPath xpath						= XPathFactory.newInstance().newXPath();


			/*** load unit translations ***/
			try {
				Queue<MeasurementUnitTranslation> translationQueue = new LinkedList<MeasurementUnitTranslation>(); // translation queue for inferred translations

				// add <unit-translations>s
				org.w3c.dom.NodeList unitTranslations = (NodeList) xpath.compile("/configuration/unit-translations/for/translation").evaluate(config, XPathConstants.NODESET);
				for (int unitTranslationIterator=0; unitTranslationIterator<unitTranslations.getLength(); unitTranslationIterator++) {

					org.w3c.dom.Node unitTranslation = unitTranslations.item(unitTranslationIterator);

					String unitType=null;
					String originalUnit=null;
					String destinationUnit=null;
					String script=null;

					if (unitTranslation.getParentNode().getNodeName().equalsIgnoreCase("for") && unitTranslation.getParentNode().getAttributes().getNamedItem("type")!=null) {
						unitType = unitTranslation.getParentNode().getAttributes().getNamedItem("type").getNodeValue();
					}
					if (unitTranslation.getAttributes().getNamedItem("from") != null) {
						originalUnit = unitTranslation.getAttributes().getNamedItem("from").getNodeValue();
					}
					if (unitTranslation.getAttributes().getNamedItem("to") != null) {
						destinationUnit = unitTranslation.getAttributes().getNamedItem("to").getNodeValue();
					}
					if (unitTranslation.getTextContent() != null) {
						script = unitTranslation.getTextContent().trim();
					}

					try {
						MeasurementUnitTranslation translation = new MeasurementUnitTranslation(unitType, originalUnit, destinationUnit, script);

						// add it to the global cache
						if (!_unitTranslations.containsKey(translation.getUnitType())) {
							_unitTranslations.put(translation.getUnitType(), new HashMap<String,Set<MeasurementUnitTranslation>>());
						}
						if (!_unitTranslations.get(translation.getUnitType()).containsKey(translation.getOriginalUnit())) {
							_unitTranslations.get(translation.getUnitType()).put(translation.getOriginalUnit(), new HashSet<MeasurementUnitTranslation>());
						}
						_unitTranslations.get(translation.getUnitType()).get(translation.getOriginalUnit()).add(translation);

						// add it to the queue for inferences
						translationQueue.add(translation);
					} catch (ScriptException e) {
						System.err.println("Couldn't compile Unit-Translation Script: ("+unitType+") " + originalUnit + " -> " + destinationUnit);
						e.printStackTrace();
					}
				}

				// construct implicit translations (if °F->°C exists and °C->K exists, °F->K exists too)
				while (!translationQueue.isEmpty()) {
					MeasurementUnitTranslation t1 = translationQueue.remove();

					// search inferences
					if (_unitTranslations.containsKey(t1.getUnitType()) && _unitTranslations.get(t1.getUnitType()).containsKey(t1.getDestinationUnit())) {
						for (MeasurementUnitTranslation t2: _unitTranslations.get(t1.getUnitType()).get(t1.getDestinationUnit())) {
							MeasurementUnitTranslation inference = new MeasurementUnitTranslationInference(t1,t2);
							if (inference.getOriginalUnit()!=null ? inference.getOriginalUnit().equals(inference.getDestinationUnit()) : inference.getDestinationUnit()==null)
								continue; // ignore if inference is a reflexive one (A -> A)

							if (!_unitTranslations.get(inference.getUnitType()).get(inference.getOriginalUnit()).contains(inference)) {
								_unitTranslations.get(inference.getUnitType()).get(inference.getOriginalUnit()).add(inference);
								translationQueue.add(inference);
							}
						}
					}
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * override EventProvider._eventRegistrationAllowed to allow subscriber to register for any event.
	 * Makes sure that Modules can register for events not known at the moment (to allow class A to register for an event fired by module B that will get loaded in future).
	 * 
	 * @param eventClass
	 */
	@Override
	protected boolean _eventRegistrationAllowed(Class<? extends Event> eventClass) {
		return true;
	}

	/**
	 * callback for all events thrown by any module.
	 * Rethrows any event for subscriber of this WSN
	 * @param e
	 */
	protected void _moduleEventOccurred(Event e) {
		this.fireEvent(e);
	}

	/**
	 * adds the events provided by module m to this WSN's provided events list
	 * @param m
	 */
	protected void _addModuleEvents(WSNModule m) {
		synchronized (this.providedEvents) {
			synchronized (this._dynamicProvidedEvents) {
				if (m.getProvidedEvents() != null) {
					HashSet<Class<? extends Event>> moduleEvents = new HashSet<Class<? extends Event>>(Arrays.asList(m.getProvidedEvents()));
					HashSet<Class<? extends Event>> providedEvents = new HashSet<Class<? extends Event>>(Arrays.asList(this.getProvidedEvents()));

					providedEvents.addAll(moduleEvents);

					try {
						@SuppressWarnings("unchecked")
						Class<? extends Event>[] temp = new Class[providedEvents.size()];
						providedEvents.toArray(temp);

						this._dynamicProvidedEvents = this._dynamicProvidedEvents.getClass().cast(temp);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}
	}
	/**
	 * updates this._dynamicProvidedEvents.
	 * may be called whenever a module was added or removed
	 */
	protected void _updateDynamicProvidedEvents() {
		synchronized (this.providedEvents) {
			synchronized (this._dynamicProvidedEvents) {
				this._dynamicProvidedEvents = this.providedEvents;

				for (WSNModule m: _modules.values()) {
					this._addModuleEvents(m);
				}
			}
		}
	}


	/* protected member */
	/** @var WSN driver module */
	protected WSNDriver _driver=null;
	/** @var protocol stack */
	protected WSNProtocol[] _protocols=null;
	/** @var array of all registered modules */
	protected HashMap<Class<? extends WSNModule>,WSNModule> _modules = new HashMap<Class<? extends WSNModule>,WSNModule>();
	/** @var contains all events provided by this WSN (WSN events as well as all events provided by modules) **/
	protected Class<? extends Event>[] _dynamicProvidedEvents = null;
	/** @var true if WSN is shut down */
	protected Boolean _shutdown = false;
	/** @var true if WSN is currently shutting down */
	protected boolean _shuttingDown = false;
	/** event subscriber (handling occured events in a seperate thread) */
	protected ThreadedEventSubscriber _eventSubscriber=new ThreadedEventSubscriber("WSN Events");

	/** @var path to folder where files are stored */
	protected String _filesPath = null;
	/** @var path to temporary files folder */
	protected String _tempFilesPath = null;

	/** represents the WSN of this WSNApp **/
	protected WSN _wsn = new WSN(this);
	/** @var holds meta-data about this WSN */
	protected Map<Class<? extends WSNMetaData>,WSNMetaData> _metaData=new HashMap<Class<? extends WSNMetaData>, WSNMetaData>();
	/** @var holds unit translations */
	protected Map<String,Map<String,Set<MeasurementUnitTranslation>>> _unitTranslations = new HashMap<String,Map<String,Set<MeasurementUnitTranslation>>>();
	/** @var set of UnitTranslations that should be applied */
	protected Set<MeasurementUnitTranslation> _activeUnitTranslations = new HashSet<MeasurementUnitTranslation>();



	/**
	 * Representing the Identity of a WSN.
	 * Can be used to compare two WSNs regarding their structure. Can perfectly be serialized!
	 *
	 * Compares the WSNs regarding their nodes to see if they are similar..
	 * May be used to recognize an already seen WSN to load specific configurations etc..
	 * 
	 * @author André Freitag
	 */
	public static class Identity implements Serializable {

		/**
		 * constructs the WSN.Identity of a given WSN.
		 * 
		 * @param wsn
		 */
		public Identity(WSNApp wsn) {
			_nodes = new ArrayList<Node>();
			Collection<Node> col = wsn.wsn().nodes().values();
			for (Node node: col) {
				_nodes.add(node);
			}
		}

		/**
		 * Compares the given identity to this Identity.
		 * Returns a Identity.CompareResult that specifies if theses identities may be considered the same as well as an degree how certain this decision was.
		 * 
		 * Considers the nodes of the WSNs.
		 * 
		 * @param identity
		 */
		public CompareResult compare(Identity identity) {
			List<Node> ownnodes = this._nodes;
			List<Node> othernodes = identity._nodes;

			synchronized (ownnodes) {
				synchronized (othernodes) {
					int ownsize=ownnodes.size();
					int othersize=othernodes.size();

					// remove all nodes that both wsns have in common
					List<Node> temp = new ArrayList<Node>(ownnodes);
					for (Node node: temp) {
						if (node==null) {
							ownnodes.remove(node);
							continue;
						}

						for (Node othernode: othernodes) {
							if (othernode==null) {
								othernodes.remove(othernode);
								continue;
							}

							if (othernode.same(node)) {
								ownnodes.remove(node);
								othernodes.remove(othernode);
								break;
							}
						}
					}

					// consider the amount of nodes that both identities have in common relative to their own size
					// add both numbers and divide by two.
					float certainty = ((ownsize!=0 ? ((float)(ownsize-ownnodes.size()))/((float)ownsize) : 1.0f)
							+ (othersize!=0 ? ((float)(othersize-othernodes.size()))/((float)othersize) : 1.0f))

							/ 2.0f;

					return new CompareResult(certainty>0.5, certainty);

				}
			}
		}

		/**
		 * representing the result of compare()
		 * 
		 * @see WSNApp.Identity#compare(WSNApp.Identity)
		 * @author André Freitag
		 */
		public static class CompareResult {
			/** @var determines whether the compared Identities are considered the same (this.certainty>0.5) */
			public final boolean same;
			/** @var specifying the degree of similarity of the two Identities, 0 -> no chance these are similar, 1 -> they are 100% similar */
			public final float certainty;

			public CompareResult(boolean same, float certainty) {
				this.same = same;
				this.certainty = certainty;
			}
		}


		/** serialization
		 * @throws IOException **/
		private void writeObject(ObjectOutputStream oos) throws IOException {
			oos.writeObject(_nodes);
		}
		/** deserialization
		 * @throws ClassNotFoundException
		 * @throws IOException **/
		@SuppressWarnings("unchecked")
		private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
			_nodes = (List<Node>) s.readObject();
		}

		protected List<Node> _nodes;
		private static final long serialVersionUID = 255111791976056608L;
	}
}
