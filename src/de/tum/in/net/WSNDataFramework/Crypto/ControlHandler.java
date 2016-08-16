package de.tum.in.net.WSNDataFramework.Crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ControlHandler implements MessageHandler {
	
	private TinyTO tinyTO;

	public void processMessage(byte[] data, InetAddress address) {
		
		int controlType = data[0];
		byte msg[] = new byte[data.length - 1];
		System.arraycopy(data, 1, msg, 0, data.length - 1);
		
		switch (controlType) {
		case 0x01 : processAggRequest(msg, address); break;
		}
		
	}

	private void processAggRequest(byte[] msg, InetAddress sender) {
				
		try {
			// convert the plain string into an IPv6 address - ugly workaround
			String plain = TinyTOMessageHandler.byteArrayToHexString(msg);
			StringBuffer strBuff = new StringBuffer (plain); 
			int len = plain.length();
			for(int i = len-4; i > 0; i = i - 4){
				strBuff.insert(i, ":");
			}

			InetAddress agg = InetAddress.getByName(strBuff.toString());
			MoteParameters moteParams = SessionHandler.getMoteParameters(agg);
			if(moteParams.isAuthenticated() && moteParams.getMoteType() == MoteType.AGGREGATOR){
				sendAggregatorConfirmation(sender, agg);
				System.out.println("Aggregator " + agg.getHostAddress() + " confirmed for mote " + sender.getHostAddress());
			} else{
				System.out.println("Unauthorized Aggregator: " + agg.getHostAddress());
				System.out.println(moteParams.isAuthenticated() + "," + moteParams.getMoteType());
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Builds and sends the answer to the request.
	 * Message structure:
	 * | 0x02 | 0x01 | pub_Agg | Sign_BS(pub_Agg, id_Agg, id_M) |
	 * @param sender 
	 * @return 
	 */
	private void sendAggregatorConfirmation(InetAddress sender, InetAddress aggregator) {
		
		MoteParameters aggParameters = SessionHandler.getMoteParameters(aggregator);
		
		byte msgType = 0x02;
		byte cType = 0x02;
		byte[] pubAgg = tinyTO.getPubKeyDecoded(aggParameters.getPubKeyMote());
		byte[] idAgg = aggregator.getAddress();
		byte[] idMote = sender.getAddress();

		// concatenate the content to sign it later
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			outputStream.write(pubAgg);
			outputStream.write(idMote);
			outputStream.write(idAgg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] sign_msg = outputStream.toByteArray().clone();
		byte[] signed_msg = null;
		
		// sign it
		signed_msg = tinyTO.signMessage(sender, sign_msg);
		
		System.out.println(TinyTOMessageHandler.byteArrayToHexString(signed_msg));
		// now write the complete final message
		outputStream.reset();
		try {
			outputStream.write(msgType);
			outputStream.write(cType);
			outputStream.write(pubAgg);
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
	
	public static void setDoa(int doa, InetAddress target){
		byte msgType = 0x02;
		byte cType = 0x03;
		BigInteger bi = BigInteger.valueOf(doa);
		byte[] bytes = bi.toByteArray();
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			outputStream.write(msgType);
			outputStream.write(cType);
			outputStream.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] msg = outputStream.toByteArray();
		
		TinyTOMessageHandler.sendMessage(target, msg);
	}
	
}
