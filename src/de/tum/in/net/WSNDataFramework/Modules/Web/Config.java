package de.tum.in.net.WSNDataFramework.Modules.Web;

public class Config {
	
	//public static final String _mysqlHost = "localhost";
	public static final String _mysqlHost = "192.41.136.222:43306";
	//public static final String _mysqlDB = "wsndb";
	public static final String _mysqlDB = "wsndb";
	//public static final String _mysqlUser = "sqluser";
	public static final String _mysqlUser = "wsn_web_user";
	//public static final String _mysqlPW = "sqluserpw";
	public static final String _mysqlPW = ".wsn_web_2014!";
	//public static final String _webURL = "http://localhost/wsn"; 
	public static final String _webURL = "https://www.corinna-schmitt.de/wsn"; 
	
	public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
	public static final int ITERATION_INDEX = 1;
	public static final int SALT_INDEX = 2;
	public static final int PBKDF2_INDEX = 3;
}
