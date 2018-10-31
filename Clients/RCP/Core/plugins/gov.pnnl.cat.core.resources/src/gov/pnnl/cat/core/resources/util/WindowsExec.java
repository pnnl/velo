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
package gov.pnnl.cat.core.resources.util;

import gov.pnnl.cat.logging.CatLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * @see <a href="http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html">When Runtime.exec() won't</a>
 * @version $Revision: 1.0 $
 */
public class WindowsExec {
  private String[] cmdArray;
  private StreamGobbler stdGobbler;
  private StreamGobbler errGobbler;
  private static Logger logger = CatLogger.getLogger(WindowsExec.class);
  public static String NEW_LINE= System.getProperty("line.separator");

  /**
   * Constructor for WindowsExec.
   * @param cmdArray String[]
   */
  public WindowsExec(String... cmdArray) {
    this.cmdArray = cmdArray;
  }

  /**
   * Method exec.
   * @return int
   * @throws InterruptedException
   * @throws IOException
   */
  public int exec() throws InterruptedException, IOException {
    Runtime rt = Runtime.getRuntime();
    Process proc = rt.exec(cmdArray);

    // any error message?
    errGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

    // any output?
    stdGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

    // kick them off
    errGobbler.start();
    stdGobbler.start();

    // any error???
    return proc.waitFor();
  }

  /**
   * Method getStdOutput.
   * @return String
   */
  public String getStdOutput() {
    return stdGobbler.getOutput();
  }

  /**
   * Method getErrOutput.
   * @return String
   */
  public String getErrOutput() {
    return errGobbler.getOutput();
  }

  /**
   */
  private class StreamGobbler extends Thread {
    InputStream is;
    String type;
    StringBuffer strBuf = new StringBuffer();

    /**
     * Constructor for StreamGobbler.
     * @param is InputStream
     * @param type String
     */
    StreamGobbler(InputStream is, String type) {
      this.is = is;
      this.type = type;
    }

    /**
     * Method getOutput.
     * @return String
     */
    public String getOutput() {
      return strBuf.toString();
    }

    /**
     * Method run.
     * @see java.lang.Runnable#run()
     */
    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
          strBuf.append(line + NEW_LINE);
          logger.debug(type + ">" + line);
        }
      } catch (IOException e) {
        logger.error("Error reading process output", e);
      }
    }
  }
}
