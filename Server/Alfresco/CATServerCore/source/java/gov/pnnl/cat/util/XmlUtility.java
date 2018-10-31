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
package gov.pnnl.cat.util;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;

/**
 * Serialize and deserialize Objects to/from XML
 * @version $Revision: 1.0 $
 */
public class XmlUtility {
  public static final String XML_EXTENSION = ".xml";

  private static final String UTF_8 = "UTF-8";

  private static final XStream XSTREAM;
  
  static {
    //xstream = new XStream(new PureJavaReflectionProvider());  // pure 
    XSTREAM = new XStream(new Sun14ReflectionProvider()); // enhanced

  }

  /**
   * Deserialize the given XML to an Object.
   * 
  
   * @param xml
   *          String XML to deserialize
  
   * @return Object */
  public static <T> T deserialize(String xml) {
    return (T) XSTREAM.fromXML(xml);
  }

  /**
   * Read the {@link File} as a {@link FileInputStream} and deserialize its XML contents to an Object.
   * 
  
   * @param file
   *          {@link File} to read and deserialize
  
   * @return Object */
  public static <T> T deserializeFile(File file) {
    InputStream inputStream = null;

    try {
      inputStream = new BufferedInputStream(new FileInputStream(file));

      return (T) deserializeInputStream(inputStream);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * Open a new {@link File} at the given absolute filename, then read and deserialize its XML contents to an Object.
   * 
  
   * @param filename
   *          String absolute file name
  
   * 
  
   * @return Object * @see #deserializeFile(File) */
  public static <T> T deserializeFile(String filename) {
    File file = new File(filename);

    return (T) deserializeFile(file);
  }

  /**
   * Deserialize the {@link InputStream} as an instance of T.
   * <p>
   * Callers should close the {@link InputStream}!
   * </p>
   * 
  
   * @param inputStream
   *          {@link InputStream} to deserialize
  
   * @return Object */
  public static <T> T deserializeInputStream(InputStream inputStream) {
    return (T) XSTREAM.fromXML(inputStream);
  }

  /**
   * Deserialize the {@link InputStream} as an instance of T.
   * <p>
   * Closes the {@link InputStream} when complete!
   * </p>
   * 
  
   * @param inputStream
   *          {@link InputStream} to deserialize
  
   * @return Object */
  public static <T> T deserializeInputStreamAndClose(InputStream inputStream) {
    try {
      return (T) deserializeInputStream(inputStream);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * Deserialize the {@link Reader} as an instance of T.
   * <p>
   * Closes the {@link Reader} when complete!
   * </p>
   * 
  
   * @param reader
   *          {@link Reader} to deserialize
  
   * @return Object */
  public static <T> T deserializeReader(Reader reader) {
    return (T) XSTREAM.fromXML(reader);
  }

  /**
   * Deserialize the {@link Reader} as an instance of T.
   * <p>
   * Closes the {@link Reader} when complete!
   * </p>
   * 
  
   * @param reader
   *          {@link Reader} to deserialize
  
   * @return Object */
  public static <T> T deserializeReaderAndClose(Reader reader) {
    try {
      return (T) deserializeReader(reader);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * Serialize the given Object to XML.
   * 
   * @param value
   *          Object to serialize
  
   * @return String XML representation of given Object */
  public static String serialize(Object value) {
    return XSTREAM.toXML(value);
  }

  /**
   * Serialize the given Object to XML in the given {@link File}.
   * 
   * @param value
   *          Object to serialize
   * @param file
   *          {@link File} to write the XML to
   */
  public static void serializeToFile(Object value, File file) {
    OutputStream outputStream = null;

    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(file));
      XSTREAM.toXML(value, outputStream);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(outputStream);
    }
  }

  /**
   * Serialize the given Object to XML in a new {@link File} at the given absolute filename.
   * 
   * @param value
   *          Object to serialize
   * @param filename
   *          String absolute file name of {@link File}
  
   * @return {@link File} with serialized value */
  public static File serializeToFile(Object value, String filename) {
    File file = new File(filename);
    serializeToFile(value, file);

    return file;
  }

  /**
   * Write the Object to XML and return as an {@link InputStream} using UTF-8 encoding.
   * <p>
   * Callers should close the returned {@link InputStream}!
   * </p>
   * 
   * @param value
   *          Object to serialize
  
   * @return Buffered {@link InputStream} pointing to serialized value */
  public static InputStream serializeToInputStream(Object value) {
    try {
      String xml = serialize(value);
      InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(xml.getBytes(UTF_8)));

      return inputStream;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Write the Object to XML into the given {@link OutputStream}.
   * <p>
   * Callers should close the {@link OutputStream}!
   * </p>
   * 
   * @param value
   *          Object to serialize
   * @param outputStream
   *          {@link OutputStream} to write to
   */
  public static void serializeToOutputStream(Object value, OutputStream outputStream) {
    XSTREAM.toXML(value, outputStream);
  }

  /**
   * Write the Object to XML into the given {@link OutputStream}.
   * <p>
   * Closes the {@link OutputStream} when complete!
   * </p>
   * 
   * @param value
   *          Object to serialize
   * @param outputStream
   *          {@link OutputStream} to write to
   */
  public static void serializeToOutputStreamAndClose(Object value, OutputStream outputStream) {
    try {
      serializeToOutputStream(value, outputStream);
    } finally {
      IOUtils.closeQuietly(outputStream);
    }
  }

  /**
   * Write the Object to XML into the given {@link Writer}.
   * <p>
   * Callers should close the {@link Writer}!
   * </p>
   * 
   * @param value
   *          Object to serialize
   * @param writer
   *          {@link Writer} to write to
   */
  public static void serializeToWriter(Object value, Writer writer) {
    XSTREAM.toXML(value, writer);
  }

  /**
   * Write the Object to XML into the given {@link Writer}.
   * <p>
   * Closes the {@link Writer} when complete!
   * </p>
   * 
   * @param value
   *          Object to serialize
   * @param writer
   *          {@link Writer} to write to
   */
  public static void serializeToWriterAndClose(Object value, Writer writer) {
    try {
      serializeToWriter(value, writer);
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  /**
   * Cannot instantiate
   */
  private XmlUtility() {
    super();
  }
}
