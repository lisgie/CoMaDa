package de.tum.in.net.WSNDataFramework.MetaData;

import java.util.Map;

import de.tum.in.net.WSNDataFramework.Modules.IPFIX.IPFIXTemplate;

/**
 * WSN Meta-Data regarding IPFIX
 * 
 * @author André Freitag
 */
public class IPFIXCommunicationInfo extends CommunicationInfo {
	public int templatesSent;
	public int packetsSent;
	public Map<String,IPFIXTemplate> currentTemplates;
}
