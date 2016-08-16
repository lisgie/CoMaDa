package de.tum.in.net.WSNDataFramework.Modules.Web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.xml.bind.DatatypeConverter;


public class WSNSQLConnector {
	private Connection _connect = null;
	private Statement _statement = null;
	private ResultSet _resultSet = null;
	private String _link = null;
	private boolean _isConnected = false;
	
	public WSNSQLConnector() {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
		    _connect = DriverManager.getConnection("jdbc:mysql://" + Config._mysqlHost + "/" + Config._mysqlDB + "?" + "user=" + Config._mysqlUser + "&password=" + Config._mysqlPW);
		    if(_connect!=null) {
		    	_isConnected = true;
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//for JUnit tests
	public WSNSQLConnector(String driver, String connection) throws Exception {
		// this will load the MySQL driver, each DB has its own driver
		Class.forName(driver);
		// setup the connection with the DB.
	    _connect = DriverManager.getConnection(connection);
	    if(_connect!=null) {
	    	_isConnected = true;
	    }
	}
	
	public boolean isConnected() {
		return _isConnected;
	}
	
	/**
	 * Check if someone is allowed to upload his WSN to the Website
	 * For this, it first verifies if User / PW combination exists in DB 
	 * Then, it checks if that user has permission to upload
	 * 
	 * @param user  username that the person uses for identification
	 * @param pw    password that is assigned to that username
	 * @return boolean  true if that user has the given name / pw combination and the rights to upload. False otherwise
	 */
	public boolean isAuthorized(String user, String pw) {
		boolean isAuthorized = false;
//		String requestURL = Config._webURL+"/checkAuthorization.php";
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("username", user);
//		params.put("password", pw);
//		try {
//			HttpUtility.sendPostRequest(requestURL, params);
//			String response = HttpUtility.readSingleLineRespone();
//			if(response.equals("true")) {
//				isAuthorized = true;
//			}
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//		HttpUtility.disconnect();
		
		ResultSet result = queryDB("SELECT * from USER where USERNAME='"+user+"'");
		if(result!=null) {			
			try {
				if(result.isBeforeFirst()) {
					result.next();
					String correctHash = result.getString("PASSWORD");
					if(validatePassword(pw.toCharArray(), correctHash)) {
						String permission = result.getString("STATUS");
						isAuthorized = (permission.equals("Admin") || permission.equals("All Rights") || permission.equals("Upload"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			//if authorization failed -> close connection
			if(!isAuthorized) {
				closeConnection();
			}
		}		
		return isAuthorized;
	}

	public void addWSN(String user) {		
			String link = randomString(10);
			while(alreadyUsed(link)){
				link = randomString(10);
			}
			updateDB("INSERT INTO `" + Config._mysqlDB + "`.`ACTIVE_WSN` (`USERNAME`, `WSNLINK`) VALUES ('"+user+"', '"+link+"');");
			this._link = link;
			updateDB("CREATE TABLE "+link+"_DATA (NODE_ID varchar(30), DATUM_ID varchar(30), NAME varchar(30), TYPE varchar(30), VALUE varchar(30), UNIT varchar(30))");
			updateDB("CREATE TABLE "+link+"_TOPOLOGY (SOURCE varchar(30), TARGET varchar(30), WEIGHT double)");
			updateDB("CREATE TABLE `"+link+"_PROTOCOL` (`TEXT` varchar(800) COLLATE latin1_general_ci NOT NULL, `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP)");
			updateDB("CREATE TABLE `"+link+"_DATASTREAM` (`FEED_NAME` varchar(50) NOT NULL, `STREAM_ID` varchar(50) NOT NULL, `URL` varchar(250) NOT NULL)");
	}
	
	public String getLink() {
		return this._link;
	}
	
	public void cleanUp() {
		if(_isConnected) {
			updateDB("DELETE FROM `" + Config._mysqlDB + "`.`ACTIVE_WSN` WHERE WSNLINK='"+_link+"';");
			updateDB("DROP TABLE "+_link+"_DATA");
			updateDB("DROP TABLE "+_link+"_TOPOLOGY");
			updateDB("DROP TABLE "+_link+"_PROTOCOL");
			updateDB("DROP TABLE "+_link+"_DATASTREAM");
			closeConnection();
		}
	}
	
	private void closeConnection() {		
		_link = null;
		try {				
			_statement.close();
			_connect.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}		
		_isConnected = false;
	}

	private ResultSet queryDB(String query) {
		if(_isConnected) {			
			try {
				_statement = _connect.createStatement();
				_resultSet = _statement.executeQuery(query);
				// resultSet gets the result of the SQL query
				return _resultSet;
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} else {			
			return null;
		}
	}
	
	private void updateDB(String query) {
		if(_isConnected) {		
			try {
				_statement = _connect.createStatement();
				_statement.executeUpdate(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private String randomString(int length) {
		String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(length);
		for(int i=0; i<length; i++) {
			sb.append(chars.charAt(rnd.nextInt(chars.length())));
		}
		return sb.toString();
	}
	
	private boolean validatePassword(char[] password, String correctHash) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the hash into its parameters
        String[] params = correctHash.split(":");
        int iterations = Integer.parseInt(params[Config.ITERATION_INDEX]);
        byte[] salt = fromBase64(params[Config.SALT_INDEX]);
        byte[] hash = fromBase64(params[Config.PBKDF2_INDEX]);
        // Compute the hash of the provided password, using the same salt, iteration count, and hash length
        byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
        // Compare the hashes in constant time. The password is correct if both hashes match.
        return slowEquals(hash, testHash);
	}
	
	private boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for(int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }
	
	private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(Config.PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
	
	private static byte[] fromBase64(String hex) {
        return DatatypeConverter.parseBase64Binary(hex);
    }
	
	public boolean alreadyUsed(String link) {
		ResultSet result = queryDB("SELECT * from ACTIVE_WSN where WSNLINK='"+link+"';");
		try {
			return result.isBeforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
