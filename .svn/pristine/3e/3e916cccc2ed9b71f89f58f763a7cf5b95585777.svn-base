package de.tum.in.net.WSNDataFramework.Crypto;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class KeyStore {
  private Preferences prefs;
  private Properties prop;
  private static KeyStore instance = null;
  private static byte[] DEFAULT_MASTERKEY = new byte[16];

  protected KeyStore() {
    
    prop = new Properties();
    InputStreamReader inStream;
	try {
		inStream = new InputStreamReader(getClass().getResourceAsStream("/keystore.properties"));
		prop.load(inStream);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   
	
  }
  

private static KeyStore getInstance() {
      if(instance == null) {
         instance = new KeyStore();
      }
      return instance;
   }

	public static byte[] getKey(String sender) {
		byte[] byteArray = new byte[16];
		String key = getInstance().prop.getProperty(sender, TinyTOMessageHandler.byteArrayToHexString(DEFAULT_MASTERKEY));
		try {
			byteArray = Hex.decodeHex(key.toCharArray());
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return byteArray;
	}

	public static void storeKey(String sender, String masterkey) {
		  OutputStream output = null;
		try {
			output = new FileOutputStream("conf/keystore.properties");
			getInstance().prop.setProperty(sender, masterkey);
			getInstance().prop.store(output, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	
  }
} 
