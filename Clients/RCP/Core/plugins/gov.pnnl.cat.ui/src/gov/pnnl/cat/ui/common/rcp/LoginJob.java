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
package gov.pnnl.cat.ui.common.rcp;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;

import java.net.ConnectException;
import java.net.UnknownHostException;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A job for logging in.
 * @version $Revision: 1.0 $
 */
public class LoginJob extends Job {
  private Exception error;
  private String username;
  private String password;
  private boolean loggedIn = false;
  private static final Logger logger = CatLogger.getLogger(LoginJob.class);

  public LoginJob() {
    super("Logging in");
  }

  /**
   * Method run.
   * @param monitor IProgressMonitor
   * @return IStatus
   */
  public IStatus run(IProgressMonitor monitor) {
    try {
      ResourcesPlugin.getSecurityManager().login(username, password);
      loggedIn = true;
    } catch (Exception e) {
      logger.error("Failed to log in to server.", e);
      this.error = e;
      loggedIn = false;
    }

    return Status.OK_STATUS;
  }

  /**
   * Method setUsername.
   * @param username String
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Method getUsername.
   * @return String
   */
  public String getUsername() {
    return username;
  }

  /**
   * Method setPassword.
   * @param password String
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Method getPassword.
   * @return String
   */
  public String getPassword() {
    return password;
  }

  /**
   * Method isLoggedIn.
   * @return boolean
   */
  public boolean isLoggedIn() {
    return loggedIn;
  }

  /**
   * Method getError.
   * @return Exception
   */
  public Exception getError() {
    return this.error;
  }

  /**
   * Method getErrorMessage.
   * @return String
   */
  public String getErrorMessage() {
    Throwable cause = error.getCause();
    String message;

    if (cause == null) {
      message = error.getMessage();
      
    } else if (cause instanceof ConnectException) {
      message = "Could not connect to server.\n\nYou may not have internet access or are being blocked by a firewall.\n\nPlease contact your IT professional.";
    
    } else {
      // default to the cause's message.
      // if we can do better than that, we will overwrite the value.
      message = cause.getMessage();
      
      if(message.toLowerCase().contains("connection refused")) {
        message = "Could not connect to server.\n\nYou may not have internet access or are being blocked by a firewall.\n\nPlease contact your IT professional.";
        
      } else if (message.contains("HTTP/1.1 404 Not Found")) {
        message = "Could not connect to server.\n\n" +
            "Please verify that the server is running.";        

      } else if (cause instanceof JMSException) {
        Throwable cause2 = cause.getCause();

        if (cause2 != null) {
          if (cause2 instanceof UnknownHostException) {
            message = "Unknown Host: "
                + cause2.getMessage()
                + "\n\nPlease make sure that the configuration files are identifying the server name correctly.";
          }
        }
      }
    }

    if (message == null || message.trim().length() == 0) {
      message = "A " + error.getClass().getSimpleName() + " was thrown.";
    }

    return message;
  }

}
