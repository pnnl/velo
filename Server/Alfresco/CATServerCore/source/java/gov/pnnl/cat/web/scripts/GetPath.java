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
package gov.pnnl.cat.web.scripts;


import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.File;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Gets a path for a uuid, even if the user doesn't have permissions to read that node
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class GetPath extends AbstractCatWebScript {
  public static final String PARAM_UUID = "uuid";  
  
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
    String uuid = req.getParameter(PARAM_UUID);
    NodeRef nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuid);
    String path = "";
    
    // Now run as admin and see if you can get the path
    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    if(nodeService.exists(nodeRef)) {
      path = nodeService.getPath(nodeRef).toString();
    }

    // write the response to the output stream
    writeMessage(res, path);  
    return null;
  }

  /**
   * Method exists.
   * @param path String
   * @return boolean
   */
  public boolean exists(String path) {
   
    // look up node (will throw exception if it doesn't exist)
    NodeRef nodeRef = NodeUtils.getNodeByName(path, nodeService);
    
    if(nodeRef == null) {
      return false;
    }
    return true;
  }

}
