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
