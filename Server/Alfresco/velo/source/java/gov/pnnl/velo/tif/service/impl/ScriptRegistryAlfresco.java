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
package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.cat.util.NodeUtils;

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class ScriptRegistryAlfresco extends ScriptRegistryAdapter {
  protected Log logger = LogFactory.getLog(ScriptRegistryAlfresco.class);

  protected NodeUtils nodeUtils;
  protected NodeService nodeService;
  protected ContentService contentService;
  
  private static final Log log = LogFactory.getLog(ScriptRegistryAlfresco.class
			.getName());
  

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.impl.ScriptRegistryAdapter#init()
   */
  @Override
  public void init() {
    // load scripts saved on alfresco server
    addRegistryProvider(new RegistryConfigFileProviderAlfresco(nodeUtils, nodeService, contentService));
    super.init();
  }

  /**
   * Method setNodeUtils.
   * @param nodeUtils NodeUtils
   */
  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

}
