package de.tum.in.net.WSNDataFramework.Modules.Locating;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTMLDocument;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPRequest;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPResponse;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.WSNHTTPController;
import de.tum.in.net.WSNDataFramework.Modules.Locating.WSNLocatingModule.Location;



public class HTTPController extends WSNHTTPController {

	public void indexAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("index.html"));


		doc.addVar("wsnnodes", this.getServerModule().getNodesVar());
		doc.addHtml(this.getServerModule().getTemplate("node.html"));

		if (this.module()!=null && this.module() instanceof WSNLocatingModule) {
			WSNLocatingModule m = (WSNLocatingModule) this.module();

			Map<String,List<Double>> jsLocations = new HashMap<String,List<Double>>();

			Map<String,Location> locations = m.getLocations();
			for (String nodeID: locations.keySet()) {
				Location loc = locations.get(nodeID);
				jsLocations.put(nodeID, Arrays.asList(new Double[]{loc.x, loc.y}));
			}

			doc.addVar("locations", jsLocations);
		}

		response.body = doc.toBytes();
	}

	public void setlocationAction(HTTPRequest request, HTTPResponse response) {
		String nodeID = request.arguments.get("nodeid").toString();
		String left = request.arguments.get("left").toString();
		String top = request.arguments.get("top").toString();

		if (this.module()!=null && this.module() instanceof WSNLocatingModule) {
			try {
				WSNLocatingModule m = (WSNLocatingModule) this.module();
				m.setLocation(nodeID, Double.parseDouble(left), Double.parseDouble(top));
			}catch (Exception e) {e.printStackTrace();}
		}
	}

	public void uploadAction(HTTPRequest request, HTTPResponse response) {
		System.out.println(request.arguments.size());

		for (String s: request.arguments.keySet()) {
			System.out.println(s);
			System.out.println(request.arguments.get(s));
		}
	}
}
