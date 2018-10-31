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
package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.model.ACE;
import gov.pnnl.velo.model.ACL;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Gets the permissions for a node.  For now we are doing one at a time
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class HasPermissions extends AbstractCatWebScript {
  public static final String PARAM_PATH = "path";  
  public static final String PARAM_PERMISSIONS = "permissions"; 
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    String path = req.getParameter(PARAM_PATH);
    NodeRef nodeRef = NodeUtils.getNodeByName(path, nodeService);
    
    // Get the request parameters:
    String strPerms = req.getParameter(PARAM_PERMISSIONS);
    String[] permissions = strPerms.split(",");
    boolean hasPermissions = true;
    for(int i=0 ;i<permissions.length;i++){
    	if(permissionService.hasPermission(nodeRef, permissions[i]) != AccessStatus.ALLOWED){
    	  hasPermissions = false;
    	  break;
    	}
    }
    // write the response to the output stream
    writeMessage(res, String.valueOf(hasPermissions));

    return null;
    
  }

}
