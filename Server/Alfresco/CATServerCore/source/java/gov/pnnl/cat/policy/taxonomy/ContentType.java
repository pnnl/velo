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
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Behavior for cm:content nodes w.r.t. taxonomies
 * @version $Revision: 1.0 $
 */
public class ContentType extends BaseTaxonomyNode {

  /**
   * Spring init method used to register the policy behaviors
   */
  public void init()
  {
    // Do not bind behavior here, because this class is injected into 
    // an ExtensiblePolicy
  }

  // For now the copy policy is being taken care of via our version of CopyServiceImpl, so we don't need this policy here
//  /**
//   * Behavior if new nodes are created inside a taxonomy
//   * @param childAssocRef ChildAssociationRef
//   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy#onCreateNode(ChildAssociationRef)
//   */
//  public void onCreateNode(ChildAssociationRef childAssocRef) {
//
//    NodeRef parent = childAssocRef.getParentRef();
//
//    // See if the new node is inside a taxonomy
//    if((nodeService.hasAspect(parent, CatConstants.ASPECT_TAXONOMY_ROOT) ||
//        nodeService.hasAspect(parent, CatConstants.ASPECT_TAXONOMY_FOLDER)) 
//        && !nodeService.hasAspect(parent, ContentModel.ASPECT_COPIEDFROM)) {
//
//      // Copy behavior will be handled by the TaxonomyCopyServiceInterceptor 
//      // so we don't need to check this condition here.  We know if it got this
//      // far, the node is not being copied, it is being created brand new.
//
//      // For now, we decided to NOT allow creating new files directly in taxonomies,
//      // because the redirect folder logic was too complicated and confusing
//      throw new ConstraintException("You can't upload files directly into a taxonomy!");
//
//      // redirectTaxonomyFile(childAssocRef);
//    }
//  }

  /**
   * TODO: My exception message is not getting passed up to the Alfresco UI - see if there's a way
   * to fix this.
   * @param oldChildAssocRef ChildAssociationRef
   * @param newChildAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnMoveNodePolicy#onMoveNode(ChildAssociationRef, ChildAssociationRef)
   */
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {

    NodeRef newParent = newChildAssocRef.getParentRef();

    if(nodeService.hasAspect(newParent, CatConstants.ASPECT_TAXONOMY_ROOT) ||
        nodeService.hasAspect(newParent, CatConstants.ASPECT_TAXONOMY_FOLDER)) {

      // throw an exception - you can't move a physical file into a taxonomy
      throw new ConstraintException("You can't move a physical file into a taxonomy!");       
    }  
  }
}
