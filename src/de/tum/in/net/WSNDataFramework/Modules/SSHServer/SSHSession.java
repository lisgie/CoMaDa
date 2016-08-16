package de.tum.in.net.WSNDataFramework.Modules.SSHServer;

/**
 * represents a SSH Session
 * 
 * @author André Freitag
 */
public class SSHSession {

	/**
	 * constructor.
	 * 
	 * @param username
	 * @param sessionID
	 */
	public SSHSession(String username, String sessionID) {
		this._username = username;
		this._sessionID = sessionID;
	}
	
	/**
	 * gets username.
	 * 
	 * @return username
	 */
	public String getUsername() {
		return this._username;
	}
	
	/**
	 * gets session id.
	 * 
	 * @return session ID
	 */
	public String getSessionID() {
		return this._sessionID;
	}
	
	
	/* protected member */
	protected String _username;
	protected String _sessionID;
}
