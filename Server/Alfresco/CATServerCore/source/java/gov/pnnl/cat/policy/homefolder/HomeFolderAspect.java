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
package gov.pnnl.cat.policy.homefolder;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Behavior used to enforce integrity constraints on home folders.  For example,
 * home folders cannot be deleted except by an admin.
 *
 * @version $Revision: 1.0 $
 */
public class HomeFolderAspect extends ExtensiblePolicyAdapter 
implements CopyServicePolicies.OnCopyNodePolicy {

  /**
   * Spring init method used to register the policy behaviors
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  @Override
  public void init()
  {
    // Register the policy behaviours
    this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
        CatConstants.ASPECT_HOME_FOLDER,
        new JavaBehaviour(this, "getCopyCallback"));        

    // rest of bindings not needed here, as they are included in ExtensiblePolicy

  }

  /**
   * In general, we don't want to copy the home folder aspect because a home
   * folder can only be created when a new person profile is created.
   * However, when we are copying a home folder space template (when a 
   * new user/team is created), then we do want to copy the aspect.
   * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#getCopyCallback(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails)
   */
  @Override
  public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
    NodeRef sourceNodeRef = copyDetails.getSourceNodeRef();	

    NodeRef userHomeFolderTemplate = nodeUtils.getNodeByName(CatConstants.PATH_USER_HOME_FOLDER_TEMPLATE);
    NodeRef teamHomeFolderTemplate = nodeUtils.getNodeByName(CatConstants.PATH_TEAM_HOME_FOLDER_TEMPLATE);

    if(sourceNodeRef.equals(userHomeFolderTemplate) ||
        sourceNodeRef.equals(teamHomeFolderTemplate) ) {
      // copy this aspect, because this is a home folder space template
      return DefaultCopyBehaviourCallback.getInstance();

    } else {
      // don't copy anything
      return DoNothingCopyBehaviourCallback.getInstance();
    }

  }

  /**
   * Throw an exception if anybody tries to delete this node except a system admin.
   * I'm putting this here instead of setting security policy because I want to give the user
   * full permissions in his home folder, and I want this to be inherited to all children of that
   * folder.  If we remove delete permissions from the home folder, than that would be inherited, which
   * is not what we want.
   * 
   * TODO: after I learn more about the security architecture, see if there is a better place to 
   * implement this
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  @Override
  public void beforeDeleteNode(NodeRef nodeRef) {

    // If the current user isn't an admin - throw an exception
    if(!authorityService.hasAdminAuthority()) {
      throw new AccessDeniedException("Only admins can delete a home folder!");
    }

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    // moving home folders not allowed unless you are admin
    if(!authorityService.hasAdminAuthority()) {
      throw new AccessDeniedException("Home folders cannot be moved!");
    }

  }

  /**
   * This method protects against error conditions on the server.  Once
   * a folder becomes a homeFolder, it cannot be removed unless the
   * node is deleted.  Note that this method ONLY gets invoked when
   * an explicty removeAspect call is made to the NodeService.
   * TODO: If we create a homeFolder type that requires the homeFolder
   * aspect, then we don't need this method.  But I don't think we want to
   * do that because we don't want to copy the home folder aspect on copy.
   * If we create a home folder type, we could get home folders populated
   * all over the place because of copy.
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#beforeRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
    throwIntegrityException("homeFolder aspect may not be removed from a folder.");
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {

    // can't add aspect if target folder is a taxonomyRoot or taxonomyFolder
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_TAXONOMY_ROOT) ||
        nodeService.hasAspect(nodeRef, CatConstants.ASPECT_TAXONOMY_FOLDER)) {
      throwIntegrityException("homeFolder aspect may not be applied to a taxonomy node!");
    }

    // can't add aspect if target folder is a project
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_PROJECT)) {
      throwIntegrityException("homeFolder aspect may not be applied to a project!");
    }

    // can't add aspect if parent folder is a taxonomyRoot or taxonomyFolder
    NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
    if (nodeService.hasAspect(parent, CatConstants.ASPECT_TAXONOMY_ROOT) ||
        nodeService.hasAspect(parent, CatConstants.ASPECT_TAXONOMY_FOLDER)) {
      throwIntegrityException("Home folders may not be created inside a taxonomy!");
    }    

  }

}
