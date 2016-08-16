package de.tum.in.net.WSNDataFramework.Crypto;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.HashMap;

public class SessionHandler {
	private static SessionHandler instance = null;
	
	private HashMap<String, MoteParameters> motes = new HashMap<>();
	
	protected SessionHandler() {
		// avoid instantiation from outside
	}
	
	
	private static SessionHandler getInstance() {
	      if(instance == null) {
	         instance = new SessionHandler();
	      }
	      return instance;
	   }
	
	public static void addSender(InetAddress sender){
		if(getInstance().motes.containsKey(sender.getHostAddress())) return;
		getInstance().motes.put(sender.getHostAddress(), new MoteParameters());
	}
	
	public static MoteParameters getMoteParameters(InetAddress sender){
		if(! getInstance().motes.containsKey(sender.getHostAddress())) addSender(sender);
		return getInstance().motes.get(sender.getHostAddress());
	}
}
