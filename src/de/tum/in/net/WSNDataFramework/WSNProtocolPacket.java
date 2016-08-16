package de.tum.in.net.WSNDataFramework;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import de.tum.in.net.WSNDataFramework.Events.WSNEvent;

public class WSNProtocolPacket extends WSNEvent {

	public WSNProtocolPacket(long id, byte[] payload, InetSocketAddress source) {
		_id = id;
		_payload = payload;
		_source = source;
	}
	public WSNProtocolPacket(WSNProtocolPacket packet) {
		this(packet.getID(), packet.getPayload(), packet.getSourceAddress());
		_addInfo = new StringBuffer(packet.info());
	}


	public long getID() {
		return _id;
	}
	public byte[] getPayload() {
		return _payload;
	}
	public InetSocketAddress getSourceAddress() {
		return _source;
	}
	/**
	 * StringBuffer to add additional information to.<br/>
	 * Used for HTML GUI output.
	 */
	public StringBuffer info() {
		return _addInfo;
	}
	public WSNProtocolPacket dup(byte[] newPayload) {
		return new WSNProtocolPacket(
				_id,
				newPayload,
				_source);
	}


	/* protected member */
	protected long _id;
	protected byte[] _payload;
	protected InetSocketAddress _source;
	protected StringBuffer _addInfo = new StringBuffer();
}
