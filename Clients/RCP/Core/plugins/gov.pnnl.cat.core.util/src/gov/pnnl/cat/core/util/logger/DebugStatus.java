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
package gov.pnnl.cat.core.util.logger;

import java.util.Hashtable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * Use this class for holding log messages for debug.  This class
 * represents a Status object with a severity of INFO.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class DebugStatus extends Status implements IDebugStatus {

   private int mDebugLevel;
   
   /*
    * To support quick mapping from int level to string label and vice versa
    */
   private static Hashtable mLabelMap;
   private static Hashtable mCodeMap;
   static {
      mLabelMap = new Hashtable();
      mLabelMap.put(new Integer(IDebugStatus.NORMAL), "NORMAL");
      mLabelMap.put(new Integer(IDebugStatus.FINE), "DEBUG-FINE");
      mLabelMap.put(new Integer(IDebugStatus.FINER), "DEBUG-FINER");
      mLabelMap.put(new Integer(IDebugStatus.FINEST), "DEBUG-FINEST");
      
      mCodeMap = new Hashtable();
      mCodeMap.put("NORMAL", new Integer(IDebugStatus.NORMAL));
      mCodeMap.put("DEBUG-FINE", new Integer(IDebugStatus.FINE));
      mCodeMap.put("DEBUG-FINER", new Integer(IDebugStatus.FINER));
      mCodeMap.put("DEBUG-FINEST", new Integer(IDebugStatus.FINEST));   
   }  
   
   /**
    * Constructor for creating a IDebugStatus object
    * @param debugLevel (FINE, FINER, or FINEST)
    * @param pluginId 
    * @param pluginCode (if your plugin wants to set its own code)
    * @param message  (the message you want to log)
    */
   public DebugStatus(int debugLevel, String pluginId, int pluginCode, String message) {
      super(IStatus.INFO, pluginId, pluginCode, message, null);
     
      if ( debugLevel >= IDebugStatus.NORMAL && debugLevel <= IDebugStatus.FINEST ) {
         mDebugLevel = debugLevel;
         
      } else {
         //TODO: log some kind of exception!
         
         // We are ignoring this debug because it has an invalid status
         mDebugLevel = IDebugStatus.NORMAL;
      }
   }
   
   /**
    * Quick constructor if you don't want to worry about a plugin code
    * @param debugLevel int
    * @param pluginId String
    * @param message String
    */
   public DebugStatus(int debugLevel, String pluginId, String message) {
      this(debugLevel, pluginId, 0, message);
   }
   
   /**
    * Return the debug level as an int
    * @return int
    * @see gov.pnnl.cat.core.util.logger.IDebugStatus#getDebugLevel()
    */
   public int getDebugLevel() {
     return mDebugLevel;
   }
   
   /**
    * Convert the int debug level to a readable String 
    * @return String
    */
   public String getDebugLevelAsString() {
      return DebugStatus.parseDebugLevel(getDebugLevel());
   }
   
   /**
    * Convert the debug level int value to a string
    * @param debugLevel
    * @return String
    */
   public static String parseDebugLevel(int debugLevel) {
      Integer debugLevelInt = new Integer(debugLevel);     
      String ret = (String)mLabelMap.get(debugLevelInt);
      if( ret == null ){
         // default is NORMAL if we can't find the level
         ret = (String)mLabelMap.get(new Integer(IDebugStatus.NORMAL));
         //TODO: log some kind of exception!
      }
      
      return ret;
   }
 
   /**
    * Convert the debug level String value to an int
    * @param debugLevel String
    * @return int
    */
   public static int parseDebugLevel(String debugLevel) {
      int ret;
      
      Integer debugLevelInt = (Integer)mCodeMap.get(debugLevel);
     
      if( debugLevelInt == null ){
         // default is NORMAL if we can't find the level
         ret = IDebugStatus.NORMAL;
         //TODO: log some kind of exception!
      } else {
            ret = debugLevelInt.intValue();
      }     
      return ret;
   }
}
