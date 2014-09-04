/*
 * Copyright (c) Erasmus MC
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.erasmusmc.jerboa.modules;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JPanel;

import org.erasmusmc.jerboa.userInterface.PickButton;

/**
 * Module for compressing and encrypting files.
 * @author schuemie
 *
 */
public class GiftWrapperModule extends JerboaModule {
	
	public JerboaModule input;
	
	
	/**
	 * Specifies which public key to use. Current options:<BR>
	 * <UL>
	 *   <LI>EU-ADR</LI>
	 *   <LI>SOS</LI>
	 *   <LI>ARITMO</LI>
	 *   <LI>VAESCO</LI>
	 * </UL>
	 * default = EU-ADR
	 */
	public String key = "EU-ADR";
	
  private static final long serialVersionUID = 689071133259105495L;

  protected JPanel createParameterPanel() {
    JPanel panel = super.createParameterPanel();
    PickButton pickButton = new PickButton(PickButton.OPEN, this, "outputFilename");
    panel.add(pickButton);
    return panel;
  }
  
  protected void runModule(String outputFilename){
    Key publicKey; 
    if (key.equals("SOS"))
      publicKey = loadKey(GiftWrapperModule.class.getResourceAsStream("JerboaPublicSOS.key"));
    else if (key.equals("ARITMO"))
      publicKey = loadKey(GiftWrapperModule.class.getResourceAsStream("JerboaPublicARITMO.key"));
    else  if (key.equals("VAESCO"))
      publicKey = loadKey(GiftWrapperModule.class.getResourceAsStream("JerboaPublicVAESCO.key"));
    else
    	publicKey = loadKey(GiftWrapperModule.class.getResourceAsStream("JerboaPublicEU-ADR.key"));
    zipAndEncrypt(input.getResultFilename(), outputFilename, publicKey);
  }

  public static void zipAndEncrypt(String source, String target, Key publicKey){
    try{
      //Step 1: generate random symmetric key (AES algorithm):
      KeyGenerator kgen = KeyGenerator.getInstance("AES");
      kgen.init(128); 
      SecretKey aesKey = kgen.generateKey();

      //Step 2: Create encoding cipher using public key (RSA algorithm):
      Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

      //Step 3: Open file stream: 
      FileOutputStream file = new FileOutputStream(target);

      //Step 4: Encode symmetric key using encoding cipher, and write to file:
      file.write(rsaCipher.doFinal(aesKey.getEncoded()));

      //Step 5: Open encrypted stream using symmetric key (AES algorithm):
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, aesKey);
      CipherOutputStream out = new CipherOutputStream(file, cipher);
      
      //Step 6: Zip and copy file input stream:
      GZIPOutputStream gzipOut = new GZIPOutputStream(out);
      copyStream(new FileInputStream(source), gzipOut);
      gzipOut.close();
    } catch (Exception e){
      e.printStackTrace();
    }
  }
  
  public static Key loadKey(String filename) throws FileNotFoundException {
    FileInputStream binFile = new FileInputStream(filename);
    return loadKey(binFile);
  }
  
  public static Key loadKey(InputStream stream) {
    Key result = null;
    try {
      ObjectInputStream inp = new ObjectInputStream(stream);
      try {
        result = (Key)inp.readObject();   
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
  
  public static void decryptAndUnzip(String source, String target, Key privateKey){
    try {
      //Step 1: Generate cipher using private key (RSA algorithm):
      Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);

      //Step 2: open file:
      FileInputStream textFileStream = new FileInputStream(source);

      //Step 3: read encrypted symmetric key, and decrypt using private key:
      byte[] encKey = new byte[64];
      textFileStream.read(encKey);
      Key aesKey = new SecretKeySpec(rsaCipher.doFinal(encKey), "AES");

      //Step 4: create decryption stream (AES algorithm):
      Cipher aesCipher = Cipher.getInstance("AES");
      aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
      CipherInputStream in = new CipherInputStream(textFileStream, aesCipher);
      
      //Step 5: decrypt and unzip stream:
      copyStream(new GZIPInputStream(in), new FileOutputStream(target));
    } catch (Exception e){
      e.printStackTrace();
    }
  }
  
  private static final void copyStream(InputStream source, OutputStream dest){
    int bufferSize = 1024;
    int bytes;
    byte[] buffer;
    buffer = new byte[bufferSize];
    try {  
      while ((bytes = source.read(buffer)) != -1) {
        if (bytes == 0) {
          bytes = source.read();
          if (bytes < 0)
            break;
          dest.write(bytes);
          dest.flush();
          continue;
        }
        dest.write(buffer, 0, bytes);
        dest.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
    	try {
				source.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    }
  }
  
}
