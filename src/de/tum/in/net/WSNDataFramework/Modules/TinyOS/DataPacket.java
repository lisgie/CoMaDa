package de.tum.in.net.WSNDataFramework.Modules.TinyOS;

import java.net.InetSocketAddress;

/**
 * class representing the result of _receiveDataPacket()
 * @author André Freitag
 */
public class DataPacket {
	public DataPacket(byte[] data, InetSocketAddress address) {
		this.data = data;
		this.address = address;
	}
	public DataPacket() {
		// TODO Auto-generated constructor stub
	}
	public InetSocketAddress address;
	public byte[] data;
}