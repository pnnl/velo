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

import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.util.WikiUtils;

import java.io.File;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to delete a resource.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class Move extends AbstractVeloWebScript {
  public static final String PARAM_OLD_PATH = "oldPath";  
  public static final String PARAM_NEW_PARENT_PATH = "newParentPath";  
  
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
    String oldPath = req.getParameter(PARAM_OLD_PATH);
    String newParentPath = req.getParameter(PARAM_NEW_PARENT_PATH);
    
    move(oldPath, newParentPath);
    
    return null;
  }

  /**
   * Method move.
   * @param oldPath String
   * @param newParentPath String
   * @throws Exception
   */
  public void move(String oldPath, String newParentPath) throws Exception {
    // Convert the path to alfresco format
    String oldAlfrescoPath = WikiUtils.getAlfrescoNamePath(oldPath);
    String newAlfrescoParentPath = WikiUtils.getAlfrescoNamePath(newParentPath);
    
    // Find the node from the path (will throw exception if node does not exist) 
    NodeRef nodeRef = WikiUtils.getNodeByName(oldAlfrescoPath, nodeService);
    
    // Find the new parent node from the path (will throw exception if node does not exist)
    NodeRef newParent = WikiUtils.getNodeByName(newAlfrescoParentPath, nodeService);
    
    // move the node
    NodeUtils.moveNode(nodeRef, newParent, nodeService);
  }

}
