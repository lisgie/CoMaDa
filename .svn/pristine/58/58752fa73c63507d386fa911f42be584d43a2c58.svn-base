package de.tum.in.net.WSNDataFramework.Protocols;

import java.util.List;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.WSNProtocol;
import de.tum.in.net.WSNDataFramework.WSNProtocolException;
import de.tum.in.net.WSNDataFramework.WSNProtocolPacket;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXField;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXParser;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXParser.ParseException;

public class IPFIXProtocol extends WSNProtocol {

	@Override
	public String getName() {
		return "IPFIX Protocol";
	}

	@Override
	public synchronized WSNProtocolPacket process(WSNProtocolPacket packet) throws WSNProtocolException {
		//System.out.println("Proto: #"+packet.getID()+" "+this.getName());

		Map<String,List<IPFIXField>> fields;
		try {
			// parse packet
			fields = _parser.parse(packet.getPayload());
			// append log to packet.info()
			packet.info().append(_parser.getLog());
		} catch (ParseException e) {
			throw new WSNProtocolException("IPFIXProtocol couldn't parse packet", e);
		}

		return fields!=null ? new IPFIXProtocolPacket(fields, packet) : null;
	}

	protected IPFIXParser _parser = new IPFIXParser();
}
