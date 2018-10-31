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
package gov.pnnl.cat.policy.links;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;

import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * Do not allow users to create a linked folder.  This can cause issues with taxonomies, search,
 * and notifications because the processing logic is so complicated.  Later we can add back the
 * use of linked folders if it becomes a real requirement.
 *
 * @version $Revision: 1.0 $
 */
public class FolderLinkType extends ExtensiblePolicyAdapter {
    
  /**
   * Spring init method used to register the policy behaviors
   */
  public void init() {
    //  don't need to bind policy here, as it is done in ExtensiblePolicy     
  }
  
  /**
   * Method onCreateNode.
   * @param childAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy#onCreateNode(ChildAssociationRef)
   */
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    // Throw an exception because we don't want to allow this node type for now
    throwIntegrityException("app:folderlink type is not allowed!");
  }

}
