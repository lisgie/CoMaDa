package de.tum.in.net.WSNDataFramework.Crypto;

import java.net.InetAddress;

public interface TinyCrypto {

	/**
	 * Generate the key pair of the basestation for a specific mote
	 */
	public abstract void init(InetAddress sender);

	public abstract boolean verifyHash(byte[] hash_msg, byte[] digest);

	public abstract byte[] hashMessage(byte[] byteArray);

	public abstract MessageHandler getMessageHandler();

}