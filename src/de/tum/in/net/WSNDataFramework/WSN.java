package de.tum.in.net.WSNDataFramework;

import java.util.HashMap;

import de.tum.in.net.WSNDataFramework.Node.NodeID;
import de.tum.in.net.WSNDataFramework.Events.WSNNodeAddedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNNodeRemovedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNTopologyUpdatedEvent;

public class WSN {

	/* constructors */
	/**
	 * default constructor
	 */
	public WSN() {

	}
	/**
	 * @param app the {@link WSNApp} this WSN belongs to
	 */
	public WSN(WSNApp app) {
		_app = app;
	}



	/* public getters */
	/**
	 * Returns the {@link WSNApp} this {@link WSN} belongs to.
	 * 
	 * @return {@link WSNApp} || NULL
	 */
	public WSNApp app() {
		return _app;
	}

	/**
	 * Returns the {@link Node} identified by the given {@link NodeID}.<br/>
	 * The returned object is the actual object representing the WSNNode within the WSN => modifying it will modify the WSN itself.
	 * 
	 * @param NodeID {@link NodeID} of the WSNNode to be retrieved
	 * @return the WSNNode identified by 'NodeID' || NULL
	 */
	public Node node(NodeID NodeID) {
		return _nodes.get(NodeID);
	}
	/**
	 * Calls {@link #node(NodeID)} with the given 'NodeID' converted into an {@link NodeID} object
	 * 
	 * @see WSN#node(NodeID)
	 * @param NodeID String representing the {@link NodeID} of the node to be retrieved
	 * @return the WSNNode identified by 'NodeID' || NULL
	 */
	public Node node(String NodeID) {
		return this.node(new NodeID(NodeID));
	}

	/**
	 * Returns all WSNNodes assigned to this WSN.<br/>
	 * Changes to the Collection will not affect the WSN itself (add/remove WSNNodes via {@link #addNode(Node)} {@link #removeNode(NodeID)}).<br/>
	 * The {@link Node} objects of this map are the actual objects representing this WSN's WSNNodes => modifying them will modify the WSN itself.
	 * 
	 * @return Map(NodeID => WSNNode)
	 */
	public NodeCollection nodes() {
		return new NodeCollection(_nodes);
		//return Collections.unmodifiableMap(_nodes);
	}

	/**
	 * Adds the given {@link Node} to the WSN.
	 * 
	 * @param Node {@link Node} to be added
	 * @return this for fluent interface
	 */
	public WSN addNode(Node node) {
		if (node != null) {
			// set the node's WSN=this
			node.setWSN(this);

			// apply needed modifications
			_applyNodeModifications(node);

			// add the node
			_nodes.put(node.getNodeID(), node);

			// fire event
			if (this.app() != null) {
				this.app().fireEvent(new WSNNodeAddedEvent(node));
			}
		}

		return this;
	}
	/**
	 * Removes the node with the given {@link NodeID} from the WSN.
	 * 
	 * @param NodeID {@link NodeID} identifying the node to be removed
	 * @return this for fluent interface
	 */
	public WSN removeNode(NodeID nodeID) {
		if (_nodes.containsKey(nodeID)) {
			// set the node's WSN=null
			_nodes.get(nodeID).setWSN(null);

			// remove the node
			_nodes.remove(nodeID);

			// fire event
			if (this.app() != null) {
				this.app().fireEvent(new WSNNodeRemovedEvent(nodeID));
			}
		}
		return this;
	}
	/**
	 * Calls {@link #removeNode(NodeID)} with the given 'NodeID' converted into an {@link NodeID} object
	 * 
	 * @see {@link #removeNode(NodeID)}
	 * @param NodeID String representing the {@link NodeID} of the node to be removed
	 * @return this for fluent interface
	 */
	public WSN removeNode(String NodeID) {
		this.removeNode(new NodeID(NodeID));
		return this;
	}

	/**
	 * Returns the {@link Topology} of the WSN's nodes.
	 * 
	 * @return The {@link Topology} of the WSN's nodes.
	 */
	public Topology topology() {
		return _topology;
	}



	/* public modifiers */
	/**
	 * Replaces this WSN's {@link Topology}.<br/>
	 * If NULL given, a freshly created {@link Topology} object is used.
	 * 
	 * @param topology The {@link Topology} the WSN's topology should be replaced with.
	 * @return this for fluent interface
	 */
	public WSN replaceTopology(Topology topology) {
		_topology = topology!=null ? topology : new Topology();
		if (this.app() != null) {
			this.app().fireEvent(new WSNTopologyUpdatedEvent(_topology));
		}

		return this;
	}
	/**
	 * Resets the WSN.<br/>
	 * Removes all nodes and clears the topology.
	 * 
	 * @return this for fluent interface
	 */
	public WSN reset() {
		_nodes.clear();
		_topology = new Topology();

		return this;
	}



	/* protected methods */
	/**
	 * apply needed modifications to the given node before adding it
	 * @param node
	 */
	protected void _applyNodeModifications(Node node) {
		if (_app != null) {
			_app.applyUnitTranslations(node);
		}
	}



	/* protected member */
	/** WSN's WSNNodes */
	protected HashMap<NodeID,Node> _nodes = new HashMap<NodeID,Node>();
	/** WSN's topology */
	protected Topology _topology = new Topology();
	/** the {@link WSNApp} this WSN belongs to */
	protected WSNApp _app = null;
}
