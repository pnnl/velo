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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class FolderType extends BaseTaxonomyNode {

  protected static Log logger = LogFactory.getLog(FolderType.class);

  /**
   * Spring init method used to register the policy behaviors
   */
  public void init()
  {     
    logger.debug("initializing");
    // Do not bind behavior here, because this class is injected into 
    // an ExtensiblePolicy
  }

  /**
   * Method beforeDeleteNode.
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  @Override
  public void beforeDeleteNode(NodeRef nodeRef) {
    // TODO: it is way to expensive to crawl the tree during deletes.  Instead we need to have a background process (maybe
    // runs once a day) that cleans up taxonomy root categories that no longer map to a folder

//    // for each child of this folder that is a taxonomyRoot, delete the taxonomyRoot category
//    List<ChildAssociationRef> children = this.nodeService.getChildAssocs(nodeRef);
//    NodeRef child;
//    for (ChildAssociationRef childRef : children) {
//      child = childRef.getChildRef();
//
//      // Get the categories registered to this node
//      // only need to find folders, not files
//      if(nodeService.getType(child).equals(ContentModel.TYPE_FOLDER)) {
//        NodeRef category = getTaxonomyCategory(child);
//        if (category != null) {
//          // it looks like all files that have this category in their categories property automatically
//          // get cleaned up
//          if (nodeService.exists(category)) {
//            categoryService.deleteCategory(category);
//          }
//        }else{//else check subfolders of the child to see if they are taxonomy roots...and so on
//          beforeDeleteNode(child);
//        }
//      }
//    }

  }
  

  /**
   * Called after a new node of type cm:folder has been created.
   * If this node was created inside a taxonomyRoot or taxonomyFolder,
   * then we also need to inherit the taxonomyFolder aspect.
   * @param childAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy#onCreateNode(ChildAssociationRef)
   */  
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {

    NodeRef parent = childAssocRef.getParentRef();
    NodeRef nodeRef = childAssocRef.getChildRef();
    
    if(!nodeService.exists(nodeRef)) {
      return;
    }

    if (nodeService.hasAspect(parent, CatConstants.ASPECT_TAXONOMY_ROOT) ||
        nodeService.hasAspect(parent, CatConstants.ASPECT_TAXONOMY_FOLDER)) {

      // ignore any derived cm:folder types (like system folders), 
      // as they are usually hidden, and should not be part of the taxonomy
      if (!nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
        if(logger.isDebugEnabled()) {
          // Alfresco 2.2E has a different signature for toDisplayPath, so commenting this out
          // for now
//          logger.debug("not including folder: " + nodeService.getPath(nodeRef).toDisplayPath(nodeService) +
//              " in taxonomy because it is of type " + nodeService.getType(nodeRef));
          logger.debug("not including folder: " + nodeRef +
              " in taxonomy because it is of type " + nodeService.getType(nodeRef));
        } 

      } else {

        // Add taxonomyFolder aspect to node
        nodeService.addAspect(childAssocRef.getChildRef(), CatConstants.ASPECT_TAXONOMY_FOLDER, null);

        //Get category from parent
        NodeRef category = getTaxonomyCategory(parent);

        //Create subcategory
        String catName = getCategoryName(nodeRef);
        NodeRef subcategory = categoryService.createCategory(category, catName);

        //Add subcategory to this node, replacing if one already exists
        replaceTaxonomyCategory(nodeRef, subcategory);
      }
    }
  }

  /**
   * If we are trying to move a non-taxonomy folder into a taxonomy tree, 
   * throw an exception.
   * @param oldChildAssocRef ChildAssociationRef
   * @param newChildAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnMoveNodePolicy#onMoveNode(ChildAssociationRef, ChildAssociationRef)
   */
  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {

    NodeRef movedNode = newChildAssocRef.getChildRef();
    if(!nodeService.hasAspect(movedNode, CatConstants.ASPECT_TAXONOMY_FOLDER)) {

      NodeRef newParent = newChildAssocRef.getParentRef();
      if(nodeService.hasAspect(newParent, CatConstants.ASPECT_TAXONOMY_ROOT) ||
          nodeService.hasAspect(newParent, CatConstants.ASPECT_TAXONOMY_FOLDER)) {

        // throw an exception - you can't move a folder into a taxonomy
        throw new ConstraintException("You can't move a regular folder into a taxonomy!");       
      }

    }

  }

}
