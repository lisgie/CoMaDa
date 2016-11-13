package de.tum.in.net.WSNDataFramework.Modules.SSHServer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.tum.in.net.WSNDataFramework.WSNModule;
import de.tum.in.net.WSNDataFramework.Modules.SSHServer.SimpleANSITerminal.TerminalString;



/**
 * SSH Controller. Handles SSH commands.
 * Calls argCommand(SSHCommand) for each command that gets called and that belongs to this controller.
 * E.g: If this controller is registered for the "test" command and a "test arg1 arg2" is called, first of all this.handleSSHCommand(SSHCommand) is called and after that this.arg1Command(SSHCommand).
 * If no method arg1Command(SSHCommand) is found, helpCommand(SSHCommand) is called to trap unknown commands.
 * 
 * *Command(SSHCommand) must return a TerminalString that gets put out to RemoteTerminal.
 * handleSSHCommand(SSHCommand) may be overriden to trap commands that don't fit the "controllerName arg" pattern (in this case don't forget to call parent::handleSSHCommand(SSHCommand) if you don't want to completely deactivate the "controllerName arg" pattern).
 * 
 * @author André Freitag
 */
public abstract class WSNSSHController implements SSHCommandHandler {

	/**
	 * tries to call cmd.args[0]Command(SSHCommand). If no such method exists it tries to call helpCommand(SSHCommand).
	 * 
	 * @return TerminalString to output to the RemoteTerminal
	 */
	@Override
	public TerminalString handleSSHCommand(SSHCommand cmd) {
		TerminalString ret=null;

		boolean handled=false;
		if (cmd.args.size() > 0) {
			try {

				Method m = this.getClass().getMethod(cmd.args.get(0).toLowerCase()+"Command", SSHCommand.class);
				Object temp = m.invoke(this, cmd);
				if (temp instanceof TerminalString) {
					ret = (TerminalString)temp;
					handled=true;
				}

			} catch (NoSuchMethodException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}

		if (!handled) {
			try {

				Method m = this.getClass().getMethod("helpCommand", SSHCommand.class);
				Object temp = m.invoke(this, cmd);
				if (temp instanceof TerminalString) {
					ret = (TerminalString)temp;
				}

			} catch (NoSuchMethodException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}

		return ret;
	}


	/**
	 * get module this controller belongs to.
	 * 
	 * @return
	 */
	public WSNModule getModule() {
		return _module;
	}

	/**
	 * get server module this controller belongs to.
	 * 
	 * @return
	 */
	public WSNSSHServerModule getServerModule() {
		return _serverModule;
	}

	/**
	 * set module this controller belongs to.
	 * 
	 * @param module
	 * @return this for fluent interface
	 */
	public WSNSSHController setModule(WSNModule module) {
		_module = module;
		return this;
	}

	/**
	 * set server module this controller belongs to.
	 * 
	 * @param module
	 * @return this for fluent interface
	 */
	public WSNSSHController setServerModule(WSNSSHServerModule module) {
		_serverModule = module;
		return this;
	}



	/* protected member */
	protected WSNModule _module=null;
	protected WSNSSHServerModule _serverModule=null;
}
