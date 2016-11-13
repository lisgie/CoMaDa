package de.tum.in.net.WSNDataFramework.Modules.IPFIX;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class PacketUtils {

	// IP header = 40, UDP header = 8 bytes
	public static final int IP_UDP_HEADER_SIZE = 48;
	public static final int IP_ADDRESS_SIZE = 16;
	
	public static byte[] extractPayload(byte[] packet) throws IllegalArgumentException {
		
		if(packet.length <= IP_UDP_HEADER_SIZE) {
			throw new IllegalArgumentException();
		}
		
		byte[] payload = new byte[packet.length - IP_UDP_HEADER_SIZE];
		System.arraycopy(packet, IP_UDP_HEADER_SIZE, payload, 0, payload.length);
		
		return payload;
	}
	
	public static InetSocketAddress extractSourceAddress(byte[] packet)
			throws UnknownHostException, IllegalArgumentException {
		
		int port_int;
		
		if(packet.length < IP_UDP_HEADER_SIZE) {
			throw new IllegalArgumentException();
		}
		
		byte[] sourceAddress = new byte[16];
		System.arraycopy(packet, IP_ADDRESS_SIZE, sourceAddress, 0, IP_ADDRESS_SIZE);
		
		byte[] port = new byte[2];
		System.arraycopy(packet, IP_UDP_HEADER_SIZE - 8, port, 0, 2);
		
		//return new InetSocketAddress(InetAddress.getByAddress(sourceAddress), ByteBuffer.wrap(port).getInt());
		port_int = (int)(port[0] << 8);
		port_int |= (int)(port[1]);
		port_int &= 0x0000ffff;
		
		return new InetSocketAddress(InetAddress.getByAddress(sourceAddress), port_int);
	}
	
	public static InetSocketAddress extractDestinationAddress(byte[] packet)
			throws UnknownHostException, IllegalArgumentException {
		
		if(packet.length < IP_UDP_HEADER_SIZE) {
			throw new IllegalArgumentException();
		}
		
		byte[] destinationAddress = new byte[16];
		System.arraycopy(packet, 0, destinationAddress, 0, IP_ADDRESS_SIZE);
		
		byte[] port = new byte[2];
		System.arraycopy(packet, IP_UDP_HEADER_SIZE - 6, port, 0, 2);
		
		return new InetSocketAddress(InetAddress.getByAddress(destinationAddress), ByteBuffer.wrap(port).getInt());
	}
}
