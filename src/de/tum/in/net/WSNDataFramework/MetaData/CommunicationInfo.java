package de.tum.in.net.WSNDataFramework.MetaData;

import java.util.List;

/**
 * WSN Meta-Data about the Communcation
 * 
 * @author André Freitag
 */
public class CommunicationInfo extends WSNMetaData {
	public int packetsSent;
	public int bytesSent;
	public List<String> protocols;
}
