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
/**
 * 
 */
package gov.pnl.ui.utils;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * We copied this from Eclipse because we want to use it, but it's in an internal package, so 
 * there's no guarantee it won't go away with the next version.
 *
 * @version $Revision: 1.0 $
 */
public class StatusUtil {
  
  private static Logger logger = Logger.getLogger(StatusUtil.class.getName());
  
  /**
   *  Answer a flat collection of the passed status and its recursive children
   * @param aStatus IStatus
   * @return List
   */
  protected static List flatten(IStatus aStatus) {
    List result = new ArrayList();

    if (aStatus.isMultiStatus()) {
      IStatus[] children = aStatus.getChildren();
      for (int i = 0; i < children.length; i++) {
        IStatus currentChild = children[i];
        if (currentChild.isMultiStatus()) {
          Iterator childStatiiEnum = flatten(currentChild).iterator();
          while (childStatiiEnum.hasNext()) {
            result.add(childStatiiEnum.next());
          }
        } else {
          result.add(currentChild);
        }
      }
    } else {
      result.add(aStatus);
    }

    return result;
  }

  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for creating status.
   * @param stati IStatus[]
   * @param message String
   * @param exception Throwable
   * @return IStatus
   */
  protected static IStatus newStatus(IStatus[] stati, String message,
      Throwable exception) {

    Assert.isTrue(message != null);
    Assert.isTrue(message.trim().length() != 0);

    return new MultiStatus(UtilsActivator.PLUGIN_ID, IStatus.ERROR,
        stati, message, exception);
  }

  /**
   * Method newStatus.
   * @param pluginId String
   * @param exception Throwable
   * @return IStatus
   */
  public static IStatus newStatus(String pluginId, Throwable exception) {
    return newStatus(pluginId, getLocalizedMessage(exception), exception);
  }

  /**
   * Returns a localized message describing the given exception. If the given exception does not
   * have a localized message, this returns the string "An error occurred".
   *
   * @param exception
  
   * @return String
   */
  public static String getLocalizedMessage(Throwable exception) {
    String message = exception.getLocalizedMessage();

    if (message != null) {
      return message;
    }

    // Workaround for the fact that CoreException does not implement a getLocalizedMessage() method.
    // Remove this branch when and if CoreException implements getLocalizedMessage() 
    if (exception instanceof CoreException) {
      CoreException ce = (CoreException)exception;
      return ce.getStatus().getMessage();
    }

    return "An error occurred";
  }

  /**
   * Creates a new Status based on the original status, but with a different message
   *
   * @param originalStatus
   * @param newMessage
  
   * @return IStatus
   */
  public static IStatus newStatus(IStatus originalStatus, String newMessage) {
    return new Status(originalStatus.getSeverity(), 
        originalStatus.getPlugin(), originalStatus.getCode(), newMessage, originalStatus.getException());
  }

  /**
   * Method newStatus.
   * @param pluginId String
   * @param message String
   * @param exception Throwable
   * @return IStatus
   */
  public static IStatus newStatus(String pluginId, String message, Throwable exception) {        
    return new Status(IStatus.ERROR, pluginId, IStatus.OK, 
        message, getCause(exception));
  }    

  /**
   * Method getCause.
   * @param exception Throwable
   * @return Throwable
   */
  public static Throwable getCause(Throwable exception) {
    // Figure out which exception should actually be logged -- if the given exception is
    // a wrapper, unwrap it
    Throwable cause = null;
    if (exception != null) {
      if (exception instanceof CoreException) {
        // Workaround: CoreException contains a cause, but does not actually implement getCause(). 
        // If we get a CoreException, we need to manually unpack the cause. Otherwise, use
        // the general-purpose mechanism. Remove this branch if CoreException ever implements
        // a correct getCause() method.
        CoreException ce = (CoreException)exception;
        cause = ce.getStatus().getException();
      } else {
        // Get the root cause
        cause = getRootCause(exception);
      }
    }

    return cause;
  }

  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for creating status.
   * @param severity int
   * @param message String
   * @param exception Throwable
   * @return IStatus
   */
  public static IStatus newStatus(int severity, String message,
      Throwable exception) {

    String statusMessage = message;
    if (message == null || message.trim().length() == 0) {
      if (exception.getMessage() == null) {
        statusMessage = exception.toString();
      } else {
        statusMessage = exception.getMessage();
      }
    }

    return new Status(severity, UtilsActivator.PLUGIN_ID, severity,
        statusMessage, getCause(exception));
  }

  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for creating status.
   * @param children List
   * @param message String
   * @param exception Throwable
   * @return IStatus
   */
  public static IStatus newStatus(List children, String message,
      Throwable exception) {

    List flatStatusCollection = new ArrayList();
    Iterator iter = children.iterator();
    while (iter.hasNext()) {
      IStatus currentStatus = (IStatus) iter.next();
      Iterator childrenIter = flatten(currentStatus).iterator();
      while (childrenIter.hasNext()) {
        flatStatusCollection.add(childrenIter.next());
      }
    }

    IStatus[] stati = new IStatus[flatStatusCollection.size()];
    flatStatusCollection.toArray(stati);
    return newStatus(stati, message, exception);
  }

  /**
   * Method logMessage.
   * @param status IStatus
   * @param e Throwable
   */
  public static void logMessage(IStatus status, Throwable e) {
    Level loggerLevel = Level.WARNING;
    String errorMsg = "";
    Throwable exception = e;
    
    if(status != null) {
      int severity = status.getSeverity();
      switch(severity) {
        case IStatus.ERROR   : loggerLevel = Level.SEVERE; break;
        case IStatus.INFO    : 
        case IStatus.OK      : loggerLevel = Level.INFO; break; 
      }
      errorMsg = status.getMessage();
      if(e == null) {
        exception = status.getException();
      }
    }
    
    if (exception != null) {
      logger.log(loggerLevel, errorMsg, exception);
    } else {
      logger.log(loggerLevel, errorMsg);
    }
  }
  
  /**
   * Method logMessage.
   * @param e Throwable
   */
  public static void logMessage(Throwable e) {
    logMessage(null, e);
  }
  
  /**
   * Method logMessage.
   * @param status IStatus
   */
  public static void logMessage(IStatus status) {
    logMessage(status, null);
  }
  
  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for handling status.
   * @param status IStatus
   * @param hint int
   * @param shell Shell
   */
  public static void handleStatus(IStatus status, int hint, Shell shell) {
    logMessage(status);
    StatusManager.getManager().handle(status, hint);
  }

  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for handling status.
   * @param e Throwable
   * @param hint int
   */
  public static void handleStatus(Throwable e, int hint) {
    IStatus newStatus = newStatus(UtilsActivator.PLUGIN_ID, e);
    StatusManager.getManager().handle(newStatus, hint);
    logMessage(newStatus, e);
  }

  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for handling status.
   * @param message String
   * @param e Throwable
   * @param hint int
   */
  public static void handleStatus(String message, Throwable e, int hint) {
    IStatus newStatus = newStatus(UtilsActivator.PLUGIN_ID, message, e);
    StatusManager.getManager().handle(newStatus, hint);
    logMessage(newStatus, e);
  }

  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for handling status.
   * @param message String
   * @param e Throwable
   * @param hint int
   * @param shell Shell
   */
  public static void handleStatus(String message, Throwable e, int hint,
      Shell shell) {
    IStatus newStatus = newStatus(UtilsActivator.PLUGIN_ID, message, e);
    StatusManager.getManager().handle(newStatus, hint);
    logMessage(newStatus, e);
  }

  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for handling status.
   * @param status IStatus
   * @param message String
   * @param hint int
   */
  public static void handleStatus(IStatus status, String message, int hint) {
    IStatus newStatus = newStatus(status, message);
    StatusManager.getManager().handle(newStatus, hint);
    logMessage(newStatus);
  }

  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for handling status.
   * @param status IStatus
   * @param message String
   * @param hint int
   * @param shell Shell
   */
  public static void handleStatus(IStatus status, String message, int hint,
      Shell shell) {
    IStatus newStatus = newStatus(status, message);
    StatusManager.getManager().handle(newStatus, hint);
    logMessage(newStatus);
  }

  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for handling status.
   * @param message String
   * @param hint int
   */
  public static void handleStatus(String message, int hint) {
    handleStatus(message, null, hint);
  }

  /**
   * This method must not be called outside the workbench.
   *
   * Utility method for handling status.
   * @param message String
   * @param hint int
   * @param shell Shell
   */
  public static void handleStatus(String message, int hint, Shell shell) {
    handleStatus(message, null, hint);
  }

  /**
   * Log a message to the status bar without popping up a modal error dialog.
   * @param statusBarMessage
   */
  public static void logToStatusBar(final String statusBarMessage) {
    Display.getDefault().asyncExec(new Runnable() {
      /*
       * (non-Javadoc)
       *
       * @see java.lang.Runnable#run()
       */
      public void run() {

        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

        IWorkbenchPage page = win.getActivePage();

        IWorkbenchPart part = page.getActivePart();
        IWorkbenchPartSite site = part.getSite();

        IViewSite vSite = ( IViewSite ) site;

        IActionBars actionBars =  vSite.getActionBars();

        if( actionBars == null )
          return ;

        IStatusLineManager statusLineManager =
          actionBars.getStatusLineManager();

        if( statusLineManager == null )
          return ;

        statusLineManager.setMessage( statusBarMessage );
      }
    });
  }
  
  /**
   * Find the root cause of the exception, so the error message will be more
   * useful.
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
}
