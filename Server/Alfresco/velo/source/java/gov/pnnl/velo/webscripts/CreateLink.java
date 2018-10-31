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
package gov.pnnl.velo.webscripts;


import gov.pnnl.velo.util.WikiUtils;

import java.io.File;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Create link to another alfresco document.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class CreateLink extends AbstractVeloWebScript {
  
  public static final String PARAM_NAME = "name";  
  public static final String PARAM_PARENT_PATH = "parentPath";  
  public static final String PARAM_TARGET_PATH = "targetPath";  
  public static final String PARAM_LINK_NAME = "linkName";  
  
  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @param requestContent File
   * @return Object
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    String parentPath = req.getParameter(PARAM_PARENT_PATH);
    String targetPath = req.getParameter(PARAM_TARGET_PATH);
    String linkName = req.getParameter(PARAM_LINK_NAME);
    
    createLink(parentPath, targetPath, linkName);
    
    return null;
  }

  /**
   * Method createLink.
   * @param parentPath String
   * @param targetPath String
   * @param linkName String
   * @throws Exception
   */
  public void createLink(String parentPath, String targetPath, String linkName) throws Exception {
 // Get the parent node
    if(!parentPath.endsWith("/"))  {
      parentPath = parentPath + "/";
    }
    parentPath = WikiUtils.getAlfrescoNamePath(parentPath);
    logger.debug("parentPath = " + parentPath);
    // (will throw exception if node does not exist)
    NodeRef parent = WikiUtils.getNodeByName(parentPath, nodeService);
    
   
    // Get the target node
    if(!targetPath.endsWith("/"))  {
      targetPath = targetPath + "/";
    }
    targetPath = WikiUtils.getAlfrescoNamePath(targetPath);
    logger.debug("targetPath = " + targetPath);
    // (will throw exception if node does not exist)
    NodeRef target = WikiUtils.getNodeByName(targetPath, nodeService);

    // Add the link (by default link gets the same name as the target)
    WikiUtils.createLinkedFile(target, parent, linkName, nodeService);
    
  }

}
