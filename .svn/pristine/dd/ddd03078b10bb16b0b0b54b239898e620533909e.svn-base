package de.tum.in.net.WSNDataFramework.Protocols;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.WSNProtocolPacket;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXEnrichedField;

public class IPFIXEnricherProtocolPacket extends WSNProtocolPacket {

	public IPFIXEnricherProtocolPacket(Map<String,List<IPFIXEnrichedField>> fields, WSNProtocolPacket originalPacket) {
		super(originalPacket);
		_fields = fields!=null ? new HashMap<String,List<IPFIXEnrichedField>>(fields) : null;
	}

	public Map<String,List<IPFIXEnrichedField>> getFields() {
		return _fields!=null ? Collections.unmodifiableMap(_fields) : null;
	}

	protected Map<String,List<IPFIXEnrichedField>> _fields;
}
