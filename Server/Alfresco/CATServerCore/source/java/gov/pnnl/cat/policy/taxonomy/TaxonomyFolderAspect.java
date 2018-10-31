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

import java.io.Serializable;
import java.util.Map;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class TaxonomyFolderAspect extends BaseTaxonomyNode implements CopyServicePolicies.OnCopyNodePolicy {
   
  protected static Log logger = LogFactory.getLog(TaxonomyFolderAspect.class);
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  @Override
  public void init()
  {
      // onCopyNode needs to be bound by this class          
      this.policyComponent.bindClassBehaviour(
            QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
               CatConstants.ASPECT_TAXONOMY_FOLDER,
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
    // category logic moved to FolderType.onCreateNode for policy efficiency
    
    // CAT integrity logic
    //Throw an error if target node already has the cat:homeFolder aspect
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_HOME_FOLDER)) {
      throwIntegrityException("taxonomyFolder aspect can not be applied to a home folder!");
    }
    
    //Throw an error if target node already has the cat:project aspect
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_PROJECT)) {
      throwIntegrityException("taxonomyFolder aspect can not be applied to a home folder!");
    }
  }
  
  /**
   * This method protects against error conditions on the server.  Once
   * a folder becomes a TaxonomyFolder, it cannot be removed unless the
   * node is deleted.  Note that this method ONLY gets invoked when
   * an explicty removeAspect call is made to the NodeService.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeRemoveAspectPolicy#beforeRemoveAspect(NodeRef, QName)
   */
  @Override
  public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
    throwIntegrityException("taxonomyFolder aspect may not be removed from a folder.");
  }

  /**
   * Delete the corresponding subcategory associated with this node.  We know the
   * taxonomy node still exists when this is called, so we can look up the category
   * information from the node.
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  @Override
  public void beforeDeleteNode(NodeRef nodeRef) {
    long start = System.currentTimeMillis();
    
    // Get the categories registered to this node
    NodeRef category = getTaxonomyCategory(nodeRef);
    if (category != null) {
      // it looks like all files that have this category in their categories property automatically
      // get cleaned up
      if(nodeService.exists(category)) {
        categoryService.deleteCategory(category);
      }
    }
    
    long end = System.currentTimeMillis();    
    if(logger.isDebugEnabled()) 
      logger.debug("TaxonomyFolder.beforeDeleteNode time = " + (end - start));
  }
  
  /**
   * We can only move from one taxonomy to another.  Trying to move outside a taxonomy
   * will produce an error.  The option to do this should be removed from the UI.
   * @param oldChildAssocRef ChildAssociationRef
   * @param newChildAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnMoveNodePolicy#onMoveNode(ChildAssociationRef, ChildAssociationRef)
   */
  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {   

    NodeRef movedNode = newChildAssocRef.getChildRef();
    NodeRef newParent = newChildAssocRef.getParentRef();
    
    // If we are trying to move outside a taxonomy
    if(!nodeService.hasAspect(newParent, CatConstants.ASPECT_TAXONOMY_ROOT) &&
        !nodeService.hasAspect(newParent, CatConstants.ASPECT_TAXONOMY_FOLDER)) {

      // throw an exception - you can't move a taxonomy node outside a taxonomy
      throw new ConstraintException("You can't move a taxonomy node outside of a taxonomy!");       

    } else {
      
      // get the category of the new parent
      NodeRef newCategory = getTaxonomyCategory(newParent);
      
      // get the old subcategory on this node
      NodeRef oldSubcategory = getTaxonomyCategory(movedNode);
      
      // move the oldsubcategory to the new category parent
      // change the name property to match
      String catName = getCategoryName(movedNode);
      nodeService.setProperty(oldSubcategory, ContentModel.PROP_NAME, catName);
      
      // change the child association
      nodeService.moveNode(oldSubcategory, newCategory, 
          ContentModel.ASSOC_SUBCATEGORIES, 
          QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, catName));
      
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
   * If the cm:name property changes, then we have to rename the subcategory.
   * 
   * @param nodeRef NodeRef
   * @param before Map<QName,Serializable>
   * @param after Map<QName,Serializable>
   * @see org.alfresco.repo.node.NodeServicePolicies$OnUpdatePropertiesPolicy#onUpdateProperties(NodeRef, Map<QName,Serializable>, Map<QName,Serializable>)
   */
  @Override
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, 
      Map<QName, Serializable> after) {    
    
    String nameBefore = (String) before.get(ContentModel.PROP_NAME);
    String nameAfter = (String) after.get(ContentModel.PROP_NAME);
    
    if (!nameAfter.equals(nameBefore)) {
      // change the category name to match
      String catName = getCategoryName(nodeRef);
      NodeRef category = getTaxonomyCategory(nodeRef);
      nodeService.setProperty(category, ContentModel.PROP_NAME, catName);
      
      // change the child association to match by moving the node
      ChildAssociationRef oldAssoc = nodeService.getPrimaryParent(category);
      NodeRef parentRef = oldAssoc.getParentRef();
      nodeService.moveNode(category, parentRef, 
          ContentModel.ASSOC_SUBCATEGORIES, 
          QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, catName));
    }    
  }  
}
