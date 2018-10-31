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
package gov.pnnl.cat.core.internal.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 */
public class MonitoredInputStream extends FileInputStream {

  private IProgressMonitor monitor;
  /**
   * Constructor for MonitoredInputStream.
   * @param file File
   * @param monitor IProgressMonitor
   * @throws FileNotFoundException
   */
  public MonitoredInputStream(File file, IProgressMonitor monitor) throws FileNotFoundException {
    super(file);
    this.monitor = monitor;
  }

  /**
   * Method available.
   * @return int
   * @throws IOException
   */
  public int available() throws IOException {
    return super.available();
  }
  /**
   * Method close.
   * @throws IOException
   * @see java.io.Closeable#close()
   */
  public void close() throws IOException {
    super.close();
  }
  
  /**
   * Method read.
   * @return int
   * @throws IOException
   */
  public int read() throws IOException {
    int read = super.read();
    if(monitor != null) {
      monitor.worked(read);
    }
    return read;
  }

  /**
   * Method read.
   * @param b byte[]
   * @return int
   * @throws IOException
   */
  public int read(byte[] b) throws IOException {
    int read = super.read(b);
    if(monitor != null) {
      monitor.worked(read);
    }
    return read;
  }
  /**
   * Method read.
   * @param b byte[]
   * @param off int
   * @param len int
   * @return int
   * @throws IOException
   */
  public int read(byte[] b, int off, int len) throws IOException {
    int read = super.read( b,  off,  len);

    if(monitor != null) {
      monitor.worked(read);
    }
    return read;
  }
  /**
   * Method skip.
   * @param n long
   * @return long
   * @throws IOException
   */
  public long skip(long n) throws IOException {
    return super.skip(n);
  }
  

}
