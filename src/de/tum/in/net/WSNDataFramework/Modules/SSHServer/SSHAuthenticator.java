package de.tum.in.net.WSNDataFramework.Modules.SSHServer;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;


/**
 * Dummy Password Authenticator. Always returns true.
 * @author André Freitag
 *
 */
public class SSHAuthenticator
{
	/**
	 * Check the validity of a password.
	 * This method should return false if the authentication fails.
	 *
	 * @param username the username
	 * @param password the password
	 * @param session the server session
	 * @return a boolean indicating if authentication succeeded or not
	 */
	public boolean authenticate(String username, String password, SSHSession session) {
		boolean authenticated=false;

		// check if there's an entry for the given username
		if (_passwords.containsKey(username)) {
			String neededPW = _passwords.get(username);

			// user is authenticated if the neededPW is either NULL or equals the given one
			authenticated = neededPW==null || neededPW!=null && neededPW.equals(password);
		}

		// check global password entry (NULL)
		if (authenticated==false && _passwords.containsKey(null)) {
			String neededPW = _passwords.get(null);

			// user is authenticated if the neededPW is either NULL or equals the given one
			authenticated = neededPW==null || neededPW!=null && neededPW.equals(password);
		}

		return authenticated;
	}

	/**
	 * Check the validity of a PublicKey.
	 * This method should return false if the authentication fails.
	 *
	 * @param username the username
	 * @param key public key provided by the user
	 * @param session the server session
	 * @return a boolean indicating if authentication succeeded or not
	 */
	public boolean authenticate(String username, PublicKey key, SSHSession session) {
		boolean authenticated=false;

		// check if there's an entry for the given username
		if (_keys.containsKey(username)) {
			PublicKey neededKey = _keys.get(username);

			// user is authenticated if the neededKey is either NULL or equals the given one
			authenticated = neededKey==null || neededKey!=null && neededKey.equals(key);
		}

		// check global key entry (NULL)
		if (authenticated==false && _keys.containsKey(null)) {
			PublicKey neededKey = _keys.get(null);

			// user is authenticated if the neededKey is either NULL or equals the given one
			authenticated = neededKey==null || neededKey!=null && neededKey.equals(key);
		}

		return authenticated;
	}

	/**
	 * adds a Username+Password combination that is allowed to use the SSHServer
	 * 
	 * @param username username | NULL if a general passwords should be set (counts for ALL users)
	 * @param password password | NULL if any password should authenticate the given user
	 * @return this for fluent interface
	 */
	public SSHAuthenticator addPassword(String username, String password) {
		synchronized (_passwords) {
			_passwords.put(username, password);
		}

		return this;
	}

	/**
	 * 
	 * @param username username | NULL if a general key should be set (counts for ALL users)
	 * @param key public key | NULL if any key should authenticate the given user
	 * @return return this for fluent interface
	 */
	public SSHAuthenticator addPublicKey(String username, PublicKey key) {
		synchronized (_keys) {
			_keys.put(username, key);
		}

		return this;
	}


	protected Map<String,String> _passwords=new HashMap<String,String>();
	protected Map<String,PublicKey> _keys=new HashMap<String,PublicKey>();
}
