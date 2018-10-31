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

import org.eclipse.core.runtime.IStatus;

/**
 * The Eclipse Platform Log defines the following severity codes in its IStatus
 * interface:
 * OK
 * CANCEL
 * INFO
 * WARNING
 * ERROR
 * 
 * Since it doesn't give different granularities on INFO, I have added a few 
 * granularity levels to supplement the INFO status.  These are meant to provide
 * additional debug information to the Log, which can be filtered out.
 * FINE
 * FINER
 * FINEST
 * 
 * These granularities do not have to be used.  If ommitted by using a normal IStatus
 * object, it will be assumed that it is just a normal, non-debug status log that 
 * should be shared with users at deployment.
 * 
 * Only use an IDebugStatus to provide detailed information when debugging your 
 * plugin.
 * 
   IDebugStatus debug = new DebugStatus(
        YourPlugin.getDefault().getBundle().getSymbolicName(), // pluginId
        IDebugStatus.FINE, //debug level
        "test message", //message (look at Messages class for canned messages)
   );
 * 
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public interface IDebugStatus extends IStatus {

   /*
    * Eclipse uses a bit mask for their severity codes - not sure why since a status
    * can only have one severity.  I don't see the need for a bit mask here for the
    * debug levels.
    */
   public final static int NORMAL = 0;  // no debugging
   public final static int FINE = 1; 
   public final static int FINER = 2;
   public final static int FINEST = 3;
   
  
  /**
   * Returns the debug level assigned to this status
   * @return int
   */
   public int getDebugLevel();
   
}
