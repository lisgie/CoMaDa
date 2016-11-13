package de.tum.in.net.WSNDataFramework.Crypto;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;


public class MoteParameters {
	
	private PublicKey pubKeyMote;
	private BigInteger sharedSec;
	
	private PrivateKey privKeyBS;
	private PublicKey pubKeyBS;
	
	private boolean isAuthenticated = false;
	
	private MoteType moteType = MoteType.UNDEFINED;


	public PublicKey getPubKeyMote() {
		return pubKeyMote;
	}

	protected void setPubKeyMote(PublicKey pubKeyMote) {
		this.pubKeyMote = pubKeyMote;
	}

	protected BigInteger getSharedSec() {
		return sharedSec;
	}

	protected void setSharedSec(BigInteger sharedSec) {
		this.sharedSec = sharedSec;
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	protected void setAuthenticated(boolean isAuthenticated) {
		this.isAuthenticated = isAuthenticated;
	}

	public PublicKey getPubKeyBS() {
		return pubKeyBS;
	}

	protected void setPubKeyBS(PublicKey pubKeyBS) {
		this.pubKeyBS = pubKeyBS;
	}

	protected PrivateKey getPrivKeyBS() {
		return privKeyBS;
	}

	protected void setPrivKeyBS(PrivateKey privKeyBS) {
		this.privKeyBS = privKeyBS;
	}

	public MoteType getMoteType() {
		return moteType;
	}

	public void setMoteType(MoteType moteType) {
		this.moteType = moteType;
	}


}
