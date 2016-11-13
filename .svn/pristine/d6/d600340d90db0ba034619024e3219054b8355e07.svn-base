package de.tum.in.net.WSNDataFramework.CUSTOM;

import de.tum.in.net.WSNDataFramework.WSNProtocol;
import de.tum.in.net.WSNDataFramework.WSNProtocolException;
import de.tum.in.net.WSNDataFramework.WSNProtocolPacket;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.TinyIPFIXParser;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.TinyIPFIXParser.ParseException;

public class TinyIPFIXProtocol extends WSNProtocol {

	@Override
	public String getName() {
		return "TinyIPFIX Translator";
	}

	@Override
	public WSNProtocolPacket process(WSNProtocolPacket packet) throws WSNProtocolException {
		//System.out.println("Proto: #"+packet.getID()+" "+this.getName());
		try {
			return packet.dup(
					_parser.translatePacket(packet.getPayload(), packet.getSourceAddress().getAddress())
					);
		} catch (ParseException e) {
			throw new WSNProtocolException("TinyIPFIX Protocol Exception", e);
		}
	}

	protected TinyIPFIXParser _parser = new TinyIPFIXParser();
}
