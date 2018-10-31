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
package gov.pnnl.cat.ui.rcp.exceptionhandling;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.apache.log4j.Logger;

/**
 */
public class ExceptionHandler {
  protected static Logger logger = CatLogger.getLogger(ExceptionHandler.class);
  // Titles
  public static String TITLE_CLOSING_APPLICATION = "Exception - Application Closing.";

  // Messages
  public static String EXCEPTION_FAILED_SERVER_CONNECTION = "Failed connection to the CAT server.  Confirm server status";

  public static String EXCEPTION_GENERAL_RESOURCE_EXCEPTION = "Failed to get resource from the CAT server.  Confirm server status";

  // our plan was to pass in an exception and then determine what the message should be based on what
  // type of exception was thrown...
  //  
  // public static boolean GeneralException( final Control control, Exception ex, final String errorTitle, final String errorMsg) {
  // boolean success = true;
  // System.out.println("Within our CAT exception handler.");
  // if(ex.getCause() instanceof )
  //    
  // ErrorDialogs.openErrorMessage(control, errorTitle, errorMsg);
  // return success ;
  // }

  /**
   * Displays a dialog box explaining the error that occurred (passed) and optionally makes an entry in the log file for the system. Then terminates the program.
   * 
   * @param ex
   *          Exception The original exception that cause this fatal exception.
   * @param errorTitle
   *          String Title used in the dialog box to the end user.
   * @param errorMsg
   *          String Message to be displayed in the dialog.
   * @param logError
   *          true This method records an entry in the EZLogger using the errorMsg param.
   */
  public static void FatalException(Exception ex, String errorTitle, String errorMsg, boolean logError) {

    // Test if the user wants us to log an entry into the EZLogger.
    if (logError) {
      //EZLogger.logError(ex, "Fatal Exception..." + errorMsg);
      logger.fatal("Fatal Exception..." + errorMsg, ex);
      errorMsg += "\n\nAdditional error information can be found within the log file.";
    }

    ToolErrorHandler.handleError(errorTitle + " " + errorMsg, ex, true);

  }
}
