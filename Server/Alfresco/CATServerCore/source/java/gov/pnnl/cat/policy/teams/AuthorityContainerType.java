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
package gov.pnnl.cat.policy.teams;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;

/**
 * If an Alfresco group (i.e., authorityContainer) is deleted, make sure
 * the team profile and team homefolder (if it exists) gets cleaned up too.  
 * We are putting
 * this in policy instead of in the GroupWebService because groups may
 * be deleted via the portal, and we need them to stay in sync.
 * @version $Revision: 1.0 $
 */
public class AuthorityContainerType extends ExtensiblePolicyAdapter {
    
  /**
   * The logger
   */
  private static Log logger = LogFactory.getLog(AuthorityContainerType.class); 

  /**
   * Spring init method used to register the policy behaviors
   */
  public void init() {    
    //  don't need to bind policy here, as it is done in ExtensiblePolicy
  }
  
  /**
   * Method beforeDeleteNode.
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  @Override
  public void beforeDeleteNode(NodeRef nodeRef) {
    logger.debug("calling AuthorityContainerType.beforeDeleteNode");
    
    String groupLongName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_NAME);
    String groupName = authorityService.getShortName(groupLongName);
    String profilePath = CatConstants.XPATH_TEAM_CONTAINER + "/cm:" + ISO9075.encode(groupName);

    logger.debug("trying to find profile with path: " + profilePath);
    
    NodeRef profile = nodeUtils.getNodeByXPath(profilePath);

    // delete the team profile if it exists
    if (profile != null) {
      NodeRef teamHome = (NodeRef)nodeService.getProperty(profile, CatConstants.PROP_TEAM_HOME_FOLDER);
      if(teamHome != null && nodeService.exists(teamHome)) { //will be null if someone deletes the team home folder        
        nodeService.removeAspect(teamHome, CatConstants.ASPECT_TEAM_HOME_FOLDER);
        //no longer want to delete the team's home folder as it means loss of data.  
        //Instead give each user on the deleted team ALL access (unless they had another set of permissions already defined)
  
        Set<String> members = authorityService.getContainedAuthorities(AuthorityType.USER, groupLongName, true);
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(teamHome);
        
        for(String member : members) {
          if(!hasPermission(member, permissions)){ 
            this.permissionService.setPermission(teamHome, member, PermissionService.ALL_PERMISSIONS, true);
          }
        }
      }
      
      logger.debug("trying to delete team profile for " + groupName);
      nodeService.deleteNode(profile);
    } else {
      logger.warn("Profile not found.");
    }
  }

  private boolean hasPermission(String member, Set<AccessPermission> permissions) {
    for (AccessPermission permission : permissions){
      if(permission.getAuthority().equalsIgnoreCase(member)){
        return true;
      }
    }
    return false;
  }

}
