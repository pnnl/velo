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
 * Notice: This computer software was prepared by Battelle Memorial Institute,
 * hereinafter the Contractor for the Department of Homeland Security under the
 * terms and conditions of the U.S. Department of Energy's Operating Contract
 * DE-AC06-76RLO with Battelle Memorial Institute, Pacific Northwest Division.
 * All rights in the computer software are reserved by DOE on behalf of the
 * United States Government and the Contractor as provided in the Contract. You
 * are authorized to use this computer software for Governmental purposes but it
 * is not to be released or distributed to the public. NEITHER THE GOVERNMENT
 * NOR THE CONTRACTOR MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
 * LIABILITY FOR THE USE OF THIS SOFTWARE. This notice including this sentence
 * must appear on any copies of this computer software.
 */
package gov.pnnl.cat.policy.taxonomy;

import gov.pnnl.cat.util.CatConstants;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 */
public class TaxonomyLinkAspect extends BaseTaxonomyNode implements CopyServicePolicies.OnCopyNodePolicy {
   
  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  @Override
  public void init()
  {
      // Register the policy behaviours
      this.policyComponent.bindClassBehaviour(
               QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
               CatConstants.ASPECT_TAXONOMY_LINK,
               new JavaBehaviour(this, "getCopyCallback"));
      
      // rest of bindings not needed here, as they are included in ExtensiblePolicy
  }  
  
  /**
   * Called after an <b>aspect</b> has been added to a node.
   * This gets called when a new node is created with a default
   * aspect, when the aspect is added manually, or when an
   * aspect is added during a copy operation.  We bound this
   * on TRANSACTION_COMMIT, so execution of this method should
   * be deferred until right before the transaction is going to
   * commit.
   * 
   * @param nodeRef the node to which the aspect was added
   * @param aspectTypeQName the type of the aspect
   * @see org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy#onAddAspect(NodeRef, QName)
   */
  @Override
  public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
    // Moved this code to FilelinkType.onCreateNode so property and
    // aspect can be set at the same time
  }

  /**
   * Before we delete the taxonomyLink, we need to
   * remove the category from the original file
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  @Override
  public void beforeDeleteNode(NodeRef nodeRef) {
  
    NodeRef category = getTaxonomyCategory(nodeRef);
    NodeRef originalFile = (NodeRef)nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);
    
    // If the target node was deleted first, and this link is being deleted
    // as part of a cleanup policy, then the destination prop will be null
    if(originalFile != null) {
      removeCategory(originalFile, category);
    }
  }

  /**
   * @param oldChildAssocRef ChildAssociationRef
   * @param newChildAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnMoveNodePolicy#onMoveNode(ChildAssociationRef, ChildAssociationRef)
   */
  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    
    NodeRef movedNode = newChildAssocRef.getChildRef();
    NodeRef newParent = newChildAssocRef.getParentRef();
    
    // If we are moving from a taxonomy to a regular folder
    if(!nodeService.hasAspect(newParent, CatConstants.ASPECT_TAXONOMY_ROOT) &&
        !nodeService.hasAspect(newParent, CatConstants.ASPECT_TAXONOMY_FOLDER)) {

      // throw an exception - you can't move a taxonomy node outside a taxonomy
      throw new ConstraintException("You can't move a taxonomy node outside of a taxonomy!");       

    } else {
      
      // get the category of the new parent
      NodeRef newCategory = getTaxonomyCategory(newParent);
      
      // get the old category
      NodeRef oldCategory = getTaxonomyCategory(movedNode);
      
      //Update link's category to match parent
      replaceTaxonomyCategory(movedNode, newCategory);
      
      //Update category on original file to match parent
      NodeRef originalFile = (NodeRef)nodeService.getProperty(movedNode, ContentModel.PROP_LINK_DESTINATION);
      replaceCategory(originalFile, oldCategory, newCategory);
    }
  }

  /**
   * If a node with this aspect is copied, we don't want to copy the aspect because due to our
   * other bhavior, the aspect will get re-applied IF the node is being copied into another
   * taxonomy tree.  If the node is being copied outside a taxonomy, then we want to lose
   * the taxonomy identity.
   *
   * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#getCopyCallback(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails)
   */
  @Override
  public CopyBehaviourCallback getCopyCallback(QName classRef,
      CopyDetails copyDetails) {
    // don't copy this aspect
    return DoNothingCopyBehaviourCallback.getInstance();
  }

  /**
   * If this node has been copied outside a taxonomy, we want to make a copy of the 
   * original file instead, not the link.  FYI - copyToNewNode should always be false
   * if we are copying outside a taxonomy, because we replace the link with the original.
   * @see org.alfresco.repo.copy.CopyServicePolicies$OnCopyCompletePolicy#onCopyComplete(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, boolean, java.util.Map)
   */
//  @Override
//  public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, 
//      NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
//   
//    NOTE: this code is now in the TaxonomyCopyServiceInterceptor, to make copies more efficient
//  }  
}
