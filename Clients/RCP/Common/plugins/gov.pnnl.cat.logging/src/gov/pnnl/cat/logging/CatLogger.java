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
package gov.pnnl.cat.logging;

import gov.pnnl.cat.internal.logging.CatLogListener;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * TODO: CatLogger needs to go - we need to move logging capabilities into 
 * gov.pnnl.velo.commons and configure via spring
 */
public class CatLogger {
  public static final String SYS_PROP_LOG_FILE_NAME = "logfile.name";

  //this prop is used by log4j, we set it at runtime for RCP deployments based off
  //the product name IFF there isn't a runtime param logfile.name passed in
  public static final String SYS_PROP_LOG4J_LOG_FILE_PATH = "logfile.path";

  public static final String LOG_PROPERTIES_FILE = "log4j.xml";
  private CatLogListener listener = null;
  private static String logFilePath;

  static {
    // TODO: Consider using install location instead.
    //Location intstallLocation = Platform.getInstallLocation();

    // First make sure the log path is set for the deployment
    // If not, use default value
    logFilePath = System.getProperty(SYS_PROP_LOG4J_LOG_FILE_PATH);

    if(logFilePath == null){
      String logFileName = System.getProperty(SYS_PROP_LOG_FILE_NAME);
      if(logFileName == null) {
        if(Platform.getProduct() == null) {
          logFileName = "velo.log";
        } else {
          // Use application name as default
          logFileName = Platform.getProduct().getName() + ".log";
        }
      } 
      File workspaceFolder;
      
      if(Platform.getProduct() == null) {
        workspaceFolder = new File(System.getProperty("user.home"));

      } else {
        workspaceFolder =  new File(Platform.getInstanceLocation().getURL().getFile());
      }
      logFilePath = new File(workspaceFolder, logFileName).getAbsolutePath();
      System.setProperty(SYS_PROP_LOG4J_LOG_FILE_PATH, logFilePath);

    }
  }

  public void init() {
    configure();
  }

  /**
   * Method getLogger.
   * @param logID String
   * @return Logger
   */
  public static Logger getLogger(String logID) {
    return Logger.getLogger(logID);
  }

  /**
   * Method getLogger.
   * @param theClass Class
   * @return Logger
   */
  public static Logger getLogger(Class theClass) {
    return Logger.getLogger(theClass);
  }

  /**
   * Method getRootLogger.
   * @return Logger
   */
  public static Logger getRootLogger() {
    return Logger.getRootLogger();
  }  

  /**
   * Method getLogFilePath.
   * @return String
   */
  public static String getLogFilePath() {
    return logFilePath;
  }
  
  /**
   * Every plugin needs to call this on startup in order to get System.out statements to properly
   * redirect to the log4j log file.  This is because of plugin classloader isolation.
   */
  public static void redirectSystemOutput() {
    // redirect system out
    PrintStream out = System.out;
  
    Log4jOutputStream outStream = new Log4jOutputStream(Logger.getLogger("out"), Level.INFO, System.out);
    System.setOut(new PrintStream(outStream));
    
    // redirect system err
    Log4jOutputStream errStream = new Log4jOutputStream(Logger.getLogger("err"), Level.ERROR, System.err);
    System.setErr(new PrintStream(errStream));
  }

  private void configure() {

    Bundle bundle = Platform.getBundle(LoggingPlugin.PLUGIN_ID);
    try {
      URL url = bundle.getEntry("/" + LOG_PROPERTIES_FILE);
      InputStream propertiesInputStream = url.openStream();
      if (propertiesInputStream != null) {
        propertiesInputStream.close();
        DOMConfigurator.configure(url);
        getLogger(this.getClass()).info("CAT Logger Installed");
        LogFactory.getLog(this.getClass()).info("Commons Logger Configured");
      } 
    } catch (Exception e) {
      throw new RuntimeException("Error while initializing log properties.",e);
    }    

    this.listener = new CatLogListener();
  }

  void dispose() {
    listener.dispose();
  }

}
