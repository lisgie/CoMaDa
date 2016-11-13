package de.tum.in.net.WSNDataFramework.CUSTOM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.Node.Datum.DatumID;
import de.tum.in.net.WSNDataFramework.WSNProtocol;
import de.tum.in.net.WSNDataFramework.WSNProtocolException;
import de.tum.in.net.WSNDataFramework.WSNProtocolPacket;
import de.tum.in.net.WSNDataFramework.WSNProtocolUnsupportedPacketException;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXEnrichedField;
import de.tum.in.net.WSNDataFramework.Protocols.IPFIXEnricherProtocolPacket;

public class TUMI8Adapter extends WSNProtocol {

	@Override
	public String getName() {
		return "TUM I8 WSN Adapter";
	}

	@Override
	public WSNProtocolPacket process(WSNProtocolPacket p)
			throws WSNProtocolException {
		if (!(p instanceof IPFIXEnricherProtocolPacket)) {
			throw new WSNProtocolUnsupportedPacketException();
		}

		IPFIXEnricherProtocolPacket packet = (IPFIXEnricherProtocolPacket) p;

		//System.out.println("Proto: #"+packet.getID()+" "+this.getName());

		// parse packet
		Map<String,List<IPFIXEnrichedField>> fields = packet.getFields();

		/* update WSN data if fields were received.. */
		try {
			if (fields != null) {
				Map<String,Node> nodes = new HashMap<String,Node>();

				// generate nodes, fill nodes list
				for (String nodeID: fields.keySet()) {

					// get field prefixes of aggregated nodes
					Map<String,Integer> aggregatedNodesPrefixes = new HashMap<String,Integer>();
					for (IPFIXEnrichedField enrichedField: fields.get(nodeID)) {
						if (enrichedField.type!=null && enrichedField.type.equals("NodeID") && enrichedField.value!=null && !enrichedField.value.toString().equals(nodeID)) {
							System.err.println("Prefix: " + (enrichedField.templateField.fieldID & 0xFF00) + " ("+enrichedField.templateField.fieldID+") -> " + enrichedField.value.toString());
							aggregatedNodesPrefixes.put(enrichedField.value.toString(), enrichedField.templateField.fieldID & 0xFF00);
						}
					}

					// add current node to list
					Node node = this.app().wsn().node(nodeID); // use a copy of the WSN's node if it already exists..
					if (node == null) {
						System.err.println("nodes are created before, this should not occur!");
						if (aggregatedNodesPrefixes.size() > 0) { // create new aggregated node if aggregatedNodesPrefixes is not empty
//							node = new TUMI8AggregationNode(nodeID, node.getAddress());
						} else { // otherwise create new simple node
							//node = new Node(nodeID);
						}
						this.app().wsn().addNode(node);
					} else if(aggregatedNodesPrefixes.size() > 0 && !(node instanceof TUMI8AggregationNode)) { // if node was already present in current WSN as simple node but it is actually an aggregation node, convert
						this.app().wsn().removeNode(node.getNodeID());
						node = new TUMI8AggregationNode(node);
						this.app().wsn().addNode(node);
					} else if (aggregatedNodesPrefixes.size() == 0 && !(node instanceof Node)) {
						this.app().wsn().removeNode(node.getNodeID());
						node = new Node(node);
						this.app().wsn().addNode(node);
					}
					nodes.put(nodeID, node);
					// add aggregated nodes to list
					if (aggregatedNodesPrefixes.size() > 0) {
						TUMI8AggregationNode n = (TUMI8AggregationNode) node;
						List<Node> aggregatedNodes = new ArrayList<Node>();

						for (String aggregatedNodeId: aggregatedNodesPrefixes.keySet()) {
							if (this.app().wsn().node(aggregatedNodeId) == null) { // create new one if node is not present in current WSN
								System.err.println("nodes are created before, this should not occur 2!");
								Node newNode = null;//new Node(aggregatedNodeId);
								this.app().wsn().addNode(newNode);
								nodes.put(aggregatedNodeId, newNode);
							} else { // otherwise use the WSN's one
								nodes.put(aggregatedNodeId, this.app().wsn().node(aggregatedNodeId));
							}

							aggregatedNodes.add(nodes.get(aggregatedNodeId));
						}

						n.updateAggregatedNodes(aggregatedNodes); // update the aggregator's aggregatedNodes list
					}


					// enrich created nodes with the received fields
					for (IPFIXEnrichedField enrichedField: fields.get(nodeID)) {

						Node.Datum wsnNodeField = new Node.Datum(
								enrichedField.templateField.getQualifier(),
								enrichedField.name,
								enrichedField.type,
								enrichedField.value,
								enrichedField.unit
								);

						boolean added=false;
						if (aggregatedNodesPrefixes.size() > 0) {
							for (String aggregatedNodeId: aggregatedNodesPrefixes.keySet()) {
								Integer aggregatedPrefix = aggregatedNodesPrefixes.get(aggregatedNodeId);

								if ((enrichedField.templateField.fieldID & 0xFF00) == aggregatedPrefix) { // field belongs to current aggregatedNodeId
									wsnNodeField = new Node.Datum(
											new DatumID(enrichedField.templateField.enterpriseNumber+"|"+(enrichedField.templateField.fieldID & 0x00FF)),
											enrichedField.name,
											enrichedField.type,
											enrichedField.value,
											enrichedField.unit
											);
									System.err.println("Added Datum to aggregated node #"+aggregatedNodeId+": "+wsnNodeField.getID()+ " ("+wsnNodeField.getName()+"|"+wsnNodeField.getType()+")");
									nodes.get(aggregatedNodeId).data().update(wsnNodeField);
									added=true;
								}
							}
						}

						if (!added) {
							nodes.get(nodeID).data().update(wsnNodeField);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new WSNProtocolException("TUMI8 Network Adapter error..", e);
		}

		return null;
	}

}
