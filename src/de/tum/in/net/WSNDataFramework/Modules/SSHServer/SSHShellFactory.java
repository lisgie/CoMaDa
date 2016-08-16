package de.tum.in.net.WSNDataFramework.Modules.SSHServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import de.tum.in.net.WSNDataFramework.Modules.SSHServer.SimpleANSITerminal.TerminalString;



/**
 * ShellFactory, creates a SimpleANSITerminal for every connection.
 * 
 * @author André Freitag
 */
public class SSHShellFactory implements Factory<Command> {

	/**
	 * Creates new SimpleANSITerminal
	 */
	@Override
	public Command create() {
		return new SSHShell(_handler);
	}

	/**
	 * registers a handler.
	 * handler gets called for each command starting with `name`.
	 * 
	 * @param handlerName
	 * @param handler
	 * @return false if handlerName was already registered
	 */
	public boolean registerHandler(String handlerName, SSHCommandHandler handler) {
		if (!_handler.containsKey(handlerName)) {
			_handler.put(handlerName, handler);
			return true;
		}

		return false;
	}
	/**
	 * gets registered handler
	 */
	public Map<String,SSHCommandHandler> getRegisteredHandler() {
		return new HashMap<String,SSHCommandHandler>(_handler);
	}

	/* protected member */
	protected Map<String,SSHCommandHandler> _handler=new HashMap<String,SSHCommandHandler>();

	/**
	 * org.apache.sshd.server.Command wrapper around SimpleANSITerminal
	 * 
	 * @author André Freitag
	 */
	protected class SSHShell implements Command {

		public SSHShell(Map<String,SSHCommandHandler> handler) {
			// add "exit" handler
			handler.put("exit", new SSHCommandHandler() {

				@Override
				public TerminalString handleSSHCommand(SSHCommand cmd) {
					SSHShell.this.destroy();
					return new TerminalString("exiting..");
				}

			});
			this._handler = handler;
		}

		/* Shell Factory implementations */
		@Override
		public void setInputStream(InputStream in) {
			this.in = in;
		}

		@Override
		public void setOutputStream(OutputStream out) {
			this.out = out;
		}

		@Override
		public void setErrorStream(OutputStream err) {
			//            this.err = err;
		}

		@Override
		public void setExitCallback(ExitCallback callback) {
			exitCallback=callback;
		}

		@Override
		public void start(Environment env) throws IOException {
			terminal=new SimpleANSITerminal(in,out,_handler);
		}

		@Override
		public void destroy() {
			terminal.stop();
			if (exitCallback!=null) {
				exitCallback.onExit(0);
			}
		}



		/* protected member */
		public SimpleANSITerminal terminal=null;
		public Map<String,SSHCommandHandler> _handler=null;
		protected InputStream in=null;
		protected OutputStream out=null;
		//		protected OutputStream err=null;
		protected ExitCallback exitCallback=null;
	}
}
