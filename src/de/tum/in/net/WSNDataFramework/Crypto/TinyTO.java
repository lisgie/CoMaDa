package de.tum.in.net.WSNDataFramework.Crypto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.params.KDFParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.jce.spec.IESParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

/**
 * @author Martin Noack
 *
 */
public class TinyTO implements TinyCrypto {
	
	private MessageHandler handshakeHandler = new TinyTOMessageHandler(this);

	// signature algorithm
	Signature sigECDSA;

	// init curve parameters - change here and in motes 
	private static ECParameterSpec ecParams = ECNamedCurveTable.getParameterSpec("secp192k1");
	

	public TinyTO() {
		
		Security.addProvider(new BouncyCastleProvider());

		// init signature algorithm (change mode always here AND in motes)
		try {
	
			sigECDSA = Signature.getInstance("SHA1withECDSA", "BC");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	/* (non-Javadoc)
	 * @see de.tum.in.net.WSNDataFramework.Crypto.TinyCrypto#init(java.net.InetAddress)
	 */
	@Override
	public void init(InetAddress sender) {
		KeyPairGenerator kpg = null;
		
		try {
			kpg = KeyPairGenerator.getInstance("ECIES", "BC");
			kpg.initialize(ecParams, new SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if( kpg == null) {
			System.err.println("Failed to initialize ECC!");
			return;
		}
		
		// key pair generation
		KeyPair kp = kpg.genKeyPair();
		SessionHandler.getMoteParameters(sender).setPrivKeyBS(kp.getPrivate());
		SessionHandler.getMoteParameters(sender).setPubKeyBS(kp.getPublic());	
		
//		System.out.println(kp.getPrivate());
//		System.out.println(kp.getPublic());
	}

	public MessageHandler getMessageHandler() {
		return handshakeHandler;
	}

	/**
	 * Convert the BigInteger representation of the Mote's public key to a Java ECC public key
	 * @param sender 
	 * @param x x-coordinate
	 * @param y y-coordinate
	 */
	protected void storeMotePublicKey(InetAddress sender, BigInteger x, BigInteger y){
			
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
	
			outputStream.write(0x04); // uncompressed
			outputStream.write(x.toByteArray());
			outputStream.write(y.toByteArray());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			byte[] pub = outputStream.toByteArray();	

			ECPoint p = ecParams.getCurve().decodePoint(pub);

			ECPublicKeySpec sp = new ECPublicKeySpec(p, ecParams);
			
			KeyFactory factory = KeyFactory.getInstance("ECDH", "BC");
			
			PublicKey pkey = factory.generatePublic(sp);
			
			SessionHandler.getMoteParameters(sender).setPubKeyMote(pkey);
			

		}  catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	/**
	 * Create the shared secret between mote and Basestation using ECDH.
	 * @param sender 
	 */
	protected void doKeyAgreement(InetAddress sender) {
		
		BigInteger sharedSec = generateSecret(SessionHandler.getMoteParameters(sender).getPrivKeyBS(), SessionHandler.getMoteParameters(sender).getPubKeyMote());
		//System.out.println("shared secret: " + HandshakeHandler.byteArrayToHexString(sharedSec.toByteArray()));
		//SessionHandler.getMoteParameters(sender).setSharedSec(sharedSec);		
	}
	
	/**
	 * Generic ECDH key agreement
	 * @param priv
	 * @param pub
	 * @return
	 */
	private static BigInteger generateSecret(PrivateKey priv, PublicKey pub) {
		
		KeyAgreement ecdhU;
		try {
			ecdhU = KeyAgreement.getInstance("ECDH", "BC");
			ecdhU.init(priv);
			ecdhU.doPhase(pub, true);
			BigInteger sharedSec = new BigInteger(ecdhU.generateSecret());
			return sharedSec;

		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
		
		
	}

	protected byte[] signMessage(InetAddress sender, byte[] data){
		byte[] signature = null;
		try {
			sigECDSA.initSign(SessionHandler.getMoteParameters(sender).getPrivKeyBS());
			sigECDSA.update(data);
			signature = sigECDSA.sign();

		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
		return signature;
	}
	
	/**
	 * Usually, the PKI of Java delivers an encoded byte array according to X.509. However, we need to process the
	 * key with minimal overhead in our motes, so we get the coordinates as simple byte array.
	 * @param pubKey EC public key
	 * @return byte array: | x coordinate | y coordinate |
	 */
	protected byte[] getPubKeyDecoded(PublicKey pubKey){
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

		try {
			byte[] rawKey = pubKey.getEncoded();
			// 0x04 | x | y
			byte[] x = new byte[24];
			System.arraycopy(rawKey, 24, x, 0, 24);
			byte[] y = new byte[24];
			System.arraycopy(rawKey, 48 , y, 0, 24);
			
			outputStream.write(x);
			outputStream.write(y);
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] result = outputStream.toByteArray();

		return result;
		
	}
	
	
	protected boolean verifySignature(InetAddress sender, byte[] sign_msg, byte[] r, byte[] s) {
		boolean result = false;
		
		BigInteger rBi = new BigInteger(1,r);
		r = rBi.toByteArray();
		BigInteger sBi = new BigInteger(1,s);
		s = sBi.toByteArray();
		
		// Java requires ASN.1 encoding for signatures
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		ByteArrayOutputStream r_stream = new ByteArrayOutputStream( );
		ByteArrayOutputStream s_stream = new ByteArrayOutputStream( );

		try {
			outputStream.write(0x30); // sequence
			  r_stream.write(0x02); // integer
			  r_stream.write(r.length);
			  r_stream.write(r);
			  s_stream.write(0x02); // integer
			  s_stream.write(s.length);
			  s_stream.write(s);
			outputStream.write(new Integer(r_stream.size() + s_stream.size()).byteValue());
			outputStream.write(r_stream.toByteArray());
			outputStream.write(s_stream.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			sigECDSA.initVerify(SessionHandler.getMoteParameters(sender).getPubKeyMote());
			sigECDSA.update(sign_msg);
			result = sigECDSA.verify(outputStream.toByteArray());			
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return result;
	}

	

	/* (non-Javadoc)
	 * @see de.tum.in.net.WSNDataFramework.Crypto.TinyCrypto#verifyHash(byte[], byte[])
	 */
	@Override
	public boolean verifyHash(byte[] hash_msg, byte[] digest) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] hash = md.digest(hash_msg);
			return Arrays.equals(hash, digest);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see de.tum.in.net.WSNDataFramework.Crypto.TinyCrypto#hashMessage(byte[])
	 */
	@Override
	public byte[] hashMessage(byte[] byteArray) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] hash = md.digest(byteArray);
			return hash;
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		return new byte[0];
	}

	/**
	 * Decrypt a message encoded with ECIES. 
	 * message format: | ephemeral key | ciphertext | mac |
	 * @param sender sender, used to retrieve the matching private BS key
	 * @param rawMsg full ECIES message
	 * @return
	 * @throws BadPaddingException For invalid MAC
	 */
	public static byte[] decryptECIES(InetSocketAddress sender, byte[] rawMsg) throws BadPaddingException {
		
		// WARNING: TinyECC uses a slightly different algorithm for ECIES than BC or flexiprovider
		// -> therefore we run all the components (SHA1MAC, decryption, key expansion) individually
		// The problem is most likely a different initialization call of the KDF, 
		// probably expanding the key to message length + mac length vs. a multiple of the blocksize
		
		System.out.println("raw: " + TinyTOMessageHandler.byteArrayToHexString(rawMsg));
			
		byte [] plaintext = null;
		int keylength = 0;
		
		// check the first byte for point compression of the ephemeral key
		switch(rawMsg[0]) {
		case 02 :
		case 03 : keylength = 25; break; // compressed key
		case 04 : keylength = 49; break; // uncompressed key
		default: return rawMsg; // not encrypted
		}
		
		if(rawMsg.length < keylength + 20) return null;
		
		try {
			
			// the ephemeral public key is the first component
			byte[] ephemeralKey = new byte[keylength];
			System.arraycopy(rawMsg, 0, ephemeralKey, 0, keylength);
			
			// ciphertext is in between the key and the mac tag
			int textLength = rawMsg.length -20 - (keylength -1) -1;
			byte[] ciphertext = new byte[textLength];
			plaintext = new byte[textLength];
			System.arraycopy(rawMsg, keylength, ciphertext, 0, textLength);
			
			// convert the point coordinates to a point on the curve
			ECPoint p = ecParams.getCurve().decodePoint(ephemeralKey);
			ECPublicKeySpec sp = new ECPublicKeySpec(p, ecParams);
			
			// convert the point to a public key
			KeyFactory factory = KeyFactory.getInstance("ECDH", "BC");
			PublicKey ephKey = factory.generatePublic(sp);
			
			// shared secret from the ephemeral public key and the local private key, valid only for this message
			// sender has used his public key with the ephemeral private key
			BigInteger secret = generateSecret(SessionHandler.getMoteParameters(sender.getAddress()).getPrivKeyBS(), ephKey);
			
			// key expansion to size msg length + mac length in one go, to use the respective part in each function
			SHA1Digest sha = new SHA1Digest();
			KDF2BytesGenerator kdf = new KDF2BytesGenerator(sha);
			kdf.init(new KDFParameters(secret.toByteArray(), null));
	        
			byte[] k = new byte[textLength + 20];
			kdf.generateBytes(k, 0, textLength + 20);
			
			// decode the ciphertext with a simple XOR
			for(int i = 0; i < textLength; i ++){
				plaintext[i] = (byte) (ciphertext[i] ^ k[i]);
			}
			
			// generate mac tag, where the secret is the last 20 bytes of the expanded key from above
			byte[] temp = new byte[20];
			System.arraycopy(k, textLength, temp, 0, 20);
			
			SecretKey key = new SecretKeySpec(temp, "HmacSHA1");
			
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(key);
			byte[] macArray = mac.doFinal(ciphertext);
			
			// check the mac tag byte-wise
			for (int i = 0; i< macArray.length; i++){
				if(macArray[i] != rawMsg[keylength + textLength + i]) {
					System.out.println("Invalid MAC in message from " + sender.getAddress().getHostAddress() + "!");
					throw new BadPaddingException("Invalid MAC!");
				}
			}
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataLengthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		System.out.println("plain: " + TinyTOMessageHandler.byteArrayToHexString(plaintext));
		return plaintext;
	
	}
	
	public static byte[] encryptECIES(InetSocketAddress sender, byte[] packet) {
		
		try {
			BigInteger N = ecParams.getN();
			BigInteger r = new BigInteger(160,  new Random()).mod(N.subtract(BigInteger.ONE));
			ECPoint K = ecParams.getCurve().getMultiplier().multiply(ecParams.getG(), r);
			
			
			ECPrivateKeySpec sp = new ECPrivateKeySpec(r, ecParams);
			
			// convert the point to a public key
			KeyFactory factory = KeyFactory.getInstance("ECDH", "BC");
			PrivateKey rPrivate = factory.generatePrivate(sp);
			
			
			//get private key from r!
			BigInteger secret = generateSecret(rPrivate, SessionHandler.getMoteParameters(sender.getAddress()).getPubKeyBS());
			
			SHA1Digest sha = new SHA1Digest();
			KDF2BytesGenerator kdf = new KDF2BytesGenerator(sha);
			kdf.init(new KDFParameters(secret.toByteArray(), null));
			
			byte[] k = new byte[packet.length + 20];
			kdf.generateBytes(k, 0, packet.length + 20);
			byte[] ciphertext = new byte[packet.length];
			
			// decode the ciphertext with a simple XOR
			for(int i = 0; i < packet.length; i ++){
				ciphertext[i] = (byte) (packet[i] ^ k[i]);
			}
			
			//MAC
			byte[] temp = new byte[20];
			System.arraycopy(k, packet.length, temp, 0, 20);
			
			SecretKey key = new SecretKeySpec(temp, "HmacSHA1");
			
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(key);
	
			byte[] macArray = mac.doFinal(packet);
			
			
			byte[] ephemeralKey = K.getEncoded();
			byte[] response = new byte[ephemeralKey.length + ciphertext.length + macArray.length];
			System.arraycopy(ephemeralKey, 0, response, 0, ephemeralKey.length);
			System.arraycopy(ciphertext, 0, response, ephemeralKey.length, ciphertext.length);
			System.arraycopy(macArray, 0, response, ephemeralKey.length + ciphertext.length, macArray.length);
			return response;
		
		} catch (NoSuchAlgorithmException e){
			e.printStackTrace();
		} catch (NoSuchProviderException e){
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return null;
	}	
}
