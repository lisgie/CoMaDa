package de.tum.in.net.WSNDataFramework.Protocols;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.WSNProtocolPacket;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXField;

public class IPFIXProtocolPacket extends WSNProtocolPacket {

	public IPFIXProtocolPacket(Map<String,List<IPFIXField>> fields, WSNProtocolPacket originalPacket) {
		super(originalPacket);
		_fields = fields!=null ? new HashMap<String,List<IPFIXField>>(fields) : null;
	}

	public Map<String,List<IPFIXField>> getFields() {
		return _fields!=null ? Collections.unmodifiableMap(_fields) : null;
	}

	protected Map<String,List<IPFIXField>> _fields;
}
