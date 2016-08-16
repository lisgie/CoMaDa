package de.tum.in.net.WSNDataFramework;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class WSNDriver extends WSNModule {

	/* protected methods */
	/**
	 * Synchronously process a packet.
	 * 
	 * @param packet
	 */
	protected void _processPacket(WSNProtocolPacket packet) {
		if (this.app() != null) {
			this.app()._processPacket(packet);
		}
	}
	/**
	 * Synchronously process a packet.
	 * 
	 * @param packet
	 * @param source
	 * @param initialInfo
	 */
	protected void _processPacket(byte[] packet, InetSocketAddress source, String initialInfo) {
		if (this.app() != null) {
			WSNProtocolPacket p = new WSNProtocolPacket(++_packetID, packet, source);
			p.info().append(initialInfo);
			_processPacket(p);
		}
	}
	/**
	 * Synchronously process a packet.
	 * 
	 * @param packet
	 * @param source
	 */
	protected void _processPacket(byte[] packet, InetSocketAddress source) {
		this._processPacket(packet, source, new String());
	}
	/**
	 * Synchronously process a packet.
	 * Takes localhost as source.
	 * 
	 * @param packet
	 */
//	protected void _processPacket(byte[] packet) {
//		try {
//			_processPacket(packet, InetAddress.getLocalHost());
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//			_processPacket(packet, null);
//		}
//	}


	protected long _packetID=0;
}
