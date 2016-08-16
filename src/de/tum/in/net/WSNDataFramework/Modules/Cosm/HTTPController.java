package de.tum.in.net.WSNDataFramework.Modules.Cosm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.Node.Datum;
import de.tum.in.net.WSNDataFramework.Modules.Cosm.CosmAPI.Feed;
import de.tum.in.net.WSNDataFramework.Modules.Cosm.CosmAPI.Feed.DataStream;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTMLDocument;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPRequest;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPResponse;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.WSNHTTPController;


public class HTTPController extends WSNHTTPController {

	public void indexAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("index.html"));

		response.body = doc.toBytes();
	}

	public void getvarsAction(HTTPRequest request, HTTPResponse response) {
		Map<String,Object> vars = new HashMap<String,Object>();

		vars.put("wsnnodes", this.getServerModule().getNodesVar());
		List<Map<String,Object>> jsFeeds = new ArrayList<Map<String,Object>>();
		if (this.module()!=null && this.module() instanceof WSNCosmModule) {
			WSNCosmModule m = (WSNCosmModule) this.module();

			List<Feed> feeds = m.getCosmAPI().getFeeds();
			for (Feed feed: feeds) {
				jsFeeds.add(_genFeedJs(feed));
			}

			Map<String,String> nodeAssignments=m.getAssignments();
			Map<String,String> feedAssignments=new HashMap<String,String>();
			for (String nodeID: nodeAssignments.keySet()) {
				feedAssignments.put(nodeAssignments.get(nodeID), nodeID);
			}
			vars.put("node_assignments", nodeAssignments);
			vars.put("feed_assignments", feedAssignments);
		}
		vars.put("cosm_feeds", jsFeeds);


		String jsVars="";
		try {
			jsVars = JSONValue.toJSONString(vars);
		} catch (Exception e) {
			e.printStackTrace();
		}

		response.body = jsVars.getBytes();
	}

	public void createfeedAction(HTTPRequest request, HTTPResponse response) {

		Map<String,Object> jsonResult = new LinkedHashMap<String,Object>();

		boolean success=false;
		if (this.module()!=null && this.module() instanceof WSNCosmModule) {
			WSNCosmModule m = (WSNCosmModule) this.module();

			if (request.arguments.get("title") != null) {
				try {
					String feedID = m.createFeed(request.arguments.get("title").toString());
					Feed feed = m.getCosmAPI().getFeed(feedID);

					if (feed != null) {
						jsonResult.put("feed", _genFeedJs(feed));
						success= true;
					}
				} catch (Exception e) {

				}
			}
		}

		jsonResult.put("success", success);

		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}

	public void setintervalAction(HTTPRequest request, HTTPResponse response) {
		String val = request.arguments.get("value").toString();

		if (this.module() instanceof WSNCosmModule) {
			try {
				((WSNCosmModule)this.module()).setDiagramSaveInterval(Integer.parseInt(val));
			} catch (Exception e) {

			}
		}
	}

	public void assignAction(HTTPRequest request, HTTPResponse response) {
		String nodeID = request.arguments.get("nodeid").toString();
		String feedID = request.arguments.get("feedid").toString();

		Map<String,Object> jsonResult = new LinkedHashMap<String,Object>();

		boolean success=false;
		if (this.module()!=null && this.module() instanceof WSNCosmModule) {
			WSNCosmModule m = (WSNCosmModule) this.module();

			Feed feed = m.getCosmAPI().getFeed(feedID);
			Node node = this.module().app().wsn().node(nodeID);
			if (feed != null && node != null) {

				Map<String,DataStream> streams = new HashMap<String,DataStream>();

				// delete non fitting datastreams
				for (DataStream stream: feed.streams) {
					boolean found=false;
					for (Datum f: node.data()) {
						/*if (f.name!=null && f.name.equals(stream.id)
								|| f.name==null && f.type!=null && f.type.equals(stream.id)) {*/
						if (f.getType()!=null && f.getType().equals(stream.id)) {
							found=true;
						}
					}

					if (!found) {
						m.getCosmAPI().deleteDataStream(stream.id, feed.id);
					}
					streams.put(stream.id, stream);
				}

				// create non existing datastreams
				for (Datum f: node.data()) {
					if (!streams.containsKey(f.getType())) {
						m.getCosmAPI().createDataStream(f.getType(), feed.id, f.getUnit());
					}
				}

				m.assignFeed(node.getNodeID().toString(), feed.id);
				jsonResult.put("feed", m.getCosmAPI().getFeed(feed.id));
				success=true;
			}
		}

		jsonResult.put("success", success);
		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}

	public void deletestreamAction(HTTPRequest request, HTTPResponse response) {
		String feedID = request.arguments.get("feedid").toString();
		String streamID = request.arguments.get("streamid").toString();

		Map<String,Object> jsonResult = new LinkedHashMap<String,Object>();

		boolean success=false;
		if (this.module()!=null && this.module() instanceof WSNCosmModule) {
			WSNCosmModule m = (WSNCosmModule) this.module();

			if (feedID!=null && streamID!=null) {
				m.getCosmAPI().deleteDataStream(streamID, feedID);
				success=true;
			}
		}

		jsonResult.put("success", success);
		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}

	protected Map<String,Object> _genFeedJs(Feed feed) {
		Map<String,Object> entry = new HashMap<String,Object>();
		entry.put("title", feed.title);
		entry.put("id", feed.id);
		entry.put("private", feed.isPrivate);

		List<String> streams = new ArrayList<String>();
		for (DataStream stream: feed.streams) {
			streams.add(stream.id);
		}
		entry.put("streams", streams);


		boolean isActive=false;
		if (this.module()!=null && this.module() instanceof WSNCosmModule) {
			WSNCosmModule m = (WSNCosmModule) this.module();
			isActive = m.isFeedActive(feed.id);
		}
		entry.put("active", isActive);

		return entry;
	}

}
