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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Proxies an output stream so that it also
 * logs to a log4j logger.  Used to
 * redirect System.out and System.err
 * @version $Revision: 1.0 $
 */
public class Log4jOutputStream extends OutputStream {

  private final Logger logger;
  private final Level logLevel;
  private final OutputStream originalOutputStream;

  /**
   * Constructor for Log4jOutputStream.
   * @param logger Logger
   * @param logLevel Level
   * @param outputStream OutputStream
   */
  public Log4jOutputStream(Logger logger, Level logLevel, OutputStream outputStream) {
    super();
    this.logger = logger;
    this.logLevel = logLevel;
    this.originalOutputStream = outputStream;
  }

  /**
   * Method write.
   * @param b byte[]
   * @throws IOException
   */
  @Override
  public void write(byte[] b) throws IOException {
    originalOutputStream.write(b);
    String string = new String(b);
    if (!string.trim().isEmpty()) {
      logger.log(logLevel, string);
    }
  }

  /**
   * Method write.
   * @param b byte[]
   * @param off int
   * @param len int
   * @throws IOException
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    originalOutputStream.write(b, off, len);
    String string = new String(b, off, len);
    if (!string.trim().isEmpty()) {
      logger.log(logLevel, string);
    }        
  }

  /**
   * Method write.
   * @param b int
   * @throws IOException
   */
  @Override
  public void write(int b) throws IOException
  {
    originalOutputStream.write(b);
    String string = String.valueOf((char) b);
    if (!string.trim().isEmpty())
      logger.log(logLevel, string);
  }

}
