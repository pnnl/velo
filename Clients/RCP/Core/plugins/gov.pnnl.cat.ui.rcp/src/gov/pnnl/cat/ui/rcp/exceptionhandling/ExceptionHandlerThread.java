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

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;

/**
 */
public class ExceptionHandlerThread {
  private SortedMap<CmsPath, List<Throwable>> errorMap = Collections.synchronizedSortedMap(new TreeMap<CmsPath, List<Throwable>>());

  private static ExceptionHandlerThread singleton;

  private static Logger logger = CatLogger.getLogger(ExceptionHandlerThread.class);

  private UpdatingErrorDialog errorDialog;

  private List<ErrorListener> listeners = Collections.synchronizedList(new ArrayList<ErrorListener>());

  protected ExceptionHandlerThread() {
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        errorDialog = new UpdatingErrorDialog(Display.getCurrent().getActiveShell(), ExceptionHandlerThread.this);
      }
    });
  }

  /**
   * Method getInstance.
   * @return ExceptionHandlerThread
   */
  public static synchronized ExceptionHandlerThread getInstance() {
    if (singleton == null) {
      singleton = new ExceptionHandlerThread();
    }
    return singleton;
  }

  /**
   * Method addError.
   * @param path CmsPath
   * @param error Throwable
   */
  public void addError(CmsPath path, Throwable error) {

    synchronized (errorMap) {
      List<Throwable> errors = errorMap.get(path);

      if (errors == null) {
        errors = new ArrayList<Throwable>();
        errorMap.put(path, errors);
      }

      errors.add(error);
    }

    IStatus status = new Status(IStatus.ERROR, CatRcpPlugin.PLUGIN_ID, 0, "An error occurred loading a resource: " + path, error);
    ErrorEvent event = new ErrorEvent(path, status);

    notifyListeners(event);

    if (shouldOpenDialog()) {
      openDialog(event);
    }
  }

  /**
   * Method shouldOpenDialog.
   * @return boolean
   */
  private boolean shouldOpenDialog() {
    return errorDialog.getShell() == null || errorDialog.getShell().isDisposed();
  }

  /**
   * Method openDialog.
   * @param error ErrorEvent
   */
  private void openDialog(final ErrorEvent error) {
    logger.debug("Opening the dialog");
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        errorDialog.open(error);
      }
    });
  }

  /**
   * Method addErrorListener.
   * @param listener ErrorListener
   */
  public void addErrorListener(ErrorListener listener) {
    listeners.add(listener);
  }

  /**
   * Method removeErrorListener.
   * @param listener ErrorListener
   * @return boolean
   */
  public boolean removeErrorListener(ErrorListener listener) {
    return listeners.remove(listener);
  }

  /**
   * Method notifyListeners.
   * @param event ErrorEvent
   */
  private void notifyListeners(ErrorEvent event) {
    List<ErrorListener> listenersCopy = new ArrayList<ErrorListener>(listeners);

    for (ErrorListener listener : listenersCopy) {
      listener.errorReceived(event);
    }
  }
}
