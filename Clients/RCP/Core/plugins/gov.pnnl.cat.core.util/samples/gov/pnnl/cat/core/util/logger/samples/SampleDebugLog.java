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
package gov.pnnl.cat.core.util.logger.samples;

import gov.pnnl.cat.core.util.UtilPlugin;
import gov.pnnl.cat.core.util.logger.DebugStatus;
import gov.pnnl.cat.core.util.logger.IDebugStatus;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * This class shows how to use the Eclipse IStatus/Status and Log classes
 * and the CUE IDebugStatus/DebugStatus classes
 * to send messages to the platform log ($WORKSPACE/.metadata/.log)
 * 
 * @author d3k339
 * @version $Revision: 1.0 $
 */
public class SampleDebugLog {

   public SampleDebugLog() {
     
   }

   /**
    * Use the regular IStatus interface to log an exception with a 
    * message.
    *
    */
   public void logSampleError()
   {
      // Test logging an exception
      try {
         throw new Exception ("this is a test");
         
      } catch (Exception e){

         // can also be INFO, WARNING, etc.
         int severity = IStatus.ERROR;
         
         // Can use ANY plugin, doesn't have to be your own
         // Get the ID from the singleton instance of each plugin
         String pluginID = UtilPlugin.getPluginId();
         
         // Can look at Eclipse's Messages class for canned messages
         String message = "This is a test error message";
         
         // This code can be anything you want.  Eclipse's IResourceStatus
         // class defines a bunch of canned codes it uses for Workspace
         // errors.
         int code = 1;
         
         // Log an error to the Platform Log (via a plugin log)
         IStatus status = new Status(severity, pluginID, code, message, e);         
         
         // Can use ANY plugin's log
         // NOTE that all Plugin Logs will automatically log to the Platform
         // Log.  NO listeners are currently running for Plugin Logs, only
         // the Platform Log.  If you want to do something special if your
         // plugin logs an error, then you have to add a listener to your
         // plugin's log
         UtilPlugin.getDefault().getLog().log(status);
      }
      
   }

   /**
    * Use CUE's IDebugStatus interface to log a debug message.  Debug
    * levels can be FINE, FINER, or FINEST (like java.util.logging).
    * Note that we are currently not filtering the Platform Log for these
    * values, so IDebugStatus acts exactly the same as IStatus.  Later, we
    * can filter out the debug messages if we need to, so if you have a
    * trace level debug message you want to display, use this class instead.
    *
    */
   public void logDebugMessage() {
      
      String traceMessage = "I like to kiss Bucket.";
      int debugLevel = IDebugStatus.FINEST; // Very few need to know this :)
      String pluginId = UtilPlugin.getPluginId();
            
      // Log debug message to the LoggerPlugin's log (and hence the Platform
      // Log).
      IDebugStatus debugStatus = 
            new DebugStatus(debugLevel,pluginId,traceMessage);
         
      UtilPlugin.getDefault().getLog().log(debugStatus);

   }
   
}
