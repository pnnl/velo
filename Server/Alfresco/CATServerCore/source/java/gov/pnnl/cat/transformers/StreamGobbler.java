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
package gov.pnnl.cat.transformers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Run each processor in its own transaction. Run as the designated user. Turn off notifications while the processor is running.
 * 



 * @version $Revision: 1.0 $
 */


public class StreamGobbler extends Thread {
  private InputStream is;
  private StringBuffer msg = new StringBuffer();

  /**
   * Constructor for StreamGobbler.
   * @param is InputStream
   */
  public StreamGobbler(InputStream is) {
    this.is = is;
  }

  /**
   * Method getMessage.
   * @return String
   */
  public String getMessage() {
    return msg.toString();
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
        msg.append(line + "\n");
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
