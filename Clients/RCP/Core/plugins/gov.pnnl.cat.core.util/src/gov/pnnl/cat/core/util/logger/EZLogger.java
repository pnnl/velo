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

import gov.pnnl.cat.core.util.UtilPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class provides quick static methods for logging to the debug log
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class EZLogger {

  // some arbitray code - for EZ logging, we don't care what it is
  public static final int code = 1;
  
  // for EZ logging, just use the UtilPlugin's log
  public static final String pluginID = UtilPlugin.getPluginId();
  
  /**
   * Method logError.
   * @param e Exception
   * @param message String
   */
  public static void logError(Exception e, String message) {
 
    int severity = IStatus.ERROR;  
    IStatus status = new Status(severity, pluginID, code, message, e);         
    UtilPlugin.getDefault().getLog().log(status);       
  }
  
  /**
   * Method logMessage.
   * @param message String
   */
  public static void logMessage(String message) {

    int severity = IStatus.INFO;
    IStatus status = new Status(severity, pluginID, code, message, null);         
    UtilPlugin.getDefault().getLog().log(status);       
   
  }
  
  /**
   * Method logTraceMessage.
   * @param traceMessage String
   * @param debugLevel int
   */
  public static void logTraceMessage(String traceMessage, int debugLevel) {
 
    IDebugStatus debugStatus = 
          new DebugStatus(debugLevel,pluginID,traceMessage);       
    UtilPlugin.getDefault().getLog().log(debugStatus);    
  }

  /*
   * @param e Exception can be null
   */
  /**
   * Method logWarning.
   * @param message String
   * @param e Exception
   */
  public static void logWarning (String message, Exception e) {        
    int severity = IStatus.WARNING;  
    IStatus status = new Status(severity, pluginID, code, message, e);         
    UtilPlugin.getDefault().getLog().log(status);      
  }
  
}
