package de.tum.in.net.WSNDataFramework;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * represents a WSNNode that is capable of doing aggregation.
 * 
 * @author André Freitag
 */
public class AggregationNode extends Node {

	/**
	 * a list of the Nodes this aggregation nodes aggregates.
	 */
	public List<Node> aggregatedNodes=new ArrayList<Node>();


	/**
	 * constructor.
	 * 
	 * @param nodeID
	 */
	public AggregationNode(String nodeID, InetSocketAddress address) {
		super(nodeID, address);
	}
	
//	public AggregationNode(String nodeID) {
//		super(nodeID);
//	}

	/**
	 * copy constructor.
	 * creates a deep copy (also copies its Fields).
	 * Field.value is not cloned, so be careful using mutable types as Field.values.
	 * 
	 * @param node
	 * @throws CloneNotSupportedException
	 */
	public AggregationNode(Node node) throws CloneNotSupportedException {
		super(node);
	}
	/**
	 * copy constructor.
	 * creates a deep copy (also copies its Fields + aggregatedNodes).
	 * Field.value is not cloned, so be careful using mutable types as Field.values.
	 * 
	 * @param node
	 * @throws CloneNotSupportedException if node couldn't be cloned correctly
	 */
	public AggregationNode(AggregationNode node) throws CloneNotSupportedException {
		super(node);
		for (Node aggregatedNode: node.aggregatedNodes) {
			this.aggregatedNodes.add(aggregatedNode);
		}
	}


	/**
	 * Updates a single aggregated node. Adds it or replaces it if already present with the given node and updates WSNNode.updatedAt.
	 * 
	 * @param aggregatedNode
	 * @return this for fluent interface
	 */
	public AggregationNode updateAggregatedNode(Node aggregatedNode) {
		for (Node node: this.aggregatedNodes) { // check if an node with the same nodeID is already present and remove it if so..
			if (aggregatedNode.getNodeID()==null && node.getNodeID()==null || aggregatedNode.getNodeID()!=null && aggregatedNode.getNodeID().equals(node.getNodeID())) {
				this.aggregatedNodes.remove(node);
				break;
			}
		}
		this.aggregatedNodes.add(aggregatedNode);
		_update();

		return this;
	}
	/**
	 * replaces this node's aggregatedNodes by given list and updates WSNNode.updatedAt.
	 * 
	 * @param aggregatedNodes
	 * @throws CloneNotSupportedException if aggregatedNodes couldn't be cloned correctly
	 * @return this for fluent interface
	 */
	public AggregationNode updateAggregatedNodes(List<Node> aggregatedNodes) throws CloneNotSupportedException {
		this.aggregatedNodes = new ArrayList<Node>();

		for (Node aggregatedNode: aggregatedNodes) {
			if (aggregatedNode==null) continue;
			this.aggregatedNodes.add(aggregatedNode);
		}
		_update();

		return this;
	}


	/**
	 * Determines whether two nodes may be considered the same.
	 * Compares WSNNode.same() style and checks additionally if aggregatedNodes are the same.
	 */
	@Override
	public boolean same(Node node) {
		if (node==null) return false;

		boolean same = super.same(node); // check if they are the same on WSNNode level

		if (same) {

			// check if they aggregate the same nodes
			if (node instanceof AggregationNode) {
				List<Node> tempNodes=new ArrayList<Node>();
				tempNodes.addAll(((AggregationNode)node).aggregatedNodes);
				same = same && tempNodes.size()==this.aggregatedNodes.size();

				if (same) {
					for (Node anode: this.aggregatedNodes) {
						for (int i=0; i<tempNodes.size(); i++) {
							if (tempNodes.get(i).same(anode)) {
								tempNodes.remove(i);
								break;
							}
						}
					}

					same = same && tempNodes.size()==0;
				}
			}

		}

		return same;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj==null) return false;

		boolean equals = super.equals(obj);

		if (equals) {
			// check if they aggregate the same nodes
			if (obj instanceof AggregationNode) {
				List<Node> tempNodes=new ArrayList<Node>();
				tempNodes.addAll(((AggregationNode)obj).aggregatedNodes);
				equals = equals && tempNodes.size()==this.aggregatedNodes.size();

				if (equals) {
					for (Node anode: this.aggregatedNodes) {
						for (int i=0; i<tempNodes.size(); i++) {
							if (tempNodes.get(i).equals(anode)) {
								tempNodes.remove(i);
								break;
							}
						}
					}

					equals = equals && tempNodes.size()==0;
				}
			}
		}

		return equals;
	}


	private static final long serialVersionUID = -1385413670086366672L;
}
