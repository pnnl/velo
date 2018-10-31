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
package gov.pnnl.cat.policy.favorites;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Behavior used to enforce integrity constraints on favorites folders.  
 *
 * @version $Revision: 1.0 $
 */
public class FavoritesAspect extends ExtensiblePolicyAdapter {

  private boolean allowUserDelete = false;

  /**
   * Spring init method used to register the policy behaviors
   */
  public void init() {
    // bindings not needed here, as they are included in ExtensiblePolicy     
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
      throw new AccessDeniedException("Only admins can delete a favorites folder!");
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
    // moving favorites folders not allowed
    throw new AccessDeniedException("Favorites cannot be moved!");
  }

  /**
   * This method protects against error conditions on the server.  Once
   * a folder becomes a favorites folder, it cannot be removed unless the
   * node is deleted.  Note that this method ONLY gets invoked when
   * an explicty removeAspect call is made to the NodeService.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeRemoveAspectPolicy#beforeRemoveAspect(NodeRef, QName)
   */
  @Override
  public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
    throwIntegrityException("Favorites aspect may not be removed from a folder.");
  }

}
