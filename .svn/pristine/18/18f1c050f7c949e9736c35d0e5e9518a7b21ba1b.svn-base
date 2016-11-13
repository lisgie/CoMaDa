package de.tum.in.net.WSNDataFramework.Modules.SSHServer;

import java.io.IOException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

/**
 * SSH Server. Wrapper around org.apache.sshd.SshServer.
 * 
 * @author André Freitag
 */
public class SSHServer {
	/**
	 * set up SSHServer.
	 * 
	 * @param port
	 * @param pathToKeyFile
	 * @param authenticator
	 * @param shellFactory
	 */
	public SSHServer(int port, String pathToKeyFile, SSHAuthenticator authenticator, SSHShellFactory shellFactory) {
		_port = port;
		_keyFile = pathToKeyFile;
		_authenticator = authenticator;
		_shellFactory = shellFactory;

		_initSshServer();
	}

	/**
	 * start server.
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		_sshd.start();
	}

	/**
	 * stop server, blocks until all resources are freed.
	 * 
	 * @throws InterruptedException
	 */
	public void stop() throws InterruptedException {
		this.stop(false);
	}
	/**
	 * stop server.
	 * 
	 * @param immediately if false it blocks until all resources are freed.
	 * @throws InterruptedException
	 */
	public void stop(boolean immediately) throws InterruptedException {
		_sshd.stop(immediately);
	}

	/**
	 * registers a handler (forwards to SSHShellFactory.registerHandler()).
	 * handler gets called for each command starting with `name`.
	 * 
	 * @param handlerName
	 * @param handler
	 * @return false if handlerName was already registered
	 */
	public boolean registerHandler(String handlerName, SSHCommandHandler handler) {
		return _shellFactory!=null
				? _shellFactory.registerHandler(handlerName, handler)
						: false;
	}
	/**
	 * gets registered handler
	 */
	public Map<String,SSHCommandHandler> getRegisteredHandler() {
		return _shellFactory!=null
				? _shellFactory.getRegisteredHandler()
						: null;
	}

	/**
	 * gets the SSHAuthenticator instance that is used for authenticating user.
	 * 
	 * @return SSHAuthenticator instance
	 */
	public SSHAuthenticator getAuthenticator() {
		return _authenticator;
	}



	/* protected helper */
	protected void _initSshServer() {
		_sshd = SshServer.setUpDefaultServer();
		_sshd.setPort(_port);
		_sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(_keyFile));

		_sshd.setPublickeyAuthenticator(new PublickeyAuthenticator() {

			@Override
			public boolean authenticate(String username, PublicKey key,	ServerSession session) {
				return _authenticator != null
						? _authenticator.authenticate(username, key, _convertSession(session))
								: true;
			}

		});
		_sshd.setPasswordAuthenticator(new PasswordAuthenticator() {

			@Override
			public boolean authenticate(String username, String password, ServerSession session) {
				return _authenticator != null
						? _authenticator.authenticate(username, password, _convertSession(session))
								: true;
			}

		});

		_sshd.setShellFactory(_shellFactory);
	}
	/**
	 * converts a org.apache.sshd.session.ServerSession to a SSHSession
	 * 
	 * @param org.apache.sshd.session.ServerSession
	 * @return SSHSession
	 */
	protected SSHSession _convertSession(ServerSession session) {
		String sessionID="";

		byte[] temp = session.getSessionId();
		for (byte b: temp) {
			sessionID += b;
		}

		return new SSHSession(session.getUsername(), sessionID);
	}


	/* protected member */
	protected int _port;
	protected String _keyFile;
	protected SSHAuthenticator _authenticator;
	protected SSHShellFactory _shellFactory;
	protected SshServer _sshd;
	protected HashMap<String,SSHCommandHandler> _handler=new HashMap<String,SSHCommandHandler>();
}
