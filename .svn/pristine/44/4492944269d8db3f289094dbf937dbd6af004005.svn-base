package de.tum.in.net.WSNDataFramework.Modules.HTTPServer;

import de.tum.in.net.WSNDataFramework.WSNModule;

/**
 * wrapper around HTTPController to offer a interface for getting WSNModules related to this controller.
 * 
 * @author André Freitag
 * @see HTTPController HTTPController for further information
 */
public abstract class WSNHTTPController extends HTTPController {


	/* constructors */
	/**
	 * default constructor. DISCOURAGED since its creates a WSNHTTPController with null references for its modules.
	 * Use one of the other constructors.
	 */
	public WSNHTTPController() {
		this(null, null, null);
	}

	/**
	 * constructs a WSNHTTPController, given its workingDirectory plus references for the module this controller belongs to as well as the server module this controller was registered at.
	 * 
	 * @param workingDirectory directory
	 * @param module module this controller belongs to
	 * @param serverModule module this controller was registered at
	 */
	public WSNHTTPController(String workingDirectory, WSNModule module, WSNHTTPServerModule serverModule) {
		this.setWorkingDirectory(workingDirectory);
		this.setModule(module);
		this.setServerModule(serverModule);
	}


	/* methods */
	/**
	 * gets the module this controller belongs to.
	 * 
	 * @return WSNModule this controller belongs to.
	 */
	public WSNModule module() {
		return _module;
	}
	/**
	 * sets the module this controller belongs to
	 * 
	 * @param module
	 * @return this for fluent interface
	 */
	public WSNHTTPController setModule(WSNModule module) {
		_module = module;
		return this;
	}
	/**
	 * gets the server module this controller was created registered for.
	 * 
	 * @return WSNModule
	 */
	public WSNHTTPServerModule getServerModule() {
		return _serverModule;
	}
	public WSNHTTPController setServerModule(WSNHTTPServerModule serverModule) {
		_serverModule = serverModule;
		return this;
	}

	/**
	 * sets this controller's working directory.
	 * 
	 * @param dir
	 * @return this for fluent interface
	 */
	@Override
	public WSNHTTPController setWorkingDirectory(String workingDirectory) {
		super.setWorkingDirectory(workingDirectory);
		return this;
	}



	/* protected member */
	protected WSNModule _module=null;
	protected WSNHTTPServerModule _serverModule=null;
}
