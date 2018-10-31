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
package gov.pnnl.cat.internal.logging;

import gov.pnnl.cat.logging.CatLogger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 */
public class CatLogListener implements ILogListener {

  /**
   * Creates a new PluginLogListener. Saves the plug-in log and logger instance.
   * Adds itself to the plug-in log.
  
  
   */
  public CatLogListener() {
    Platform.addLogListener(this);
    CatLogger.getLogger(this.getClass()).info("CAT Logger now listening to Eclipse log.");
  }

  /**
   * Removes itself from the plug-in log, reset instance variables.
   */ 
  public void dispose() {
    Platform.removeLogListener(this);
  }

  /**
   * Log event happened.
   * Translates status instance to Logger level and message.
   * Status.ERROR - Level.ERROR
   * Status.WARNING - Level.WARN
   * Status.INFO - Level.INFO
   * Status.CANCEL - Level.FATAL
   * default - Level.DEBUG
   * @param status Log Status
   * @param plugin plug-in id
   * @see org.eclipse.core.runtime.ILogListener#logging(IStatus, String)
   */ 
  public void logging(IStatus status, String plugin) {
    if (null == status) 
      return;

    int severity = status.getSeverity();
    Level level = Level.DEBUG;  
    if (severity == Status.ERROR) {
      level = Level.ERROR;
    } else if (severity == Status.WARNING) {
      level = Level.WARN;
    } else if (severity == Status.INFO) {
      level = Level.INFO;
    } else if (severity == Status.CANCEL) {
      level = Level.FATAL;
    }

    plugin = formatText(plugin);
    String pluginName = formatText(status.getPlugin());
    String statusMessage = formatText(status.getMessage());

    Logger.getLogger(pluginName).log(level, statusMessage, status.getException());    
  }

  /**
   * Method formatText.
   * @param text String
   * @return String
   */
  private static String formatText(String text) {
    if (text != null) {
      text = text.trim();
      if (text.length() == 0) return null;
    } 
    return text;
  }
}
