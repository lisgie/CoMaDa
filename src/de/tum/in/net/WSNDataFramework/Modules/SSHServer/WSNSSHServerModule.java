package de.tum.in.net.WSNDataFramework.Modules.SSHServer;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.WSNModule;
import de.tum.in.net.WSNDataFramework.Modules.SSHServer.SimpleANSITerminal.TerminalString;


/**
 * Module for handling Client Communication via SSH.
 * 
 * @author André Freitag
 */
public class WSNSSHServerModule extends WSNModule {

	/**
	 * Set up WSNSSHServerModule
	 * 
	 * @param port port to listen to
	 * @param pathToKeyFile path to keyfile
	 */
	public WSNSSHServerModule(int port, String pathToKeyFile) {

		_server = new SSHServer(port,
				pathToKeyFile,
				new SSHAuthenticator(),
				new SSHShellFactory());

		this.registerController("shutdown", new WSNSSHController() {
			@Override
			public TerminalString handleSSHCommand(SSHCommand cmd) {
				if (this.getModule()!=null && this.getModule().app()!=null) {
					this.getModule().app().shutdown();
				}

				return new TerminalString("shutting down..");
			}
		}, this);

		this.registerController("help", new WSNSSHServerModuleHelpController(), this);
		this.registerController("nodes", new WSNSSHServerModuleNodesController(), this);
		this.registerController("topology", new WSNSSHServerModuleTopologyController(), this);

		_setRunning("up and running");
	}
	/**
	 * Set up WSNSSHServerModule, generates a new SSH key pair and stores it in "hostkey.ser"
	 * @param port
	 */
	public WSNSSHServerModule(int port) {
		this(port, "hostkey.ser");
	}
	/**
	 * set up WSNSSHServerModule, uses default keyfile and port 22
	 */
	public WSNSSHServerModule() {
		this(22, "hostkey.ser");
	}
	/**
	 * name
	 */
	@Override
	public String getName() {
		return "SSH Server";
	}

	/**
	 * register a controller (forwards to SSHServer.registerHandler()).
	 * controller gets called for every command that starts with `name`.
	 * 
	 * @param controllerName
	 * @param controller
	 * @param module module the controller belongs to
	 * @return false if controllerName was already registered
	 */
	public boolean registerController(String controllerName, WSNSSHController controller, WSNModule module) {
		return _server.registerHandler(controllerName, controller.setServerModule(this).setModule(module));
	}
	/**
	 * get registered controller.
	 */
	public Map<String,WSNSSHController> getRegisteredController() {
		HashMap<String,WSNSSHController> ret = new HashMap<String,WSNSSHController>();

		Map<String,SSHCommandHandler> controller = _server.getRegisteredHandler();
		for (String cn: controller.keySet()) {
			SSHCommandHandler c = controller.get(cn);
			if (c instanceof WSNSSHController) {
				ret.put(cn, (WSNSSHController)c);
			}
		}

		return ret;
	}

	/**
	 * gets the SSHAuthenticator instance that is used for authenticating user.
	 * 
	 * @return SSHAuthenticator instance
	 */
	public SSHAuthenticator getAuthenticator() {
		return _server.getAuthenticator();
	}


	/* protected helper */
	@Override
	protected void _init() {
		try {
			_server.start();
		} catch (IOException e) {
		}
	}
	@Override
	protected void _shutdown() {
		try {
			_server.stop(true);
		} catch (InterruptedException e) {
		}
	}



	/* private member */
	protected SSHServer _server;
}
