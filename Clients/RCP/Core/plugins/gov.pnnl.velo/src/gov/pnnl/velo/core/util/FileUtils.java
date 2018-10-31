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
package gov.pnnl.velo.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 */
public class FileUtils {
  
  /**
   * Method closeQuietly.
   * @param is Reader
   */
  public static void closeQuietly(Reader is) {
    if (is != null) {
      try {
        is.close();
      } catch (IOException e) {
        // ignore errors
      }
    }
  }
  
  /**
   * Method closeQuietly.
   * @param is InputStream
   */
  public static void closeQuietly(InputStream is) {
    if (is != null) {
      try {
        is.close();
      } catch (IOException e) {
        // ignore errors
      }
    }
  }
  
  public static final int BUFFER_SIZE = 4096;
  
  /**
   * Copy the contents of the given input File to the given output File.
   * @param in the file to copy from
   * @param out the file to copy to
  
  
   * @return the number of bytes copied * @throws IOException in case of I/O errors */
  public static int copy(File in, File out) throws IOException {
    assertNotNull(in, "No input File specified");
    assertNotNull(out, "No output File specified");
    return copy(new BufferedInputStream(new FileInputStream(in)),
        new BufferedOutputStream(new FileOutputStream(out)));
  }
  
  /**
   * Copy the contents of the given InputStream to the given OutputStream.
   * Closes both streams when done.
   * @param in the stream to copy from
   * @param out the stream to copy to
  
  
   * @return the number of bytes copied * @throws IOException in case of I/O errors */
  public static int copy(InputStream in, OutputStream out) throws IOException {
    assertNotNull(in, "No InputStream specified");
    assertNotNull(out, "No OutputStream specified");
    try {
      int byteCount = 0;
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead = -1;
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
        byteCount += bytesRead;
      }
      out.flush();
      return byteCount;
    }
    finally {
      try {
        in.close();
      }
      catch (IOException ex) {
      }
      try {
        out.close();
      }
      catch (IOException ex) {
      }
    }
  }
  
  /**
   * Method writeStringToFile.
   * @param file File
   * @param data String
   * @throws IOException
   */
  public static void writeStringToFile(File file, String data) throws IOException {
    OutputStream out = null;
    try {
      out = new FileOutputStream(file);
      out.write(data.getBytes());
        
    } finally {
        if(out != null) {
          out.close();
        }
    }
  }
  
  /**
   * Method readFileAsString.
   * @param file File
   * @return String
   * @throws IOException
   */
  public static String readFileAsString(File file) throws IOException{
    byte[] buffer = new byte[(int) file.length()];
    BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
    f.read(buffer);
    return new String(buffer);
  }

  /**
   * Assert that an object is not <code>null</code> .
   * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
   * @param object the object to check
   * @param message the exception message to use if the assertion fails
  
   * @throws IllegalArgumentException if the object is <code>null</code> */
  public static void assertNotNull(Object object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }

}
