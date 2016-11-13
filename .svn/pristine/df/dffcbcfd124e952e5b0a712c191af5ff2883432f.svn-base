package de.tum.in.net.WSNDataFramework;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.tum.in.net.WSNDataFramework.Node.ActionNotImplementedException;
import de.tum.in.net.WSNDataFramework.Node.NodeID;
import de.tum.in.net.WSNDataFramework.NodeAction.NodeAction;

public class NodeCollection implements Map<NodeID, Node> {

	/* constructors */
	/**
	 * creates an empty NodeCollection
	 */
	public NodeCollection() {
		_collection = new HashMap<NodeID, Node>();
	}
	/**
	 * uses the given collection as underlying collection
	 */
	public NodeCollection(HashMap<NodeID, Node> collection) {
		_collection = collection;
	}


	/* filter interface */
	/**
	 * Filters this NodeCollection by a given tag.
	 * 
	 * @param tag
	 * @return new NodeCollection only containing the nodes of the original collection matching the filtering criterion
	 */
	public NodeCollection filterByTag(String tag) {
		NodeCollection newCollection = new NodeCollection();

		for (Node node: this.values()) {
			if (node.tags().contains(tag)) {
				newCollection.put(node.getNodeID(), node);
			}
		}

		return newCollection;
	}
	/**
	 * Filters this NodeCollection by the given metadata.
	 * 
	 * @param tag
	 * @return new NodeCollection only containing the nodes of the original collection matching the filtering criterion
	 */
	public NodeCollection filterByMetadata(String key, Object val) {
		NodeCollection newCollection = new NodeCollection();

		for (Node node: this.values()) {
			Object data = node.getMetadata(key, val.getClass());
			if (data == null ? val == null : data.equals(val)) {
				newCollection.put(node.getNodeID(), node);
			}
		}

		return newCollection;
	}
	/**
	 * Filters this NodeCollection by removing all nodes the given fromNode has no connection to (unidirectional from fromNode to the questioned node).
	 * 
	 * @param tag
	 * @return new NodeCollection only containing the nodes of the original collection matching the filtering criterion
	 */
	public NodeCollection filterByOutgoingLinks(Node fromNode) {
		NodeCollection newCollection = new NodeCollection();

		if (fromNode != null && this.containsValue(fromNode)) {
			for (Topology.Link link: fromNode.wsn().topology().getLinks(fromNode.getNodeID())) {
				if (link.isSource(fromNode) && this.containsKey(link.getTarget())) {
					newCollection.put(link.getTarget(), this.get(link.getTarget()));
				}
			}
		}

		return newCollection;
	}
	/**
	 * Filters this NodeCollection by removing all nodes that have no connection to the given toNode.
	 * 
	 * @param tag
	 * @return new NodeCollection only containing the nodes of the original collection matching the filtering criterion
	 */
	public NodeCollection filterByIncomingLinks(Node toNode) {
		NodeCollection newCollection = new NodeCollection();

		if (toNode != null && this.containsValue(toNode)) {
			for (Topology.Link link: toNode.wsn().topology().getLinks(toNode.getNodeID())) {
				if (link.isTarget(toNode) && this.containsKey(link.getSource())) {
					newCollection.put(link.getSource(), this.get(link.getSource()));
				}
			}
		}

		return newCollection;
	}
	/**
	 * Filters this NodeCollection by removing all nodes that have no connection to the given toNode (bidrectional, resulting collection only contains the given node if such a connection is present in the WSN's topology).
	 * 
	 * @param tag
	 * @return new NodeCollection only containing the nodes of the original collection matching the filtering criterion
	 */
	public NodeCollection filterByLinks(Node node) {
		NodeCollection newCollection = new NodeCollection();

		if (node != null && this.containsValue(node)) {
			for (Topology.Link link: node.wsn().topology().getLinks(node.getNodeID())) {
				if (link.isSource(node) && this.containsKey(link.getTarget())) {
					newCollection.put(link.getTarget(), this.get(link.getTarget()));
				}
				if (link.isTarget(node) && this.containsKey(link.getSource())) {
					newCollection.put(link.getSource(), this.get(link.getSource()));
				}
			}
		}

		return newCollection;
	}


	/* grouped node interaction interface */
	/**
	 * Passes the given action to each Node of this collection.
	 * Unlike {@link Node.performAction}, no {@link ActionNotImplementedException} is thrown but is internally catched and ignored.
	 * 
	 * @param action
	 */
	public void performAction(NodeAction action) {
		for (Node node: this.values()) {
			try {
				node.performAction(action);
			} catch (ActionNotImplementedException e) {
				// ignore
			}
		}
	}

	/* Map interface (maps directly to the functions of this._collection) */
	@Override
	public void clear() {
		_collection.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return _collection.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return _collection.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<NodeID, Node>> entrySet() {
		return _collection.entrySet();
	}

	@Override
	public Node get(Object key) {
		return _collection.get(key);
	}

	@Override
	public boolean isEmpty() {
		return _collection.isEmpty();
	}

	@Override
	public Set<NodeID> keySet() {
		return _collection.keySet();
	}

	@Override
	public Node put(NodeID key, Node value) {
		return _collection.put(key, value);
	}

	@Override
	public void putAll(Map<? extends NodeID, ? extends Node> m) {
		_collection.putAll(m);
	}

	@Override
	public Node remove(Object key) {
		return _collection.remove(key);
	}

	@Override
	public int size() {
		return _collection.size();
	}

	@Override
	public Collection<Node> values() {
		return _collection.values();
	}

	/* protected member */
	protected Map<NodeID, Node> _collection;
}
