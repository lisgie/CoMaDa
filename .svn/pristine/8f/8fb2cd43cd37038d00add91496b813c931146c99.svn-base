package de.tum.in.net.WSNDataFramework.Crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class HandshakeHandler implements MessageHandler{
	
	// motes listen on port 4738 for handshake messages
	private static final int _handhakeMsgPort = 4738;
	private TinyTO tinyTO;
	
	// basestation address as ID
	private final String BS_ID = "fec0::100";
	
	private DatagramSocket clientSocket;

	protected HandshakeHandler() {
		
	}

	public void processMessage(byte[] data, InetAddress address) {
		SessionHandler.addSender(address);
	
		// first byte is the handshake type, message begins on index 1
		byte handshakeType = data[0];
		byte msg[] = new byte[data.length - 1];
		System.arraycopy(data, 1, msg, 0, data.length - 1);
		
		switch (handshakeType) {
		case 0x11 : processMsg1(msg, address, MoteType.COLLECTOR); break;
		case 0x21 : processMsg1(msg, address, MoteType.AGGREGATOR); break;
		case 0x03 : processMsg3(msg, address); break;
		}
	}


	/**
	 * Processes the first handshake message.
	 * We expect a packet like this: < br >
	 * | pub_x ( = 24 bytes) 00 00 | pub_y ( = 24 bytes) 00 00 | H(masterkey, pub_M.x, pub_M.y) ( = 20 byte ) |
	 * @param data handshake message 1 without type field
	 * @param sender address of the sending node
	 * @param aggregator 
	 */
	protected void processMsg1(byte[] data, InetAddress sender, MoteType moteType) {
		System.out.println("Authentication start: " + sender.getHostAddress());
		tinyTO.init(sender);
		
		byte pub_x[] = new byte [24];
		System.arraycopy(data, 0, pub_x, 0, 24);
		
		byte pub_y[] = new byte [24];
		System.arraycopy(data, 24, pub_y, 0, 24);
		
		byte[] digest = new byte [20];
		System.arraycopy(data, 48, digest, 0, 20);
		
		
		tinyTO.storeMotePublicKey(sender, new BigInteger(pub_x), new BigInteger(pub_y));
		MoteParameters moteParameters = SessionHandler.getMoteParameters(sender);
		moteParameters.setMoteType(moteType);
		
		// recreate signed message
		byte[] pubM = tinyTO.getPubKeyDecoded(moteParameters.getPubKeyMote());
		byte[] masterkey = KeyStore.getKey(sender.getHostAddress());

		// concatenate the content
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(masterkey);
			outputStream.write(pubM);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] hash_msg = outputStream.toByteArray();
		
		boolean b = tinyTO.verifyHash(hash_msg, digest);
		
		if(b){
			System.out.println("Masterkey: valid");
		} else {
			System.out.println("Masterkey: invalid");
			return;
		}
		
		tinyTO.doKeyAgreement(sender);

		sendMsg2(sender);
	}
	

	/**
	 * Processes the third handshake message.
	 * We expect a packet like this: < br >
	 * | r ( = 24 byte) | s ( = 24 byte) |
	 * @param data handshake message 3 without type field
	 * @param address 
	 */
	protected void processMsg3(byte[] data, InetAddress sender) {
		MoteParameters moteParameters = SessionHandler.getMoteParameters(sender);
		byte r[] = new byte [24];
		byte s[] = new byte [24];
		
		System.arraycopy(data, 0, r, 0, 24);
		System.arraycopy(data, 24, s, 0, 24);
		
//		System.out.println("r " + byteArrayToHexString(r));
//		System.out.println("s " + byteArrayToHexString(s));
		
		// recreate signed message
		byte[] pubM = tinyTO.getPubKeyDecoded(moteParameters.getPubKeyMote());
		byte[] pubBS = tinyTO.getPubKeyDecoded(moteParameters.getPubKeyBS());	
		byte[] id = null;
		
		
		try {
			InetAddress a = InetAddress.getByName(BS_ID);
			id = a.getAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   

		// concatenate the content
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			outputStream.write(pubM);
			outputStream.write(pubBS);
			outputStream.write(id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] sign_msg = outputStream.toByteArray();
		
		boolean result = tinyTO.verifySignature(sender, sign_msg, r, s);
		
		if(result) {
			System.out.println("Authenticated successfully: " + sender.getHostAddress());
			moteParameters.setAuthenticated(true);
		} else {
			System.out.println("Authentication failed: " + sender.getHostAddress());
		}
	}	
	

	/**
	 * Builds and sends the answer to the first message.
	 * Message structure:
	 * | 0x02 | pub_BS | Sign_BS(pub_BS, pub_M, id_M) | H(masterkey, pub_BS) |
	 * @param sender 
	 * @return 
	 */
	private void sendMsg2(InetAddress sender) {
		
		MoteParameters moteParameters = SessionHandler.getMoteParameters(sender);
		
		byte msgType = 0x01;
		byte hsType = 0x02;
		byte[] pubBS = tinyTO.getPubKeyDecoded(moteParameters.getPubKeyBS());	
		byte[] pubM = tinyTO.getPubKeyDecoded(moteParameters.getPubKeyMote());
		byte[] id = sender.getAddress();

		// concatenate the content to sign it later
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			outputStream.write(pubBS);
			outputStream.write(pubM);
			outputStream.write(id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] sign_msg = outputStream.toByteArray().clone();
		byte[] signed_msg = null;
		
		// sign it
		signed_msg = tinyTO.signMessage(sender, sign_msg);
		
		outputStream.reset();
		
		try {
			outputStream.write(KeyStore.getKey(sender.getHostAddress()));
			outputStream.write(pubBS);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		byte[] digest = tinyTO.hashMessage(outputStream.toByteArray());
		
		
		// now write the complete final message
		outputStream.reset();
		try {
			outputStream.write(msgType);
			outputStream.write(hsType);
			outputStream.write(pubBS);
			outputStream.write(digest);
			outputStream.write(signed_msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] message = outputStream.toByteArray();
		
		TinyTOMessageHandler.sendMessage(sender, message);
		
	}

	public void setCrypto(TinyTO tinyTO) {
		this.tinyTO = tinyTO;
	}
    
    

}
