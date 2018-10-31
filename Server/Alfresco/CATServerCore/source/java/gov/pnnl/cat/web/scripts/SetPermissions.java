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
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sets the permissions for a node or set of node.   The permissions (ACL)
 * is all-inclusive and assumes that it will completely replace the permissions
 * previously set on the node.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class SetPermissions extends AbstractCatWebScript {
  
  // set recursive = true if you want to apply the permissions recursively
  public static final String PARAM_RECURSIVE = "recursive"; 
  // ACL[] passed in request body via JSON

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    String recursiveStr = req.getParameter(PARAM_RECURSIVE);
    boolean recursive = false;
    if(recursiveStr != null) {
      recursive = Boolean.valueOf(recursiveStr);
    }
    
    // Convert request body to JSON
    ACL[] acls = null;
    ObjectMapper mapper = new ObjectMapper();

    acls = mapper.readValue(requestContent, ACL[].class);

    // TODO: need a parameter in the request to indicate if we want to apply the ACLs recursively or not!
    for (ACL acl : acls) {
      setPermissions(acl, recursive);
    }
    
    return null;
  }
  
  private void setPermissions(ACL acl, boolean recursive) {
    NodeRef nodeRef = NodeUtils.getNodeByName(acl.getNodePath(), nodeService);
    
    // If we can't find the node (maybe the user doesn't have permissions to see the node)
    // log error (not sure if we should throw this back or not?)
    if(nodeRef == null) {
      String currentUser = authenticationComponent.getCurrentUserName();
      String errorMsg = "Could not set permissions for node: " + acl.getNodePath() + 
          ", as this node does not exist or current user " + currentUser +
          " does not have permissions to see it!";
      logger.error(errorMsg);
      //throw new RuntimeException(errorMsg);
      return;
    }
    
    // First start with a clean slate
    permissionService.deletePermissions(nodeRef);
    
    permissionService.setInheritParentPermissions(nodeRef, acl.isInheritPermissions());
    
    // Add the permissions for each ace
    for (ACE ace : acl.getAces()) {

      // Add the permissions associated with the ace
      boolean allow = false;
      if (ace.getAccessStatus().equals(ACE.ACCESS_STATUS_ALLOWED)) {
        allow = true;
      }
      this.permissionService.setPermission(nodeRef, ace.getAuthority(), ace.getPermission(), allow);
    }

    // set the owner to the owner specified by the ACL
    // TODO: check if the owner has changed first.
    if(acl.getOwner() != null) {
      ownableService.setOwner(nodeRef, acl.getOwner());
    }
    if(recursive) {
      List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
      for(ChildAssociationRef assoc : assocRefs) {
        recursivelyInheritPermissions(assoc.getChildRef(), acl.getOwner());
      }
    }
    
  }
  
  private void recursivelyInheritPermissions(NodeRef nodeRef, String owner) {
    // First start with a clean slate
    permissionService.deletePermissions(nodeRef);
    // Inherit permissions from our parent
    permissionService.setInheritParentPermissions(nodeRef, true);
    
    // change the owner
    if(owner != null) {
      ownableService.setOwner(nodeRef, owner);
    }
    
    List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
    for(ChildAssociationRef assoc : assocRefs) {
      recursivelyInheritPermissions(assoc.getChildRef(), owner);
    } 
  }
    

}
