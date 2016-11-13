package de.tum.in.net.WSNDataFramework.Modules.HTTPServer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * represents a HTTP request
 * 
 * @author André Freitag
 */
public class HTTPRequest {

	/**
	 * requested relative URI.
	 * ('/controller/action/path' -> '/controller/action/path')
	 */
	public String requestURI;
	/**
	 * controller name. string between first and second '/' in URI.
	 * ('/controller/action/path' -> 'controller')
	 */
	public String controller;
	/**
	 * action name. string between second and third '/' in URI.
	 * ('/controller/action/path' -> 'action')
	 */
	public String action;
	/**
	 * action path. string after third '/' in URI.
	 * ('/controller/action/path' -> 'path')
	 */
	public String actionpath;
	/**
	 * given arguments. (usually GET+POST arguments).
	 */
	public URIArgs arguments;



	/**
	 * default constructor.
	 */
	public HTTPRequest() {
		this.requestURI = "";
		this.controller = "";
		this.action = "";
		this.actionpath = "";
		this.arguments = new URIArgs();
	}
	/**
	 * create HTTPRequest with given URI and request body.
	 * 
	 * @param requestURI (relative to host, http://localhost/controller/action/path -> /controller/action/path)
	 * @param requestBody
	 */
	public HTTPRequest(String requestURI, String requestBody) {
		/* initialize public member */
		this();

		/* parse request */
		String getArgStr=null;
		String postArgStr=null;

		// parse URI
		this.requestURI = requestURI;
		try {
			Matcher uriMatcher = Pattern.compile("^(/.*?)((\\?((.*))$)|$)").matcher(requestURI);
			if (uriMatcher.find()) {
				String ctrlPart = uriMatcher.group(1);
				String argPart = uriMatcher.group(4);

				if (argPart!=null && !argPart.trim().equals(""))
					getArgStr = argPart.trim();

				uriMatcher = Pattern.compile("^/(.*?)((/(.*?)((/(.*)$)|$))|$)").matcher(ctrlPart);
				if (uriMatcher.find()) {
					this.controller = uriMatcher.group(1)!=null ? uriMatcher.group(1).trim() : "";
					this.action = uriMatcher.group(4)!=null ? uriMatcher.group(4).trim() : "";
					this.actionpath = uriMatcher.group(6)!=null ? uriMatcher.group(6).trim() : "/";
				}
			} else {
				this.controller = null;
				this.action = null;
				this.actionpath = null;
			}
		} catch(Exception e) {
		}

		// parse requestBody
		postArgStr = requestBody;

		// parse arguments
		String argStr = (getArgStr!=null)
				? (getArgStr + (postArgStr!=null ? "&"+postArgStr : ""))
						: postArgStr;
				if (argStr != null) {
					this.arguments = new URIArgs(argStr);
				}
	}


	/**
	 * Representing URI-Arguments.<br/>
	 * Recursive Structure, can be either STRING or possibly infinite Map.{@code Map<String,Map<String,...>>}.
	 * 
	 * <p>IF .isString() -> can be treated as simple String (.toString())<br/>
	 * Otherwise normal Map access</p>
	 * 
	 * @author André Freitag
	 */
	public static class URIArgs extends HashMap<String,URIArgs> {

		/* constructors */
		/**
		 * Initializes an empty URIArgs.
		 */
		public URIArgs() {
			this("","");
		}
		/**
		 * Initializes an URIArgs with a given Argument-String.
		 * 
		 * @param argStr URI-Argument-String, like "var1=a&arr1[key]=b"
		 * @see URIArgs
		 */
		public URIArgs(String argStr) {

			Matcher argMatcher = Pattern.compile("(.*?)=(.*?)(&|$)").matcher(argStr);
			while (argMatcher.find()) {
				String name = argMatcher.group(1);
				String val = argMatcher.group(2);
				String keys = "";
				try {
					name = URLDecoder.decode(name,Charset.defaultCharset().name());
					val = URLDecoder.decode(val,Charset.defaultCharset().name());
				} catch (UnsupportedEncodingException e) {
				}

				Matcher nameMatcher = Pattern.compile("(.*?)((\\[(.*?)\\])*)$").matcher(name);
				if (nameMatcher.find()) {
					name = nameMatcher.group(1);
					keys = nameMatcher.group(2);
				}

				putMerge(name, new URIArgs(val, keys));
			}
		}
		/**
		 * internal use only
		 */
		protected URIArgs(String value, String keys) {

			if (keys.isEmpty()) {

				// string
				_str = value;

			} else {

				// assoc
				Matcher keyMatcher = Pattern.compile("\\[(.*?)\\](.*)").matcher(keys);
				if (keyMatcher.find()) {
					String head = keyMatcher.group(1);
					String tail = keyMatcher.group(2);

					putMerge(head, new URIArgs(value, tail));
				}
			}
		}


		/* public methods */
		/**
		 * checks if this URIArgs is an Associative-Array or a simple String
		 * 
		 * @return true if simple string || false
		 */
		public boolean isString() {
			return this.size()==0;
		}

		/**
		 * @return the String this argument represents
		 */
		@Override
		public String toString() {
			return _toString(false);
		}

		/**
		 * Adds the given Key/Value Pair to this map.
		 * Merges the given URIArgs if the given key already exists.
		 * 
		 * @param key
		 * @param value
		 */
		public void putMerge(String key, URIArgs value) {
			if (!this.containsKey(key) || this.get(key).isString() || value.isString()) {

				// put / replace
				this.put(key, value);

			} else {

				// merge
				for (String mKey: value.keySet()) {
					this.get(key).putMerge(mKey, value.get(mKey));
				}

			}
		}



		/* protected methods */
		/**
		 * internal use only (to encode assoc string)
		 */
		protected String _toString(boolean simpleStringEnced) {
			String str="";

			if (this.isString()) {
				str = _str;
				if (simpleStringEnced && str!=null) {
					str = "\"" + str.replaceAll("\"", "\\\"") + "\"";
				}
			} else {
				str = "{";
				for (String key: this.keySet()) {
					str += "\"" + key.replaceAll("\"", "\\\"") +"\": "
							+ this.get(key)._toString(true) + ", ";
				}
				str = str.substring(0, str.length()-2) + "}";
			}

			return str;
		}



		/* protected member */
		protected String _str=null;
		private static final long serialVersionUID = 1795029353884021087L;
	}
}