package de.tum.in.net.WSNDataFramework.Modules.IPFIX;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTMLDocument;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPRequest;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPResponse;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.WSNHTTPController;



public class HTTPController extends WSNHTTPController {

	/**
	 * index action
	 * 
	 * @param request
	 * @param response
	 */
	public void indexAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("index.html"));
		try {

			doc.addVar("wsnnodes", this.getServerModule().getNodesVar());
			doc.addHtml(this.getServerModule().getTemplate("topology.html"));
			doc.addHtml(this.getServerModule().getTemplate("node.html"));

			response.body = doc.toBytes();
		}catch(Exception e) {e.printStackTrace();}
	}


	/**
	 * ajax update action
	 * 
	 * @param request
	 * @param response
	 */
	public void updateAction(HTTPRequest request, HTTPResponse response) {
		String log=null;
		if (this.module()!=null && this.module() instanceof WSNTinyIPFIXModule) {
			WSNTinyIPFIXModule m = (WSNTinyIPFIXModule) this.module();
			log = m.getLog();
		}


		Map<String,String> jsonResult = new LinkedHashMap<String,String>();
		jsonResult.put("text", log);
		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}

}
