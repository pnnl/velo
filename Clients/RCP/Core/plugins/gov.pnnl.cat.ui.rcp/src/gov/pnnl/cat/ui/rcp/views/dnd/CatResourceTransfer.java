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
package gov.pnnl.cat.ui.rcp.views.dnd;

import gov.pnnl.cat.logging.CatLogger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * IResource transfer type
 */
public class CatResourceTransfer extends ByteArrayTransfer {

  private static final String CAT_FILE_FOLDER = "catFileFolder";
  private static final int CAT_TYPE_ID = registerType(CAT_FILE_FOLDER);
  private static CatResourceTransfer _instance = new CatResourceTransfer();
  private Logger logger = CatLogger.getLogger(this.getClass());

  /**
   * Method getInstance.
   * @return CatResourceTransfer
   */
  public static CatResourceTransfer getInstance () {
   return _instance;
  }
  
  /**
   * Method javaToNative.
   * @param object Object
   * @param transferData TransferData
   */
  protected void javaToNative (Object object, TransferData transferData) {
logger.debug("javaToNative");
   if (object == null || !(object instanceof String[])) return;
   if (isSupportedType(transferData)) {

     String[] filePaths = (String[]) object;
    // write data to a byte array and then ask super to convert to pMedium
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     PrintWriter pOut = new PrintWriter(new OutputStreamWriter(out));
     for (int i = 0, length = filePaths.length; i < length;  i++){
       String strFile = filePaths[i];       
       pOut.println(strFile);
     }
     pOut.flush();
     pOut.close();
     byte[] buffer = out.toByteArray();

logger.debug("byte[] created");
     super.javaToNative(buffer, transferData);
   }
   
logger.debug("javaToNative Finished");
  }
  
  /**
   * Method nativeToJava.
   * @param transferData TransferData
   * @return Object
   */
  protected Object nativeToJava(TransferData transferData) {
logger.debug("nativeToJava");
    if (isSupportedType(transferData)) {

      byte[] buffer = (byte[]) super.nativeToJava(transferData);
      if (buffer == null)
        return null;

      try {
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(in));

        String strFilePath;
        Vector strFiles = new Vector();
        while ((strFilePath = bufReader.readLine()) != null) {
          strFiles.addElement(strFilePath);
logger.debug("File DND: "+strFilePath);          
        }
        bufReader.close();
        
        String[] strFileArray = new String[strFiles.size()];
        int iCount = 0;
        for (Iterator iter = strFiles.iterator(); iter.hasNext();) {
          strFilePath = (String) iter.next();
          strFileArray[iCount] = strFilePath;
          iCount++;
        }
        
        return strFileArray;
      } catch (IOException ex) {
        logger.error(ex);
        return null;
      }
    }

   return null;
  }
  
  /**
   * Method getTypeNames.
   * @return String[]
   */
  protected String[] getTypeNames(){
   return new String[]{CAT_FILE_FOLDER};
  }
  
  /**
   * Method getTypeIds.
   * @return int[]
   */
  protected int[] getTypeIds(){
   return new int[] {CAT_TYPE_ID};
  }
  }
