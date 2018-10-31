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
public class GetPermissions extends AbstractCatWebScript {
  public static final String PARAM_PATH = "path";  
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    String path = req.getParameter(PARAM_PATH);
    NodeRef nodeRef = NodeUtils.getNodeByName(path, nodeService);
    
    ACL acl = new ACL(); // permissions for one node
    List<ACE> aces = new ArrayList<ACE>();
    
    // Return all the permissions set against the current node
    // for any authentication instance (user/group).
    // Then combine them into a single list for each authentication found. 
    Map<String, List<String>> permissionMap = new HashMap<String, List<String>>(8, 1.0f);
    Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
    for (AccessPermission permission : permissions)
    {
       // we are only interested in Allow and not groups/owner etc.
       if (permission.getAccessStatus() == AccessStatus.ALLOWED &&
           (permission.getAuthorityType() == AuthorityType.USER ||
            permission.getAuthorityType() == AuthorityType.GROUP ||
            permission.getAuthorityType() == AuthorityType.GUEST ||
            permission.getAuthorityType() == AuthorityType.EVERYONE))
       {
         String authority = permission.getAuthority();
         ACE ace = new ACE();
         ace.setAccessStatus(permission.getAccessStatus().toString());
         ace.setAuthority(authority);
         ace.setPermission(permission.getPermission());
         aces.add(ace);
       }
    }   
    acl.setInheritPermissions(permissionService.getInheritParentPermissions(nodeRef));
    acl.setAces(aces.toArray(new ACE[aces.size()]));
    // get the owner for this node
    String owner = ownableService.getOwner(nodeRef);
    if(owner == null) {
      owner = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR);
    }
    acl.setOwner(owner);
    acl.setNodePath(path);

    // convert the results to JSON
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), acl);
    
    
    return null;
  }

}
