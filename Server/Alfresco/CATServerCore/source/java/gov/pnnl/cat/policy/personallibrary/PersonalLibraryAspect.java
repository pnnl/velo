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
package gov.pnnl.cat.policy.personallibrary;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Behavior used to enforce integrity constraints on personal library folders.  
 *
 * @version $Revision: 1.0 $
 */
public class PersonalLibraryAspect extends ExtensiblePolicyAdapter implements CopyServicePolicies.OnCopyNodePolicy {

  private boolean allowUserDelete = false;

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  @Override
  public void init() {

    // Bind copy behavior to prevent this aspect from being copied
    this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
        CatConstants.ASPECT_PERSONAL_LIBRARY_ROOT,
        new JavaBehaviour(this, "getCopyCallback"));        

    // rest of bindings not needed here, as they are included in ExtensiblePolicy

  }

  /**
   * Method setAllowUserDelete.
   * @param allowUserDelete boolean
   */
  public void setAllowUserDelete(boolean allowUserDelete) {
    this.allowUserDelete = allowUserDelete;
  }

  /**
   * Throw an exception if anybody tries to delete this node except a system admin.
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  @Override
  public void beforeDeleteNode(NodeRef nodeRef) {
    // If the current user isn't an admin - throw an exception

    if(!allowUserDelete && !authorityService.hasAdminAuthority()) {
      throw new AccessDeniedException("Only admins can delete a personal library folder!");
    }    
  }

  /**
   * Method onMoveNode.
   * @param oldChildAssocRef ChildAssociationRef
   * @param newChildAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnMoveNodePolicy#onMoveNode(ChildAssociationRef, ChildAssociationRef)
   */
  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    // moving not allowed
    String user = AuthenticationUtil.getSystemUserName();
    if(!authorityService.hasAdminAuthority() && !AuthenticationUtil.getSystemUserName().equalsIgnoreCase(user)) {
      throw new AccessDeniedException("Only admins can move a personal library folder!");
    } 
  }

  /**
   * This method protects against error conditions on the server.  Once
   * a folder becomes a personal libraryfolder, it cannot be removed unless the
   * node is deleted.  Note that this method ONLY gets invoked when
   * an explicty removeAspect call is made to the NodeService.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeRemoveAspectPolicy#beforeRemoveAspect(NodeRef, QName)
   */
  @Override
  public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
    throwIntegrityException("Personal library aspect may not be removed from a folder.");
  }

  /**
   * In general, we don't want to copy this aspect because a personal
   * library should only be created when creating a new home folder from
   * a template.
   * 
   * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#getCopyCallback(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails)
   */
  @Override
  public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
    NodeRef sourceNodeRef = copyDetails.getSourceNodeRef();	

    NodeRef userHomeFolderTemplate = nodeUtils.getNodeByName(CatConstants.PATH_USER_HOME_FOLDER_TEMPLATE);
    NodeRef teamHomeFolderTemplate = nodeUtils.getNodeByName(CatConstants.PATH_TEAM_HOME_FOLDER_TEMPLATE);
    NodeRef[] possibleParents = new NodeRef[]{userHomeFolderTemplate, teamHomeFolderTemplate};

    if(nodeUtils.isDescendant(sourceNodeRef, possibleParents)) {
      // Only copy this aspect if we are copying from the home folder space template
      return DefaultCopyBehaviourCallback.getInstance();

    } else {
      // don't copy anything
      return DoNothingCopyBehaviourCallback.getInstance();
    }

  }

}
