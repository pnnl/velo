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
package gov.pnnl.cat.pipeline;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 */
public class FileProcessingInfo {
  
  private boolean textExtractionRequired;
  private NodeRef nodeRef;
  private long fileSize;
  private String mimetype;
  private String username;
  private String fileName;
  private Map<QName, Serializable> properties;
  
  //TODO no longer pass in the username to run the extraction as. instead turn off audit policy when 
  //we run the extractor, so it won't try to set the current user as the modifier then we can run 
  //the extractor as admin
  /**
   * Constructor for FileProcessingInfo.
   * @param fileSize long
   * @param mimetype String
   * @param nodeToProcess NodeRef
   * @param textExtractionRequired boolean
   * @param username String
   * @param fileName String
   * @param properties Map<QName,Serializable>
   */
  public FileProcessingInfo(long fileSize, String mimetype, NodeRef nodeToProcess, boolean textExtractionRequired, 
      String username, String fileName,  Map<QName, Serializable> properties) {
    super();
    this.fileSize = fileSize;
    this.mimetype = mimetype;
    this.nodeRef = nodeToProcess;
    this.textExtractionRequired = textExtractionRequired;
    this.username = username;
    this.fileName = fileName;
    this.properties = properties;
  }
  
  /**
  
   * @return the fileSize */
  public long getFileSize() {
    return fileSize;
  }
  /**
  
   * @return the mimetype */
  public String getMimetype() {
    return mimetype;
  }
  
  /**
  
   * @return the username */
  public String getUsername() {
    return username;
  }

  /**
  
   * @return the textExtractionRequired */
  public boolean isTextExtractionRequired() {
    return textExtractionRequired;
  }
  /**
  
   * @return the nodeRef */
  public NodeRef getNodeToExtract() {
    return nodeRef;
  }

  /**
   * Method getFileName.
   * @return String
   */
  public String getFileName() {
    return fileName;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof FileProcessingInfo) && ((FileProcessingInfo)obj).nodeRef.equals(this.nodeRef);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return nodeRef.hashCode();
  }

  /**
   * Method getProperties.
   * @return Map<QName,Serializable>
   */
  public Map<QName, Serializable> getProperties() {
    return properties;
  }

}
