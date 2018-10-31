/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.JDKMessageDigest.MD4;

//import cryptix.jce.provider.CryptixCrypto;

/**
 * Run this class to generate an encrypted password that you can add to the
 * alfrescoUserStore.xml file for bootstrapped users.
 * @version $Revision: 1.0 $
 */
public class BootstrapPasswordGenerator {

  static {
    try {
      MessageDigest.getInstance("MD4");
    } catch (NoSuchAlgorithmException e) {
//      Security.addProvider(new CryptixCrypto());
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    System.out.println("Hash: " + new String(Hex.encodeHex(md4(args[0]))));

  }

  /**
   * Method md4.
   * @param input String
   * @return byte[]
   */
  private static byte[] md4(String input) {

    try
    {
      MessageDigest digester = new MD4();
      //MessageDigest digester = MessageDigest.getInstance("MD4");
      return digester.digest(input.getBytes("UnicodeLittleUnmarked"));
    
    } catch (Throwable e) {
      e.printStackTrace();
    }
    
    return null;
  } 

}
