package de.tum.in.net.WSNDataFramework.Modules.IPFIX;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import de.tum.in.net.WSNDataFramework.Modules.TinyOS.DataPacket;




/**
 * Module handling data coming from an (tiny)IPFIX based WSN using DTLS. Listening to UDP socket.
 * Handles IPFIX messages as well as tinyIPFIX ones.
 * 
 * The packet format is supposed to conform to the SSL Proxy struct sourcedData:
 * struct sourcedData{
 *	unsigned short	addr_len;
 *	char 			addr[47];
 *	unsigned short	port;
 *	unsigned short	data_len;
 *	}sourcedData;
 *
 * The read packets are passed to the (tiny)IPFIX Parser.
 * 
 * @author Thomas Kothmayr, adapted by André Freitag
 * @version 0.1
 */
public class WSNTinyIPFIXDTLSModule extends WSNTinyIPFIXModule {

	/* constructors */
	/**
	 * listen to :port, use pathToMetdata to parse packets
	 *
	 * @param pathToMetadata path to metadata file used to translate incoming packets
	 * @param port to listen to
	 * @param data.getAddress() to listen to
	 * @throws Exception
	 */
	public WSNTinyIPFIXDTLSModule(String pathToMetadata, int port) throws Exception {
		super(pathToMetadata,port);
	}
	/**
	 * listen to :5568, use pathToMetdata to parse packets
	 *
	 * @param pathToMetadata path to metadata file used to translate incoming packets
	 * @param port to listen to
	 * @throws Exception
	 */
	public WSNTinyIPFIXDTLSModule(String pathToConf) throws Exception {
		this(pathToConf, 5568);
	}


	/* overridden methods */
	/**
	 * overridden version of _receiveDataPacket. Extracts DTLS header.
	 */
	@Override
	protected DataPacket _receiveDataPacket() throws IOException {
		// receive packet
		byte[] buf = new byte[2048];
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		_serverSocket.receive(p);

		// extract DTLS header
		byte[] data = new byte[p.getLength()];
		System.arraycopy(p.getData(), 0, data, 0, data.length);
		int dataLen = getDataLen(data);
		if(p.getLength() < HEADER_LEN + dataLen){
			System.err.println("Insufficient Length for a SSL Proxy packet");
			return null;
		}
		

		byte[] realdata = new byte[dataLen];
		System.arraycopy(data, HEADER_LEN, realdata, 0, dataLen);

		_parser._log("SSL received, data length: %d", dataLen);
		System.out.println("SSL packet");

		// return packet
		return new DataPacket(realdata, (InetSocketAddress)p.getSocketAddress());
	}


	/* protected helper */
	private static final int HEADER_LEN = 54;

	protected int getAddrLen(byte[] buf) throws IOException{
		UnsignedDataInputStream stream = new UnsignedDataInputStream(new ByteArrayInputStream(buf, 0, 2));
		int ret = stream.readUnsignedShort();
		stream.close();
		return ret;
	}
	protected int getPort(byte[] buf) throws IOException{
		UnsignedDataInputStream stream = new UnsignedDataInputStream(new ByteArrayInputStream(buf, 50, 2));
		int ret = stream.readUnsignedShort();
		stream.close();
		return ret;
	}
	protected int getDataLen(byte[] buf) throws IOException{
		UnsignedDataInputStream stream = new UnsignedDataInputStream(new ByteArrayInputStream(buf, 52, 2));
		int ret = stream.readUnsignedShort();
		stream.close();
		return ret;
	}

	String getAddress(byte[] buf) throws IOException{

		if(buf.length < HEADER_LEN){
			return null;
		}
		int len = getAddrLen(buf);
		if(len < 0){
			return null;
		}
		return new String(buf, 2, len);
	}

}
