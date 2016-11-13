package de.tum.in.net.WSNDataFramework.Modules.HTTPServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * represents a HTTP response
 * 
 * @author André Freitag
 */
public class HTTPResponse {

	/**
	 * response code
	 */
	public int responseCode;
	/**
	 * response headers
	 */
	public Map<String,List<String>> headers;
	/**
	 * response body
	 */
	public byte[] body;
	

	/**
	 * constructor. uses 200 as responseCode, empty String as body, empty HashMap<> as headers
	 */
	public HTTPResponse() {
		this(new HashMap<String,List<String>>());
	}
	/**
	 * constructor. uses 200 as responseCode, empty String as body
	 * @param headers
	 */
	public HTTPResponse(Map<String,List<String>> headers) {
		this(new byte[0], headers);
	}
	/**
	 * constructor. uses 200 as responseCode
	 * 
	 * @param body
	 * @param headers
	 */
	public HTTPResponse(byte[] body, Map<String,List<String>> headers) {
		this(200, body, headers);
	}
	/**
	 * constructor
	 * @param responseCode
	 * @param body
	 * @param headers
	 */
	public HTTPResponse(int responseCode, byte[] body, Map<String,List<String>> headers) {
		this.responseCode = responseCode;
		this.body = body.clone();
		
		this.headers = new HashMap<String,List<String>>();
		for (Map.Entry<String,List<String>> e: headers.entrySet()) {
			this.headers.put(e.getKey(), new ArrayList<String>());
			for (String s: e.getValue()) {
				this.headers.get(e.getKey()).add(s);
			}
		}
	}
}
