package de.tum.in.net.WSNDataFramework.Protocols;

import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;

import de.tum.in.net.WSNDataFramework.WSNProtocol;
import de.tum.in.net.WSNDataFramework.WSNProtocolException;
import de.tum.in.net.WSNDataFramework.WSNProtocolPacket;
import de.tum.in.net.WSNDataFramework.Crypto.SessionHandler;
import de.tum.in.net.WSNDataFramework.Crypto.TinyTO;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXField;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXParser;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXParser.ParseException;

public class TinyToProtocol extends WSNProtocol {

	@Override
	public String getName() {
		return "TinyTO Protocol";
	}

	@Override
	public synchronized WSNProtocolPacket process(WSNProtocolPacket packet)
			throws WSNProtocolException {
		System.out.println("Proto: #" + packet.getID() + " " + this.getName());
		if (SessionHandler.getMoteParameters(packet.getSourceAddress().getAddress()).isAuthenticated()) {
			byte[] plaintext;
			try {
				plaintext = TinyTO.decryptECIES(packet.getSourceAddress(), packet.getPayload());
				if(plaintext == null) return null;
				return new WSNProtocolPacket(packet.getID(), plaintext,	packet.getSourceAddress());
			} catch (BadPaddingException e) {
				return null;
			}
			
		} else
			return packet;
		

	}
}
