package de.tum.in.net.WSNDataFramework.CUSTOM;

import de.tum.in.net.WSNDataFramework.WSNProtocol;
import de.tum.in.net.WSNDataFramework.WSNProtocolException;
import de.tum.in.net.WSNDataFramework.WSNProtocolPacket;

public class PhilippSSHProtocol extends WSNProtocol {

	@Override
	public String getName() {
		return "Philipp SSH Translator";
	}

	@Override
	public WSNProtocolPacket process(WSNProtocolPacket packet) throws WSNProtocolException {
		//System.out.println("Proto: #"+packet.getID()+" "+this.getName());

		byte[] originalPayload = packet.getPayload();

		// hier originalPayload entschlüsseln
		boolean translationSuccessful = true;
		byte[] newPayload = originalPayload;


		return translationSuccessful
				? packet.dup(newPayload)	// altes Packet mit neuer Payload weiterreichen
						: null;				// NULL zurückgeben falls Übersetzung fehlgeschlagen ist
	}
}
