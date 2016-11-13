package de.tum.in.net.WSNDataFramework.Modules.Web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;

import de.tum.in.net.WSNDataFramework.AggregationNode;
import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.Node.Datum;
import de.tum.in.net.WSNDataFramework.Node.Datum.DatumID;
import de.tum.in.net.WSNDataFramework.Node.NodeID;
import de.tum.in.net.WSNDataFramework.Topology;
import de.tum.in.net.WSNDataFramework.Topology.Link;
import de.tum.in.net.WSNDataFramework.Modules.Cosm.CosmAPI.Feed;
import de.tum.in.net.WSNDataFramework.Modules.Cosm.CosmAPI.Feed.DataStream;

public class WSNDataUploader {
	
	private Topology _previousTopology = null;
	private ArrayList<PastNodeState> _previousNodes = new ArrayList<PastNodeState>();
	private Map<String,Set<String>> _previousAggragators = new HashMap<String, Set<String>>();
	private int _topologyCounter = 0;
	private Map<String,List<String>> _streams = new HashMap<String, List<String>>();
	private String _webURL = ""; //will be read from Config.java when inizialized
	private String _username;
	private String _password;
	
	public WSNDataUploader(String username, String password) {
		_webURL = Config._webURL;
		_username = username;
		_password = password;
	}
	
	//constructor for JUnit-Test
	public WSNDataUploader(String webURL) {
		_webURL = webURL;
	}
	
	public void updateTopology(Topology topology, String dblink) {
		//check if we got first topology or if topology changed since last time
		if(_previousTopology == null) {
			Set<Link> newLinks = topology.getLinks();
			if(!newLinks.isEmpty()) {
				sendTopologyUpdate(newLinks, dblink, "add");
			}
			try {
				_previousTopology = new Topology(topology);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		else if(!topology.equals(_previousTopology)) {
			if(topology.getLinks().isEmpty()) {
				if(_topologyCounter < 1) {
					_topologyCounter++;
					return;
				}
			}else{
				_topologyCounter=0;
			}
			//topology has changed -> need to find newly added and/or deleted links
			Set<Link> oldLinks = _previousTopology.getLinks();
			Set<Link> newLinks = topology.getLinks();
			Set<Link> addedLinks = new HashSet<Link>();
			Set<Link> removedLinks = new HashSet<Link>();
			//find links that have been added to previous topology (if any)
			for(Link nlink : newLinks) {
				if(!oldLinks.contains(nlink)) {
					addedLinks.add(nlink);
				}
			}
			//find links that have been removed from previous topology (if any)
			for(Link olink : oldLinks) {
				if(!newLinks.contains(olink)) {
					removedLinks.add(olink);
				}
			}
			//send that info to the Web-Interface
			if(!addedLinks.isEmpty()) {
				sendTopologyUpdate(addedLinks, dblink, "add");
			}
			if(!removedLinks.isEmpty()) {
				sendTopologyUpdate(removedLinks, dblink, "remove");
			}
			//save the new topology 
			try {
				_previousTopology = new Topology(topology);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		} else {
			_topologyCounter=0;
		}
	}
	
	public void updateAggregator(AggregationNode node, String _link) {
		if(_previousAggragators.containsKey(node.getNodeID().toString())) {
			Set<String> oldIds = _previousAggragators.get(node.getNodeID().toString());
			Set<String> newIds = new HashSet<String>();
			String valuesStr = "";
			for(Node tmpnode : node.aggregatedNodes) {
				newIds.add(tmpnode.getNodeID().toString());
				valuesStr = valuesStr + tmpnode.getNodeID().toString() + ", ";
			}
			boolean aggregationChanged = !(oldIds.equals(newIds));
			if(aggregationChanged) {
				valuesStr = valuesStr.substring(0, valuesStr.length() - 2);
				DatumID ID = new DatumID("1");
				Datum datum = new Datum(ID, "Aggregating", valuesStr, " ");
				updateDatum(datum, node.getNodeID(), _link, false, false, true, false);
				_previousAggragators.remove(node.getNodeID().toString());
				_previousAggragators.put(node.getNodeID().toString(), newIds);
			}
		} else {
			String valuesStr = "";
			Set<String> valuesList = new HashSet<String>();
			for(Node agrNode : node.aggregatedNodes) {
				valuesStr = valuesStr + agrNode.getNodeID().toString() + ", ";
				valuesList.add(agrNode.getNodeID().toString());
			}
			valuesStr = valuesStr.substring(0, valuesStr.length() - 2);
			DatumID ID = new DatumID("1");
			Datum datum = new Datum(ID, "Aggregating", valuesStr, " ");
			addNewDatum(datum, node.getNodeID(), _link);
			_previousAggragators.put(node.getNodeID().toString(), valuesList);
		}
	}
	
	public void updateNode(Node node, String dblink) {
		//check if this node is new or not
		boolean seenBefore = false;
		PastNodeState oldNode = null;
		for(PastNodeState prevNode : _previousNodes) {
			if(node.getNodeID().equals(prevNode.getNodeID())) {
				seenBefore = true;
				oldNode = prevNode;
				break;
			}
		}
		if(seenBefore) {
			//node is not new -> find the field(s) of the node that changed and UPDATE those
			//also check if new fields have been added or removed
			PastNodeState nodeState = new PastNodeState(node.getNodeID().toString());
			for(Datum ndata : node.data()) {
				boolean found = false;
				PastDatumState tmpData = null;

				for(String pastDatumID : oldNode.getDatumIDs()) {
					if(ndata.getID().equals(pastDatumID)){
						found = true;
						tmpData = oldNode.getDatum(pastDatumID);
						break;
					}
				}
				if(found) {
					//the datum ndata was already in the old node state (identified over same ID)
					//check if something changed
					boolean newName = false;
					boolean newType = false;
					boolean newValue = false;
					boolean newUnit = false;
					if(ndata.getName() != null) {
						newName =  !(ndata.getName().equals(tmpData.getName()));
					} else {
						newName = (tmpData.getName() != null);
					}
					if(ndata.getType() != null) {						
						newType = !(ndata.getType().equals(tmpData.getType()));
					} else {
						newType = (tmpData.getType() != null);
					}
					if(ndata.getValue() != null) {
						newValue = !(ndata.getValue().toString().equals(tmpData.getValue()));
					} else {
						newValue = (tmpData.getValue() != null);
					}
					if(ndata.getUnit() != null) {
						newUnit = !(ndata.getUnit().equals(tmpData.getUnit()));
					} else {
						newUnit = (tmpData.getUnit() != null);
					}
						
					if(newName || newType || newValue || newUnit) {
						//at least one datum value changed -> send to update
						updateDatum(ndata, node.getNodeID(), dblink, newName, newType, newValue, newUnit);
						nodeState.addDatum(ndata.getID().toString(), ndata.getName(), ndata.getType(), ndata.getValue().toString(), ndata.getUnit());
					}
					else {
						nodeState.addDatum(ndata.getID().toString(), ndata.getName(), ndata.getType(), ndata.getValue().toString(), ndata.getUnit());
					}
				}
				else {
					//the datum ndata was not in the node before
					//add the node data
					addNewDatum(ndata, node.getNodeID(), dblink);
					nodeState.addDatum(ndata.getID().toString(), ndata.getName(), ndata.getType(), ndata.getValue().toString(), ndata.getUnit());
				}
			}
			//datum objects that are in node but were not in the last state of node have been added
			//datum objects that are in both old and new state have been updated if a value changed
			//-> also need to remove datum objects that were in last node state but are not anymore
			for(String oldDatumID : oldNode.getDatumIDs()) {
				boolean found = false;
				
				for(Datum nData : node.data()) {
					if(nData.getID().equals(oldDatumID)) {
						found = true;
						break;
					}
				}
				
				if(!found) {
					//datum found that existed in previous node state but no longer in the new node
					removePastDatum(oldNode.getDatum(oldDatumID), oldNode.getNodeID(), dblink);
				}
			}
			//_prevoiusNodes must be updated by adding new node and removing the old one
			removeFromPreviousNodes(oldNode.getNodeID());
			_previousNodes.add(nodeState);
		}
		else {
			//this is a new node -> iterate over all his data and ADD them			
			PastNodeState nodeState = new PastNodeState(node.getNodeID().toString());
			for(Datum datum : node.data()) {
				nodeState.addDatum(datum.getID().toString(), datum.getName(), datum.getType(), datum.getValue().toString(), datum.getUnit());
				addNewDatum(datum, node.getNodeID(), dblink);
			}
			//_prevoiusNodes must be updated by adding new node
			_previousNodes.add(nodeState);
		}
		//node update is finished
	}
	
	public void updateProtocol(String protocol, String dblink) {
		sendProtocalUpdate(protocol, dblink);
	}
	
	public void updateDatastream(Feed feed, String _link) {
		synchronized(_streams) {
			if(feed.streams.isEmpty()) {
				//this feed does not have any datastream (any more)
				//-> check if feed is in db, and if so remove
				if(_streams.containsKey(feed.id)){
					for(String streamID : _streams.get(feed.id)) {
						removeDatastreams(feed.id, feed.title, streamID, _link);
					}
					_streams.remove(feed.id);
				}
			} else {
				//this feed has datastream(s)
				//-> check if this feed is already known or if it appeared for the first time
				List<String> oldFeedStreams = _streams.get(feed.id);
				if(oldFeedStreams == null) {
					//this is the first time this feed has appeared
					//-> make new entry in _streams and add all his streams to DB
					List<String> theStreams = new ArrayList<String>();
					for(DataStream stream : feed.streams) {
						addDatastreams(feed.id, feed.title, stream.id, _link);
						theStreams.add(stream.id);
					}
					_streams.put(feed.id, theStreams);
				} else {
					//this feed has been saved in previous updates
					//-> check for all streams if they are already in DB, and add if not
					for(DataStream stream : feed.streams) {
						boolean found = false;
						for(String oldStreamID : oldFeedStreams) {
							if(stream.id.equals(oldStreamID)) {
								found = true;
								break;
							}
						}
						if(!found) {
							addDatastreams(feed.id, feed.title, stream.id, _link);
							_streams.get(feed.id).add(stream.id);
						}
					}
					//-> also check if there are streams in db which are not in feed any more, and if so remove
					List<String> toRemove = new ArrayList<String>();
					for(String oldStreamID : oldFeedStreams) {
						boolean found = false;
						for(DataStream stream : feed.streams) {
							if(oldStreamID.equals(stream.id)){
								found = true;
								break;
							}
						}
						if(!found) {
							removeDatastreams(feed.id, feed.title, oldStreamID, _link);
							//_streams.get(feed.id).remove(oldStreamID);
							toRemove.add(oldStreamID);
						}
					}
					_streams.get(feed.id).removeAll(toRemove);
				}
			}
		}
	}

	private void removeFromPreviousNodes(String nodeID) {
		for(PastNodeState state : _previousNodes) {
			if(state.getNodeID().equals(nodeID)) {
				if(!(_previousNodes.remove(state))) {
					System.out.println("ERROR - PREVIOUS NODE STATE COULD NOT BE REMOVED");;
				}
				return;
			}
		}
	}

	private void removePastDatum(PastDatumState datum, String nodeID_, String dblink) {
		String nodeID = nodeID_;
		String datumID = datum.getID();
		String name = "none";
		String type = "none";
		String unit = "none";
		String value = "none";
		if(datum.getName() != null) {
			name = datum.getName();
		}
		if(datum.getType() != null) {
			type = datum.getType();
		}
		if(datum.getUnit() != null) {
			unit = datum.getUnit();
		}
		if(datum.getValue() != null) {
		 value = datum.getValue().toString();
		}
		List<NameValuePair> params = new ArrayList<NameValuePair>(8);
		params.add(new BasicNameValuePair("task", "remove"));
		params.add(new BasicNameValuePair("dblink", dblink));
		params.add(new BasicNameValuePair("nodeID", nodeID));
		params.add(new BasicNameValuePair("datumID", datumID));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("type", type));
		params.add(new BasicNameValuePair("unit", unit));
		params.add(new BasicNameValuePair("value", value));
		params.add(new BasicNameValuePair("username", _username));
		params.add(new BasicNameValuePair("password", _password));
		
		sendDatumUpdate(params);
	}

	private void updateDatum(Datum datum, NodeID nodeID_, String dblink, boolean newName, boolean newType, boolean newValue, boolean newUnit) {
		String nodeID = nodeID_.toString();
		String datumID = datum.getID().toString();
		String name = "sameAsBefore";
		String type = "sameAsBefore";
		String value = "sameAsBefore";
		String unit = "sameAsBefore";
		if(newName) {
			if(datum.getName() != null) {
				name = datum.getName();
			} else {
				name = "none";
			}
		}
		if(newType) {
			if(datum.getType() != null) {
				type = datum.getType();
			} else {
				type = "none";
			}
		}
		if(newValue) {
			if(datum.getValue() != null) {
				value = datum.getValue().toString();
			} else {
				value = "none";
			}
		}
		if(newUnit) {
			if(datum.getUnit() != null) {
				unit = datum.getUnit();
			} else {
				unit = "none";
			}
		}
		List<NameValuePair> params = new ArrayList<NameValuePair>(8);
		params.add(new BasicNameValuePair("task", "update"));
		params.add(new BasicNameValuePair("dblink", dblink));
		params.add(new BasicNameValuePair("nodeID", nodeID));
		params.add(new BasicNameValuePair("datumID", datumID));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("type", type));
		params.add(new BasicNameValuePair("unit", unit));
		params.add(new BasicNameValuePair("value", value));
		params.add(new BasicNameValuePair("username", _username));
		params.add(new BasicNameValuePair("password", _password));
		
		sendDatumUpdate(params);
	}
	
	private void addNewDatum(Datum datum, NodeID nodeID_, String dblink) {
		String nodeID = nodeID_.toString();
		String datumID = datum.getID().toString();
		String name = "none";
		String type = "none";
		String unit = "none";
		String value = "none";
		if(datum.getName() != null) {
			name = datum.getName();
		}
		if(datum.getType() != null) {
			type = datum.getType();
		}
		if(datum.getUnit() != null) {
			unit = datum.getUnit();
		}
		if(datum.getValue() != null) {
		 value = datum.getValue().toString();
		}
		List<NameValuePair> params = new ArrayList<NameValuePair>(8);
		params.add(new BasicNameValuePair("task", "add"));
		params.add(new BasicNameValuePair("dblink", dblink));
		params.add(new BasicNameValuePair("nodeID", nodeID));
		params.add(new BasicNameValuePair("datumID", datumID));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("type", type));
		params.add(new BasicNameValuePair("unit", unit));
		params.add(new BasicNameValuePair("value", value));
		params.add(new BasicNameValuePair("username", _username));
		params.add(new BasicNameValuePair("password", _password));
		
		sendDatumUpdate(params);
	}
	
	private void sendDatumUpdate(List<NameValuePair> params) {
		try {
			HttpPost _topologyPost = new HttpPost(_webURL + "/updateNode.php");				
			CloseableHttpClient _client = HttpClients.createDefault();
			_topologyPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			_client.execute(_topologyPost);
			_client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendTopologyUpdate(Set<Link> updatedLinks, String dblink, String action) {
		for(Link link : updatedLinks) {
			String source = "none";
			String target = "none";
			String weight = "none";
			if(link.getSource()!=null) {
				source = link.getSource().toString();
			}
			if(link.getTarget()!=null) {
				target = link.getTarget().toString();
			}
			if(link.getWeight()!=null) {
				weight = String.valueOf(link.getWeight());
			}
			List<NameValuePair> params = new ArrayList<NameValuePair>(5);
			params.add(new BasicNameValuePair("task", action));
			params.add(new BasicNameValuePair("dblink", dblink));
			params.add(new BasicNameValuePair("source", source));
			params.add(new BasicNameValuePair("target", target));
			params.add(new BasicNameValuePair("weight", weight));
			params.add(new BasicNameValuePair("username", _username));
			params.add(new BasicNameValuePair("password", _password));
			try {
				HttpPost _topologyPost = new HttpPost(_webURL + "/updateTopology.php");				
				CloseableHttpClient _client = HttpClients.createDefault();
				_topologyPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
				_client.execute(_topologyPost);
				_client.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendProtocalUpdate(String protocol, String dblink) {
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("protocol", protocol));
		params.add(new BasicNameValuePair("dblink", dblink));
		params.add(new BasicNameValuePair("username", _username));
		params.add(new BasicNameValuePair("password", _password));
		try {
			HttpPost _topologyPost = new HttpPost(_webURL + "/updateProtocol.php");				
			CloseableHttpClient _client = HttpClients.createDefault();
			_topologyPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			_client.execute(_topologyPost);
			_client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addDatastreams(String feedID, String feedName, String streamID, String dblink) {
		sendDatastreamUpdate(feedID, feedName, streamID, dblink, "add");
	}
	
	private void removeDatastreams(String feedID, String feedName, String streamID, String dblink) {
		sendDatastreamUpdate(feedID, feedName, streamID, dblink, "remove");
	}
	
	private void sendDatastreamUpdate(String feedID, String feedName, String streamID, String dblink, String action) {
		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		params.add(new BasicNameValuePair("feedID", feedID));
		params.add(new BasicNameValuePair("feedName", feedName));
		params.add(new BasicNameValuePair("streamID", streamID));
		params.add(new BasicNameValuePair("dblink", dblink));
		params.add(new BasicNameValuePair("action", action));
		params.add(new BasicNameValuePair("username", _username));
		params.add(new BasicNameValuePair("password", _password));
		try {
			HttpPost _topologyPost = new HttpPost(_webURL + "/updateDatastream.php");				
			CloseableHttpClient _client = HttpClients.createDefault();
			_topologyPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			_client.execute(_topologyPost);
			_client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
