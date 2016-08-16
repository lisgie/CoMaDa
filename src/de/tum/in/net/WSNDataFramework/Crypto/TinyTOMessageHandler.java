package de.tum.in.net.WSNDataFramework.Crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class TinyTOMessageHandler implements MessageHandler {
	
	// motes listen on port 4738 for handshake messages
	private static final int _handhakeMsgPort = 4738;
	private HandshakeHandler hsHandler;
	private ControlHandler cmsgHandler;
	
	// basestation address as ID
	private final String BS_ID = "fec0::100";
	
	private static DatagramSocket clientSocket;

	public TinyTOMessageHandler(TinyTO tinyTO) {
		hsHandler = new HandshakeHandler();
		hsHandler.setCrypto(tinyTO);
		cmsgHandler = new ControlHandler();
		cmsgHandler.setCrypto(tinyTO);
	}

	/* (non-Javadoc)
	 * @see de.tum.in.net.WSNDataFramework.Crypto.MessageHandler#processMessage(byte[], java.net.InetAddress)
	 */
	@Override
	public void processMessage(byte[] data, InetAddress address) {
		
		// stop if the authentication is already over
		// if(SessionHandler.getMoteParameters(address).isAuthenticated()) return;
		
		// first byte is the handshake type, message begins on index 1
		int messageType = data[0];
		byte msg[] = new byte[data.length - 1];
		System.arraycopy(data, 1, msg, 0, data.length - 1);
		
		System.out.println("RECV \n" + byteArrayToHexString(data));
		switch (messageType) {
		case 1 : hsHandler.processMessage(msg, address); break;
		case 2 : cmsgHandler.processMessage(msg, address); break;
		}
	}


	protected static void sendMessage(InetAddress sender, byte[] message) {
		
		System.out.println("SENDING: \n"+byteArrayToHexString(message));

		if( clientSocket == null) 
		{
			try {
				clientSocket = new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		DatagramPacket sendPacket = new DatagramPacket(message, message.length, sender, _handhakeMsgPort);
		
		try {
			clientSocket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	// helper methods //
	public static String byteArrayToHexString(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for (byte b : a) {
			int i = b & 0xff;
			sb.append(String.format("%02x", i));
		}
		return sb.toString();
	}
    
    

}
