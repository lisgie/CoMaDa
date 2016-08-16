package de.tum.in.net.WSNDataFramework.Events;

import de.tum.in.net.WSNDataFramework.WSNProtocolPacket;

public class WSNProtocolPacketProcessedEvent extends WSNEvent {

	public WSNProtocolPacketProcessedEvent(WSNProtocolPacket packet) {
		_packet = packet;
	}

	public WSNProtocolPacket packet() {
		return _packet;
	}

	protected WSNProtocolPacket _packet;

}
