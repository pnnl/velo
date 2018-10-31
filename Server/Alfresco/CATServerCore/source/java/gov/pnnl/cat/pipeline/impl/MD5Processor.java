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
package gov.pnnl.cat.pipeline.impl;

import gov.pnnl.cat.pipeline.AbstractFileProcessor;
import gov.pnnl.cat.pipeline.FileProcessingInfo;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Computes the MD5 hash for a file's binary contents.
 * @version $Revision: 1.0 $
 */
public class MD5Processor extends AbstractFileProcessor {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#getName()
   */
  @Override
  public String getName() {
    return "MD5 Digest";
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#processFile(gov.pnnl.cat.policy.pipeline.FileProcessingInfo)
   */
  @Override
  public void processFile(FileProcessingInfo fileInfo) throws Exception {
    NodeRef nodeRef = fileInfo.getNodeToExtract();
    
    if (!nodeService.hasAspect(nodeRef, CatConstants.ASPECT_IDENTIFIABLE)) {
      nodeService.addAspect(nodeRef, CatConstants.ASPECT_IDENTIFIABLE, null);
    }
    
    // Generate the MD5 hash
    InputStream input = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream();
    String md5Hash = NodeUtils.createMd5Hash(input);
    nodeService.setProperty(nodeRef, CatConstants.PROP_HASH, md5Hash);
    
  }

}
