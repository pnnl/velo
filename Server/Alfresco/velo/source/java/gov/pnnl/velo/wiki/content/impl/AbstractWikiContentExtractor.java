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
/**
 * 
 */
package gov.pnnl.velo.wiki.content.impl;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.velo.wiki.content.WikiContentExtractor;
import gov.pnnl.velo.wiki.content.WikiContentExtractorRegistry;

import java.io.File;
import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public abstract class AbstractWikiContentExtractor extends ExtensiblePolicyAdapter implements WikiContentExtractor {

  // Logger
  protected Log logger = LogFactory.getLog(this.getClass());
      
  protected WikiContentExtractorRegistry registry;
  
  @Override
  public void init() {
    // register myself with the registry
    registry.registerWikiContentExtractor(this);
  }
  
  /**
   * Method setRegistry.
   * @param registry WikiContentExtractorRegistry
   */
  public void setRegistry(WikiContentExtractorRegistry registry) {
    this.registry = registry;
  }

  /**
   * Method getFileContentAsString.
   * @param alfrescoFile File
   * @return String
   * @throws IOException
   */
  protected String getFileContentAsString(File alfrescoFile) throws IOException  {
    return FileUtils.readFileToString(alfrescoFile);
  }
  
  /**
   * Method getFileContentAsString.
   * @param alfrescoNode NodeRef
   * @return String
   * @throws IOException
   */
  protected String getFileContentAsString(NodeRef alfrescoNode) throws IOException  {
    FileContentReader reader = (FileContentReader)contentService.getReader(alfrescoNode, ContentModel.PROP_CONTENT);
    return FileUtils.readFileToString(reader.getFile());
  }

}
