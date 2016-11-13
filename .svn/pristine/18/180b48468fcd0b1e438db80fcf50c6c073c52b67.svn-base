package de.tum.in.net.WSNDataFramework;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.Node.Datum.DatumID;
import de.tum.in.net.WSNDataFramework.Events.WSNNodeUpdatedEvent;
import de.tum.in.net.WSNDataFramework.NodeAction.NodeAction;

/**
 * represents a WSN Node.
 * 
 * @author André Freitag
 */
public class Node implements Cloneable, Serializable {

	@SuppressWarnings("unchecked")
	public Class<? extends NodeAction>[] getOfferedActions() {
		return new Class[0];
	}
	public void performAction(NodeAction action) throws ActionNotImplementedException {
		throw new ActionNotImplementedException();
	}
	public static class ActionNotImplementedException extends Exception {
		private static final long serialVersionUID = -8943169047715383981L;
		public ActionNotImplementedException() {
			super();
		}
		public ActionNotImplementedException(String message) {
			super(message);
		}
		public ActionNotImplementedException(String message, Throwable cause) {
			super(message, cause);
		}
		public ActionNotImplementedException(Throwable cause) {
			super(cause);
		}
	}


	/* constructors */
	/**
	 * constructor.
	 * 
	 * @param nodeID
	 */
	public Node(String nodeID, InetSocketAddress address) {
		_address = address;
		_nodeID = new NodeID(nodeID);
	}
	
//	public Node(String nodeID) {
//		_nodeID = new NodeID(nodeID);
//	}
	/**
	 * copy constructor.
	 * creates a deep copy (also copies its `fields`).
	 * Field.value is not cloned, so be careful using mutable types as Field.values.
	 * 
	 * @param node
	 * @throws CloneNotSupportedException if node couldn't be cloned correctly
	 */
	public Node(Node node) throws CloneNotSupportedException {
		this(node.getNodeID().toString(), node.getAddress());
		_updatedAt = node.getUpdatedAt();
		_location = node.getLocation() != null ? node.getLocation().clone() : null;
		for (Datum d: node.data()) {
			_data.add(d.clone());
		}
		for (String info: node._metadata.keySet()) {
			_metadata.put(info, node._metadata.get(info));
		}
	}
	
	/* public methods */
	/**
	 * {@link WSN} this node belongs to.
	 * 
	 * @return {@link WSN} instance this node belongs to || NULL
	 */
	public WSN wsn() {
		return _wsn;
	}
	/**
	 * sets the {@link WSN} this node belongs to.
	 * 
	 * @param wsn
	 * @return this for fluent interface
	 */
	Node setWSN(WSN wsn) {
		_wsn = wsn;
		return this;
	}

	/**
	 * {@link NodeID} identifying this node.<br/>
	 * Not modifiable!
	 */
	public NodeID getNodeID() {
		return _nodeID;
	}

	/**
	 * Returns the {@link Date} this node was updated the last time.<br/>
	 * Returns NULL if this node was never updated/if it was freshly created.<br/>
	 * Not directly modifiable! Gets implicitly updated when calling one of the setters "set*()".
	 * 
	 * @return {@link Date} this node was updated || NULL
	 */
	public Date getUpdatedAt() {
		return (Date)_updatedAt.clone();
	}
	/**
	 * Returns the {@link Data} of this node.<br/>
	 * 
	 * @return Mutable&Iterable {@link Datum} Collection
	 */
	public Data data() {
		return _data;
	}
	/**
	 * Returns the tags of this node.<br/>
	 * 
	 * @return Mutable&Iterable {@link HashSet} Collection
	 */
	public HashSet<String> tags() {
		return _tags;
	}

	/**
	 * The node's {@link Location}.<br/>
	 * Returns a copy => modifying it will not affect the nodes actual location object.
	 * 
	 * @return {@link Location} || NULL
	 */
	public Location getLocation() {
		try {
			return _location!=null ? _location.clone() : null;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	/**
	 * Updates this node's location. Replaces it by the given one and updates WSNNode.updatedAt.
	 * @param location
	 * @return this for fluent interface
	 */
	public Node setLocation(Location location) {
		_location = location;
		_update();

		return this;
	}

	/**
	 * Gets arbitrary metadata about this node set by {@link #setMetadata(String, Object)}.<br/>
	 * The object returned is casted to the given 'infoType'.
	 * 
	 * @param name name the information is filed under
	 * @param infoType type to cast the info to
	 * @return NULL || info casted to 'infoType'
	 */
	public <T> T getMetadata(String name, Class<T> infoType) {
		if (_metadata.containsKey(name)) {
			try {
				// TODO: track changes to the info object..
				return infoType.cast(_metadata.get(name));
			} catch (Exception e) {
			}
		}

		return null;
	}
	/**
	 * Gets all metadata assigned to this node.
	 * 
	 * @see #getMetadata(String, Class)
	 * @return (name => info)
	 */
	public Map<String,Object> getMetadata() {
		return new HashMap<String,Object>(_metadata);
	}
	/**
	 * Adds the given metadata to this node's info object and updates WSNNode.updatedAt.
	 * 
	 * @param name name describing the kind of information
	 * @param info the info to save
	 * @return this for fluent interface
	 */
	public Node setMetadata(String name, Object info) {
		_metadata.put(name, info);
		_update();

		return this;
	}
	
	/**
	 * @return the address under which this node is reachable.
	 */
	public InetSocketAddress getAddress() {
		return _address;
	}

	/**
	 * Determines whether two nodes may be considered the same.
	 * Compares ID + field entries (two field entries are the same if all their fields are equal except their value).
	 */
	public boolean same(Node node) {
		if (node==null) return false;

		boolean same=true;

		// compare type
		same = same && this.getClass().equals(node.getClass());

		// compare ID
		same = same && (node.getNodeID()!=null && node.getNodeID().equals(this.getNodeID()) || node.getNodeID()==null && this.getNodeID()==null);

		// compare fields
		List<Datum> tempFields=new ArrayList<Datum>();
		tempFields.addAll(node.data());
		same = same && tempFields.size()==_data.size();

		if (same) {
			for (Datum Field: _data) {
				for (int i=0; i<tempFields.size(); i++) {

					if (tempFields.get(i).same(Field)) {
						tempFields.remove(i);
						break;
					}
				}
			}

			same = same && tempFields.size()==0;
		}

		return same;
	}

	/**
	 * Determines whether two nodes are exact the same.
	 * Checks all public fields for equality.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj==null) return false;

		if (obj instanceof Node) {
			Node node = (Node) obj;

			boolean equals=true;

			// compare type
			equals = equals && this.getClass().equals(node.getClass());

			// compare fields
			equals = equals && (node.getNodeID()!=null && node.getNodeID().equals(this.getNodeID()) || node.getNodeID()==null && this.getNodeID()==null);
			equals = equals && (node.getLocation()!=null && node.getLocation().equals(this.getLocation()) || node.getLocation()==null && this.getLocation()==null);
			equals = equals && (node.getUpdatedAt()!=null && node.getUpdatedAt().equals(this.getUpdatedAt()) || node.getUpdatedAt()==null && this.getUpdatedAt()==null);

			// compare fields
			List<Datum> tempFields=new ArrayList<Datum>();
			tempFields.addAll(node._data);
			equals = equals && tempFields.size()==_data.size();

			if (equals) {
				for (Datum Field: _data) {
					for (int i=0; i<tempFields.size(); i++) {

						if (tempFields.get(i).equals(Field)) {
							tempFields.remove(i);
							break;
						}
					}
				}

				equals = equals && tempFields.size()==0;
			}

			return equals;
		}

		return false;
	}


	/**
	 * clones a WSNNode using the copy constructor of the actual class
	 * 
	 * @throws CloneNotSupportedException if actual class doesn't offer a copy constructor!
	 */
	@Override
	public Node clone() throws CloneNotSupportedException {
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
		return (Node) clone;
	}

	@Override
	public String toString() {
		return "#"+this.getNodeID()+": "+_data.toString();
	}



	/* protected methods */
	/**
	 * Mark this node as "updated".<br/>
	 * Update {@link #getUpdatedAt()} and fire an {@link WSNNodeUpdatedEvent}.
	 */
	protected void _update() {
		_updatedAt = new Date();

		if (this.wsn() != null && this.wsn().app() != null) {
			this.wsn().app().fireEvent(new WSNNodeUpdatedEvent(this));
		}
	}


	/* protected member */
	/** @see #wsn() */
	protected WSN _wsn=null;
	/** @see #getNodeID() */
	protected NodeID _nodeID=null;
	/** @see #getUpdatedAt() */
	protected Date _updatedAt=null;
	/** @see #data() */
	protected Data _data=new Data(this);
	/** @see #tags() */
	protected HashSet<String> _tags=new HashSet<String>();
	/** @see #getLocation() */
	protected Location _location=null;
	/** @see #getMetadata(String, Class) */
	protected Map<String,Object> _metadata=new HashMap<String,Object>();
	/** @see #getAddress() */
	protected InetSocketAddress _address = null;

	/* private member */
	private static final long serialVersionUID = 2258458696530387311L;


	public static class Data implements Collection<Datum> {

		/* constructors */
		/**
		 * constructor
		 * @param node
		 */
		protected Data(Node node) {
			_node = node;
		}


		/* public methods */
		@Override
		public boolean add(Datum datum) {
			if (datum != null) {
				// add
				_data.put(datum.getID(), datum);

				// update node
				_update();

				return true;
			}
			return false;
		}
		@Override
		public boolean addAll(Collection<? extends Datum> c) {
			// add
			for (Datum d: c) {
				_data.put(d.getID(), d);
			}

			// update node
			_update();

			return true;
		}
		public Datum get(DatumID id) {
			return _data.get(id);
		}
		public Datum get(String id) {
			return this.get(new DatumID(id));
		}
		public boolean update(Datum datum) {
			if (datum != null) {
				// update
				_data.put(datum.getID(), datum);

				// update node
				_update();

				return true;
			}

			return false;
		}
		public boolean remove(DatumID id) {
			// remove
			_data.remove(id);

			// udpate node
			_update();

			return true;
		}
		public boolean remove(String id) {
			return this.remove(new DatumID(id));
		}
		@Override
		public boolean remove(Object datum) {
			if (datum != null && datum instanceof Datum) {
				return this.remove(((Datum)datum).getID());
			} else {
				return false;
			}
		}
		@Override
		public boolean removeAll(Collection<?> c) {
			// remove
			_data.values().removeAll(c);

			// update node
			_update();

			return true;
		}
		@Override
		public boolean retainAll(Collection<?> c) {
			// retain
			_data.values().retainAll(c);

			// update node
			_update();

			return true;
		}
		@Override
		public void clear() {
			// clear
			_data.clear();

			// update node
			_update();
		}


		/**
		 * Returns an iterator over all {@link Datum} entries.<br/>
		 * Does NOT support {@link Iterator.remove()}!
		 */
		@Override
		public Iterator<Datum> iterator() {
			return Collections.unmodifiableCollection(_data.values()).iterator();
		}


		/* overridden methods */
		@Override
		public boolean isEmpty() {
			return _data.isEmpty();
		}
		@Override
		public int size() {
			return _data.size();
		}
		@Override
		public Object[] toArray() {
			return _data.values().toArray();
		}
		@Override
		public <T> T[] toArray(T[] a) {
			return _data.values().toArray(a);
		}
		@Override
		public boolean contains(Object o) {
			return _data.values().contains(o);
		}
		@Override
		public boolean containsAll(Collection<?> c) {
			return _data.values().containsAll(c);
		}
		@Override
		public String toString() {
			return _data.toString();
		}



		/* protected methods */
		protected void _update() {
			if (_node != null) {
				_node._update();
			}
		}



		/* protected member */
		protected Node _node=null;
		protected Map<DatumID,Datum> _data=new HashMap<DatumID,Datum>();
	}
	/*** field entry class ***/
	public static class Datum implements Cloneable,Serializable {

		/* constructors */
		/**
		 * constructor
		 * 
		 * @param ID
		 * @param name
		 * @param type
		 * @param value
		 * @param unit
		 */
		public Datum(String ID, String name, String type, Object value, String unit) {
			_id = new DatumID(ID);
			_name = name;
			_type = type;
			_value = value;
			_unit = unit;
		}
		/**
		 * constructor
		 * 
		 * @param ID
		 * @param name
		 * @param type
		 * @param value
		 * @param unit
		 */
		public Datum(DatumID ID, String name, String type, Object value, String unit) {
			_id = ID;
			_name = name;
			_type = type;
			_value = value;
			_unit = unit;
		}
		/**
		 * constructor, omits Datum.name.
		 * 
		 * @param ID
		 * @param type
		 * @param value
		 * @param unit
		 */
		public Datum(String ID,String type, Object value, String unit) {
			this(ID, null, type, value, unit);
		}
		/**
		 * constructor, omits Datum.name.
		 * 
		 * @param ID
		 * @param type
		 * @param value
		 * @param unit
		 */
		public Datum(DatumID ID,String type, Object value, String unit) {
			this(ID, null, type, value, unit);
		}
		/**
		 * copy constructor.
		 * Does not clone Field.value ! So be careful if using mutable types as Field.value.
		 * 
		 * @param Field
		 */
		public Datum(Datum field) {
			this(field.getID().toString(), field.getName(), field.getType(), field.getValue(), field.getUnit());
		}



		/* public methods */
		/** ID that uniquely identifies this datum in the scope of the node */
		public DatumID getID() {
			return _id;
		}
		/** specific name (may be null) */
		public String getName() {
			return _name;
		}
		/** Datum type */
		public String getType() {
			return _type;
		}
		/** Datum value */
		public Object getValue() {
			return _value;
		}
		/** Datum unit */
		public String getUnit() {
			return _unit;
		}



		/* overridden methods */
		/**
		 * Determines whether two Field may represent the same field.
		 * returns true if all fields except `value` are equal.
		 */
		public boolean same(Datum field) {
			if (field==null) return false;

			boolean same=true;
			same = same && this.getClass().equals(field.getClass());
			same = same && (field.getID()!=null && field.getID().equals(_id) || field.getID()==null && _id==null);
			same = same && (field.getName()!=null && field.getName().equals(_name) || field.getName()==null && _name==null);
			same = same && (field.getType()!=null && field.getType().equals(_type) || field.getType()==null && _type==null);
			same = same && (field.getUnit()!=null && field.getUnit().equals(_unit) || field.getUnit()==null && _unit==null);

			return same;
		}
		/**
		 * Determines whether two Field may be considered equal.
		 * return true if obj is a Field and all its fields equal the fields of this.
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj==null) return false;

			boolean equals=true;

			// compare types
			equals = equals && this.getClass().equals(obj.getClass());

			// compare fields
			if (obj instanceof Node.Datum) {
				Datum field = (Datum) obj;

				equals = equals && this.getClass().equals(field.getClass());
				equals = equals && (field.getID()!=null && field.getID().equals(_id) || field.getID()==null && _id==null);
				equals = equals && (field.getName()!=null && field.getName().equals(_name) || field.getName()==null && _name==null);
				equals = equals && (field.getType()!=null && field.getType().equals(_type) || field.getType()==null && _type==null);
				equals = equals && (field.getUnit()!=null && field.getUnit().equals(_unit) || field.getUnit()==null && _unit==null);
				equals = equals && (field.getValue()!=null && field.getValue().equals(_value) || field.getValue()==null && _value==null);
			}

			return equals;
		}


		/**
		 * clones a WSNNode.Field using the copy constructor of the actual class
		 * 
		 * @throws CloneNotSupportedException if actual class doesn't offer a copy constructor!
		 */
		@Override
		public final Datum clone() throws CloneNotSupportedException {
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
			return (Datum) clone;
		}

		@Override
		public String toString() {
			return (_name!=null?_name:_type)+"="+_value;
		}



		/* protected member */
		/** @see #getID() */
		protected DatumID _id;
		/** @see #getName() */
		protected String _name;
		/** @see #getType() */
		protected String _type;
		/** @see #getValue() */
		protected Object _value;
		/** @see #getUnit() */
		protected String _unit;

		/* private member */
		private static final long serialVersionUID = -9066980604618593645L;



		/* helper */
		public static class DatumID extends ID {
			public DatumID(String id) {
				super(id);
			}
		}
	}

	/** WSNNode location */
	public static class Location implements Cloneable,Serializable {

		/* constructors */
		/**
		 * constructor
		 * 
		 * @param latitude
		 * @param longitude
		 * @param elevation
		 */
		public Location(Double latitude, Double longitude, Double elevation) {
			_latitude = latitude;
			_longitude = longitude;
			_elevation = elevation;
		}
		/**
		 * copy constructor.
		 * 
		 * @param location
		 */
		public Location(Location location) {
			this(location._latitude, location._longitude, location._elevation);
		}



		/* public methods */
		public Double getLatitude() {
			return _latitude;
		}
		public Double getLongitude() {
			return _longitude;
		}
		public Double getElevation() {
			return _elevation;
		}



		/* overridden methods */
		/**
		 * overrides equals.
		 * checks if two Locations equal regarding their latitude, longitude and elevation.
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj==null) return false;

			boolean equals = true;

			// compare type
			equals = equals && this.getClass().equals(obj.getClass());

			// compare fields
			if (obj instanceof Location) {
				Location loc = (Location) obj;
				equals = equals && (loc._latitude!=null && loc._latitude.equals(_latitude) || loc._latitude==null && _latitude==null);
				equals = equals && (loc._longitude!=null && loc._longitude.equals(_longitude) || loc._longitude==null && _longitude==null);
				equals = equals && (loc._elevation!=null && loc._elevation.equals(_elevation) || loc._elevation==null && _elevation==null);
			}

			return equals;
		}

		/**
		 * clones a WSNNode.Location using the copy constructor of the actual class.
		 * 
		 * @throws CloneNotSupportedException if actual class doesn't offer a copy constructor!
		 */
		@Override
		public final Location clone() throws CloneNotSupportedException {
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
			return (Location) clone;
		}



		/* protected member */
		protected Double _latitude=null;
		protected Double _longitude=null;
		protected Double _elevation=null;



		/* private member */
		private static final long serialVersionUID = 7443814019088917386L;
	}
	public static class NodeID extends ID {
		public NodeID(String id) {
			super(id);
		}
	}
	protected static class ID {
		public ID(String id) {
			_id = id;
		}
		@Override
		public String toString() {
			return _id;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj!=null && obj instanceof ID) {
				ID id = (ID) obj;

				return id.toString().equals(this.toString());
			} else if (obj!=null && obj instanceof String) {
				String id = (String) obj;

				return id.equals(this.toString());
			}
			return false;
		}
		@Override
		public int hashCode() {
			return _id.hashCode();
		}

		protected String _id=null;
	}
}
