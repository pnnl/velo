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
package gov.pnnl.cat.server.webservice.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Utilities to help make error reporting easier to the remote client.
 * @version $Revision: 1.0 $
 */
public class ExceptionUtils {

  /**
   * Find the root cause of the exception, so we can send a better error message
   * back to the remote client.
   * @param e
  
   * @return Throwable
   */
  public static Throwable getRootCause(Throwable e) {
    Throwable rootCause;
    Throwable cause = e;
    do {
      rootCause = cause;
      cause = cause.getCause();
    } while(cause != null);
    
    return rootCause;
  }

  /**
   * Gets the full stack trace of the exception as a string, so better information
   * can be returned to the client.
   * @param e
  
   * @return the stack trace as a String */
  public static String getStackTrace(Throwable e)
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream p = new PrintStream(baos);
    e.printStackTrace(p);
    return baos.toString();
  }
  
}
