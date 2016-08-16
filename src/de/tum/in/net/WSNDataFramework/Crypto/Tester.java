package de.tum.in.net.WSNDataFramework.Crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
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

public class Tester {
	
	public static final String PUB_X_BS = "ddd019c79f713fe5b17fe3dd44fb99513083ecdeb4518931";
	public static final String PUB_Y_BS = "c3a2e14fe5d8fe4490fdbbd9e9e6c2a623beb7a38b983a72";
	public static final String PRIV_BS = "31f6203eb0d257504d3add0dd0988d466c9092d3699c20";
	//public static final String SEC_BS = "0097747780c2b7d27653375b5ddbdee58b7499aef3f74dec31";
	
	public static final String PUB_X_M = "189e22f7f4040280e6dc92d2eb3dc7bec7e3ab039d774923";
	public static final String PUB_Y_M = "433437dd691c812dc95f6869ff88b180549df475d7649b74";
	public static final String PRIV_M = "4bc01c2837dc222628dba5a3e31fc04dd1ea513d99527561";
	//public static final String SEC_M = "97747780c2b7d27653375b5ddbdee58b7499aef3f74dec31";
	
	public static ECParameterSpec ecParams = null;
	
	public static PublicKey pub_BS;
	public static PublicKey pub_M;
	
	public static PrivateKey priv_BS;
	public static PrivateKey priv_M;
	
	public static void main(String[] args) {
		
		Security.addProvider(new BouncyCastleProvider());
		
		ecParams = ECNamedCurveTable.getParameterSpec("secp192k1");
		
		byte[] pub_x_bs = hexStringToByteArray(PUB_X_BS);
		byte[] pub_y_bs = hexStringToByteArray(PUB_Y_BS);
		byte[] priv_bs = hexStringToByteArray(PRIV_BS);
		//byte[] sec_bs = hexStringToByteArray(SEC_BS);
		
		byte[] pub_x_m = hexStringToByteArray(PUB_X_M);
		byte[] pub_y_m = hexStringToByteArray(PUB_Y_M);
		byte[] priv_m = hexStringToByteArray(PRIV_M);
		//byte[] sec_m = hexStringToByteArray(SEC_M);
		
		pub_BS = makePublicKey(pub_x_bs,pub_y_bs);
		pub_M = makePublicKey(pub_x_m,pub_y_m);
		
		priv_BS = makePrivateKey(priv_bs);
		priv_M = makePrivateKey(priv_m);
		
		generateSecret(priv_BS, pub_M);
		generateSecret(priv_M, pub_BS);
		
		byte[] sig_BS = signMsg(priv_BS);
		byte[] sig_M = signMsg(priv_M);
		
		verifyMsg(pub_BS, sig_BS);
		verifyMsg(pub_M, sig_M);
		
//		byte[] r = hexStringToByteArray("3d32dbf0177aaa71b1d29bf04007150dd77e853d709ba2be");
//		byte[] s = hexStringToByteArray("00c72048a72e80bcfffae144ccc76ffab8677f5787771ac10d");
//		byte[] sign_msg = hexStringToByteArray("8e623e76f3db31b7f8049d96aa66fb01e2e1e7fcaccb9220657dd9cbb46209e381fe38ac71febaca2041045acf9caee110be3838c0f4331f20659c444d46e0b0059434816ada5794353ba02090f03faec6689a97b589047a6b62fb7190f7335ffec00000000000000000000000000100");
//		System.out.println(new BigInteger(s));
//		byte[] signature = signMessage(priv_M, sign_msg);
//		System.out.println(HandshakeHandler.byteArrayToHexString(signature));
//		verifySignature(pub_M, sign_msg, r, s);
		
		byte[] payload = hexStringToByteArray("042f00010000058001000200000f018002000200000f018001000200000f0280020004000000018001000200000001");
		byte[] ciphertext = hexStringToByteArray("024777412392e8589043ac4e7cfc8134443d41dd0afaef32c8cbdb8828dbe4fe6db803866620e0ff4393932f81e5ba447a6f5904d67b115f0472bfa0e558dd8fcdc5054053701c68d1984892133df6b8cece18e2c42ca284bfba64ad");
		byte[] blank = new byte[47];
		byte[] enc = encryptECIES(blank, pub_BS);
		out(enc);
		//out(ciphertext);
		byte[] res = decryptECIES(enc, priv_BS);
		out(res);
		
		
		
	}
	
	private static void out(byte[] res) {
		System.out.println(TinyTOMessageHandler.byteArrayToHexString(res));
		
	}

	private static void verifyMsg(PublicKey pub, byte[] sig) {
				
		Signature sigECDSA;
		try {
			sigECDSA = Signature.getInstance("SHA1withECDSA", "BC");
			sigECDSA.initVerify(pub);
			sigECDSA.update(new byte[16]);
			boolean result = sigECDSA.verify(sig);	
			System.out.println(result);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static byte[] signMsg(PrivateKey priv) {
		byte[] signature = null;
		byte[] data = new byte[16];
		try {
			Signature sigECDSA = Signature.getInstance("SHA1withECDSA", "BC");
			sigECDSA.initSign(priv);
			sigECDSA.update(data);
			signature = sigECDSA.sign();

		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return signature;
		
	}

	private static BigInteger generateSecret(PrivateKey priv, PublicKey pub) {
		
		KeyAgreement ecdhU;
		try {
			ecdhU = KeyAgreement.getInstance("ECDH", "BC");
			ecdhU.init(priv);
			ecdhU.doPhase(pub, true);
			BigInteger sharedSec = new BigInteger(ecdhU.generateSecret());
			System.out.println("shared secret: " + TinyTOMessageHandler.byteArrayToHexString(sharedSec.toByteArray()));
			return sharedSec;
			//SessionHandler.getMoteParameters(sender).setSharedSec(sharedSec);

		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
		
		
	}

	private static PublicKey makePublicKey(byte[] x, byte[] y) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
	
			outputStream.write(0x04);
			outputStream.write(x);
			outputStream.write(y);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			byte[] pub = outputStream.toByteArray();	
			

			ECPoint p = ecParams.getCurve().decodePoint(pub);
//			byte[] b = p.getEncoded();
//			out(b);
//			ECPublicKeyParameters bpubKey = (ECPublicKeyParameters)PublicKeyFactory.createKey(pub);

			ECPublicKeySpec sp = new ECPublicKeySpec(p, ecParams);
			
			KeyFactory factory = KeyFactory.getInstance("ECDH", "BC");
			
			PublicKey pkey = factory.generatePublic(sp);
			
//			System.out.println(new BigInteger(x));
//			System.out.println(new BigInteger(y));
//			System.out.println(pkey);
			
			return pkey;
			
			

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
		return null;
		
	}
	
	private static PrivateKey makePrivateKey(byte[] w) {
		
	
		
		try {

			ECPrivateKeySpec sp = new ECPrivateKeySpec(new BigInteger(w), ecParams);
			
			KeyFactory factory = KeyFactory.getInstance("ECDH", "BC");
			

			PrivateKey pkey = factory.generatePrivate(sp);
			
			
			return pkey;
			
			

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
		return null;
		
	}

	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static boolean verifySignature(PublicKey pub, byte[] sign_msg, byte[] r, byte[] s) {
		boolean result = false;
		
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
			Signature sigECDSA = Signature.getInstance("SHA1withECDSA", "FlexiEC");
			sigECDSA.initVerify(pub);
			sigECDSA.update(sign_msg);
			result = sigECDSA.verify(outputStream.toByteArray());			
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(result);
		return result;
	}

	protected static byte[] signMessage(PrivateKey priv, byte[] data){
		byte[] signature = null;
		try {
			Signature sigECDSA = Signature.getInstance("SHA1withECDSA", "FlexiEC");
			sigECDSA.initSign(priv);
			sigECDSA.update(data);
			signature = sigECDSA.sign();

		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return signature;
	}
public static byte[] encryptECIES(byte[] raw_msg, PublicKey pub) {
		
		
		Cipher cipher = null;
		byte [] plaintext = null;
		
		try {
			
			cipher = Cipher.getInstance("ECIES", "BC");
			cipher.init(Cipher.ENCRYPT_MODE, pub);
//			AlgorithmParameters params = cipher.getParameters();
			
			
			plaintext = cipher.doFinal(raw_msg, 0, raw_msg.length);
			System.out.println(plaintext.length);
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return plaintext;
	}

public static byte[] decryptECIES(byte[] raw_msg, PrivateKey priv) {
		
	byte [] plaintext = null;
	int keylength = 0;
	
	switch(raw_msg[0]) {
	case 02 :
	case 03 : keylength = 25; break;
	case 04 : keylength = 49; break;
	}
	
	try {
		
		byte[] ephemeralKey = new byte[keylength];
		System.arraycopy(raw_msg, 0, ephemeralKey, 0, keylength);
	
		int msgLength = raw_msg.length -20 - (keylength -1) -1;
		byte[] ciphertext = new byte[msgLength];
		plaintext = new byte[msgLength];
		
		ECPoint p = ecParams.getCurve().decodePoint(ephemeralKey);
		ECPublicKeySpec sp = new ECPublicKeySpec(p, ecParams);
		
		KeyFactory factory = KeyFactory.getInstance("ECDH", "BC");
		PublicKey pkey = factory.generatePublic(sp);
				
		BigInteger secret = generateSecret(priv, pkey);
		
		SHA1Digest sha = new SHA1Digest();
		
		KDF2BytesGenerator kdf = new KDF2BytesGenerator(sha);
		kdf.init(new KDFParameters(secret.toByteArray(), null));
        
		byte[] k = new byte[msgLength + 20];
		kdf.generateBytes(k, 0, msgLength + 20);
		
		System.arraycopy(raw_msg, keylength, ciphertext, 0, msgLength);
		
		for(int i = 0; i < msgLength; i ++){
			plaintext[i] = (byte) (ciphertext[i] ^ k[i]);
		}
		
		byte[] temp = new byte[20];
		System.arraycopy(k, msgLength, temp, 0, 20);
		
		SecretKey key = new SecretKeySpec(temp, "HmacSHA1");
		
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(key);
		byte[] macarray = mac.doFinal(ciphertext);
		
		
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
	return plaintext;
	}
}
