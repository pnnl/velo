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
package gov.pnnl.velo.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.core.util.Base64Encoder;

/**
 */
public class Email {
  
  private String from;
  private String to;
  private String subject;
  private String message;
  private Map<String, String> attachments = new HashMap<String, String>();
  
  public Email() {
    
  }
  
  /**
   * Constructor for Email.
   * @param from String
   * @param to String
   * @param subject String
   * @param message String
   * @param attachments List<File>
   */
  public Email(String from, String to, String subject, String message, List<File>attachments) {
    super();
    this.from = from;
    this.to = to;
    this.subject = subject;
    this.message = message;
    
    for(File file : attachments) {
      addAttachment(file);
    }
  }
  
  /**
   * Method addAttachment.
   * @param file File
   */
  public void addAttachment(File file) {
    try {

      String fileName = file.getName();

      // base64 encode the binary contents    
      byte[] ba = FileUtils.readFileToByteArray(file);
      
      String contents = new Base64Encoder().encode(ba);
      attachments.put(fileName,  contents);

    } catch (RuntimeException e) {
      throw e;

    } catch (Throwable e) {
      throw new RuntimeException("Failed to encode file", e);
    }
  }
  
  /**
   * Method getAttachments.
   * @return List<File>
   */
  public List<File> getAttachments() {
    List<File> files = new ArrayList<File>();

    try {

      File tempDir = File.createTempFile("emailAttachments_", "");
      tempDir.delete();
      tempDir.mkdir();

      // base64 decode the string contents    
      for(String fileName : attachments.keySet()) {
        File file = new File(tempDir, fileName);
        String contents = attachments.get(fileName);
        
        byte[] binary = new Base64Encoder().decode(contents);
        FileUtils.writeByteArrayToFile(file, binary);
        files.add(file);
      }

    } catch (RuntimeException e) {
      throw e;

    } catch (Throwable e) {
      throw new RuntimeException("Failed to encode file", e);
    }
    
    return files;
  }

  /**
   * Method getFrom.
   * @return String
   */
  public String getFrom() {
    return from;
  }

  /**
   * Method setFrom.
   * @param from String
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * Method getTo.
   * @return String
   */
  public String getTo() {
    return to;
  }

  /**
   * Method setTo.
   * @param to String
   */
  public void setTo(String to) {
    this.to = to;
  }

  /**
   * Method getSubject.
   * @return String
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Method setSubject.
   * @param subject String
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Method getMessage.
   * @return String
   */
  public String getMessage() {
    return message;
  }

  /**
   * Method setMessage.
   * @param message String
   */
  public void setMessage(String message) {
    this.message = message;
  }

}
