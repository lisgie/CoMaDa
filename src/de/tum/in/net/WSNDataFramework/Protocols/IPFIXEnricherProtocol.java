package de.tum.in.net.WSNDataFramework.Protocols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.WSNException;
import de.tum.in.net.WSNDataFramework.WSNProtocol;
import de.tum.in.net.WSNDataFramework.WSNProtocolException;
import de.tum.in.net.WSNDataFramework.WSNProtocolPacket;
import de.tum.in.net.WSNDataFramework.WSNProtocolUnsupportedPacketException;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXEnrichedField;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXField;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXParser.ParseException;

public class IPFIXEnricherProtocol extends WSNProtocol {

	public IPFIXEnricherProtocol(String pathToMetaXML) throws WSNException {
		try {
			_enricher = new de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXEnricher(pathToMetaXML);
		} catch (Exception e) {
			throw new WSNException("IPFIXEnricherProtocol couldn't create IPFIXEnricher", e);
		}
	}

	@Override
	public String getName() {
		return "IPFIX Enricher";
	}

	@Override
	public synchronized WSNProtocolPacket process(WSNProtocolPacket p) throws WSNProtocolException{
		if (!(p instanceof IPFIXProtocolPacket)) {
			throw new WSNProtocolUnsupportedPacketException();
		}

		IPFIXProtocolPacket packet = (IPFIXProtocolPacket) p;


		//System.out.println("Proto: #"+packet.getID()+" "+this.getName());
		Map<String,List<IPFIXEnrichedField>> enrichedFields = new HashMap<String,List<IPFIXEnrichedField>>();

		for (String nodeID: packet.getFields().keySet()) {
			List<IPFIXField> fields = packet.getFields().get(nodeID);
			enrichedFields.put(nodeID, new ArrayList<IPFIXEnrichedField>(fields.size()));

			
			Node node = this.app().wsn().node(nodeID);
			if (node == null) {
				node = new Node(nodeID, p.getSourceAddress());
				this.app().wsn().addNode(node);
			}
			for (IPFIXField field: fields) {
				try {
					enrichedFields.get(nodeID).add(
							_enricher.enrich(field)
							);
					packet.info().append(_enricher.getLog());
				} catch (ParseException e) {
					throw new WSNProtocolException("IPFIXEnricherProtocol couldn't enrich field", e);
				}
			}
		}


		return new IPFIXEnricherProtocolPacket(enrichedFields, packet);
	}

	protected de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXEnricher _enricher;
}
