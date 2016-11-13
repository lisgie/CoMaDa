package de.tum.in.net.WSNDataFramework;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tum.in.net.WSNDataFramework.Node.NodeID;
import de.tum.in.net.WSNDataFramework.Events.WSNTopologyUpdatedEvent;

public class Topology implements Cloneable, Serializable {

	/* constructors */
	/**
	 * default constructor.
	 */
	public Topology() {
	}
	/**
	 * copy constructor.
	 * Copies all links from topology and adds them to the new one.
	 * 
	 * @param topology
	 * @throws CloneNotSupportedException if topology couldn't be cloned correctly
	 */
	public Topology(Topology topology) throws CloneNotSupportedException {
		this();

		if (topology != null) {
			synchronized (_links) {
				Set<Link> links = topology.getLinks();

				synchronized (links) {
					for (Link link: links) {
						_links.add(link.clone());
					}
				}
			}
		}
	}


	/* public methods */
	/**
	 * {@link WSN} this topology belongs to.
	 * 
	 * @return {@link WSN} instance this topology belongs to || NULL
	 */
	public WSN wsn() {
		return _wsn;
	}
	/**
	 * sets the {@link WSN} this topology belongs to.
	 * 
	 * @param wsn
	 * @return this for fluent interface
	 */
	Topology setWSN(WSN wsn) {
		_wsn = wsn;
		return this;
	}

	/**
	 * Returns the {@link Date} this topology was updated the last time.<br/>
	 * Returns NULL if this topology was never updated/if it was freshly created.<br/>
	 * Not directly modifiable! Gets implicitly updated when calling one of the setters "add*()/remove*()".
	 * 
	 * @return {@link Date} this node was updated || NULL
	 */
	public Date getUpdatedAt() {
		return (Date)_updatedAt.clone();
	}
	/**
	 * gets all the links of this topology.
	 * 
	 * @return immutable set of all links of this topology
	 */
	public Set<Link> getLinks() {
		return Collections.unmodifiableSet(_links);
	}

	/**
	 * gets all the links that include the given nodeID.
	 * 
	 * @param nodeID
	 */
	public Set<Link> getLinks(NodeID nodeID) {
		synchronized (_links) {
			Set<Link> set = new HashSet<Link>();

			for (Link link: _links) {
				if (link.involves(nodeID)) {
					set.add(link);
				}
			}

			return set;
		}
	}

	/**
	 * adds a link to the topology.
	 */
	public Topology addLink(Link link) {
		if (link != null) {
			synchronized (_links) {
				_links.add(link);
				_update();
			}
		}
		return this;
	}
	/**
	 * remove a link to the topology.
	 */
	public Topology removeLink(Link link) {
		if (link != null) {
			synchronized (_links) {
				_links.remove(link);
				_update();
			}
		}
		return this;
	}


	/**
	 * Determines whether two WSNTopology instances may represent the same topology.
	 * Currently returns this.equals(topology) but may be overridden!
	 */
	public boolean same(Topology topology) {
		if (topology==null) return false;

		boolean same=true;
		same = same && this.getClass().equals(topology.getClass());

		synchronized (_links) {
			// compare data
			Set<Link> tempSet=new HashSet<Link>();
			List<Link> tempData=new ArrayList<Link>();
			for (Link link: tempSet) {
				tempData.add(link);
			}
			tempData.addAll(topology.getLinks());
			same = same && tempData.size()==_links.size();

			if (same) {
				for (Link link: _links) {
					for (int i=0; i<tempData.size(); i++) {

						if (tempData.get(i).same(link)) {
							tempData.remove(i);
							break;
						}
					}
				}

				same = same && tempData.size()==0;
			}
		}

		return same;
	}

	/**
	 * override Object.equals.
	 * Returns true if obj is a WSNTopology and contains the same links as this WSNTopology.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj==null || !this.getClass().equals(obj.getClass())) return false;

		if (obj instanceof Topology) {
			synchronized (_links) {
				Set<Link> links = new HashSet<Link>(((Topology)obj).getLinks());

				synchronized (links) {
					return _links.equals(links);
				}
			}
		}

		return false;
	}
	/**
	 * clones a WSNTopology using the copy constructor of the actual class
	 */
	@Override
	public final Topology clone() throws CloneNotSupportedException {
		Object clone=null;

		try {
			Constructor<?> constr = this.getClass().getConstructor(this.getClass());
			clone = constr.newInstance(this.getClass().cast(this));
		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}

		if (clone==null) {
			throw new CloneNotSupportedException();
		}
		return (Topology) clone;
	}



	/* protected methods */
	/**
	 * Mark this topology as "updated".<br/>
	 * Update {@link #getUpdatedAt()} and fire an {@link WSNTopologyUpdatedEvent}.
	 */
	protected void _update() {
		_updatedAt = new Date();

		if (this.wsn() != null && this.wsn().app() != null) {
			this.wsn().app().fireEvent(new WSNTopologyUpdatedEvent(this));
		}
	}



	/* protected member */
	/** @see #wsn() */
	protected WSN _wsn=null;
	/** @see #getUpdatedAt() */
	protected Date _updatedAt=null;
	/** @see #getLinks() */
	protected HashSet<Link> _links=new HashSet<Link>();

	private static final long serialVersionUID = -6150672308985917391L;


	/**
	 * representing a WSNTopology Link.<br/>
	 * holds source and target nodeID.<br/>
	 * <i>null</i> as source/target stands for the base node (the global data sink).
	 * 
	 * @author André Freitag
	 */
	public static class Link implements Cloneable {

		/* constructors */
		/**
		 * constructor.
		 * 
		 * @param source
		 * @param target
		 * @source weight
		 */
		public Link(NodeID source, NodeID target, Double weight) {
			this._source = source;
			this._target = target;
			this._weight = weight;
		}
		/**
		 * copy constructor.
		 * 
		 * @param link
		 */
		public Link(Link link) {
			this(link._source, link._target, link._weight);
		}



		/* public methods */
		public NodeID getSource() {
			return _source;
		}
		public NodeID getTarget() {
			return _target;
		}
		public Double getWeight() {
			return _weight;
		}
		/**
		 * checks if this link involves a specific nodeID.
		 * 
		 * @param nodeID
		 * @return true if this.source or this.target equals nodeID
		 */
		public boolean involves(NodeID nodeID) {
			if (nodeID == null) {
				return (this._source==null || this._target==null);
			} else {
				return (nodeID.equals(this._source) || nodeID.equals(this._target));
			}
		}
		/**
		 * checks if a given node is the source of this link
		 */
		public boolean isSource(Node node) {
			return node != null ? node.getNodeID().equals(_source) : _source == null;
		}
		/**
		 * checks if a given node is the target of this link
		 */
		public boolean isTarget(Node node) {
			return node != null ? node.getNodeID().equals(_target) : _target == null;
		}



		/* overridden methods */
		/**
		 * Determines whether two WSNTopology.Links may represent the same link but with a different `weight`.
		 */
		public boolean same(Link link) {
			if (link==null) return false;

			boolean same=true;
			same = same && this.getClass().equals(link.getClass());
			same = same && ((link._source==null && this._source==null) || (link._source!=null && link._source.equals(this._source)));
			same = same && ((link._target==null && this._target==null) || (link._target!=null && link._target.equals(this._target)));

			return same;
		}

		/**
		 * Determines whether two WSNTopology.Links are the same.
		 * returns true if `obj` is an instance of WSNTopology.Link and obj's source,target and weight equal the values of this.
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Link) {
				Link link = (Link) obj;

				boolean equals=true;
				equals = equals && this.getClass().equals(obj.getClass());
				equals = equals && ((link._source==null && this._source==null) || (link._source!=null && link._source.equals(this._source)));
				equals = equals && ((link._target==null && this._target==null) || (link._target!=null && link._target.equals(this._target)));
				equals = equals && ((link._weight==null && this._weight==null) || (link._weight!=null && link._weight.equals(this._weight)));
				return equals;
			}
			return false;
		}
		/**
		 * clones a WSNTopology.Link using the copy constructor of the actual class
		 */
		@Override
		public final Link clone() throws CloneNotSupportedException {
			Object clone=null;

			try {
				Constructor<?> constr = this.getClass().getConstructor(this.getClass());
				clone = constr.newInstance(this.getClass().cast(this));
			} catch (IllegalArgumentException e) {
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			} catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
			}

			if (clone==null) {
				throw new CloneNotSupportedException();
			}
			return (Link) clone;
		}

		/**
		 * override Object.hashCode for easy use in a Hash based Collection.<br/>
		 * creates a hash by forming a unique string from source,target and weight.
		 */
		@Override
		public int hashCode() {
			return new String(this._source!=null?this._source.toString().replace("|","\\|"):null+"|"+this._target!=null?this._target.toString().replace("|","\\|"):null+"|"+this._weight).hashCode();
		}



		/* protected member */
		/** source node ID */
		protected NodeID _source;
		/** target node ID */
		protected NodeID _target;
		/** link weight **/
		protected Double _weight;
	}
}
