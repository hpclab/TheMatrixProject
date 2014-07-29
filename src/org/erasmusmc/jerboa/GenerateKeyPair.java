package org.erasmusmc.jerboa;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Class for generating public and private keys
 * @author schuemie
 *
 */
public class GenerateKeyPair {


  public static void main(String[] args) {
    KeyPair keyPair = generateKeyPair();
    saveKey("x:/sos/JerboaPublicSOS.key", keyPair.getPublic());
    saveKey("x:/sos/JerboaPrivateSOS.key", keyPair.getPrivate());
  }
  
  public static KeyPair generateKeyPair(){
    KeyPair result = null;
    KeyPairGenerator keygen;
    try {
      keygen = KeyPairGenerator.getInstance("RSA");
      keygen.initialize(512);
      result = keygen.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return result;
  }
  
  public static void saveKey(String filename, Key key) {
    try {
      FileOutputStream binFile = new FileOutputStream(filename);
      ObjectOutputStream out=null;
      try {
    	  out = new ObjectOutputStream(binFile);
    	  out.writeObject(key);
    	  out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
