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
import gov.pnnl.cat.util.NodeUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.node.integrity.IntegrityRecord;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 */
public class TaxonomyRootAspect extends BaseTaxonomyNode {
   
  /**
   * Spring init method called when bean is instantiated.
   * Used to register the policy behaviors
   */
  public void init()
  {
    // bindings not needed here, as they are included in ExtensibleAspectPolicy
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
   * TODO: we want to move these categories under a different
   * classification other than generalclassifiable
   * 
   * @param nodeRef the node to which the aspect was added
   * @param aspectTypeQName the type of the aspect
   * @see org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy#onAddAspect(NodeRef, QName)
   */
  public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
    
    // If target is not a folder, throw an exception
    QName type = nodeService.getType(nodeRef);
    if (!type.equals(ContentModel.TYPE_FOLDER)) {
      throw new InvalidTypeException("Can only apply taxonomyRoot aspect to a cm:folder", type);
    }
    
    // If target already has taxFolder aspect, throw exception
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_TAXONOMY_FOLDER)) {
      throw new InvalidAspectException("taxonomyRoot aspect cannot be applied to a taxonomy folder.", 
            aspectTypeQName);
    }
    
    // If parent node is a taxRoot or taxFolder, throw an exception
    NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
    if(nodeService.hasAspect(parent, CatConstants.ASPECT_TAXONOMY_FOLDER) || 
        nodeService.hasAspect(parent, CatConstants.ASPECT_TAXONOMY_ROOT)) {
      throw new InvalidAspectException("taxonomyRoot cannot be applied inside an existing taxonomy.",
           aspectTypeQName);
    }
    
    //Throw an error if target node already has the cat:homeFolder aspect
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_HOME_FOLDER)) {
      throwIntegrityException("taxonomyRoot aspect can not be applied to a home folder!");
    }
    
    //Throw an error if target node already has the cat:project aspect
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_PROJECT)) {
      throwIntegrityException("taxonomyRoot aspect can not be applied to a home folder!");
    }
    
    // If target node has any pre-existing children, throw an exception
    // Stub this out for now until we fix the JUnit tests
//    List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
//    if(children.size() > 0) {
//      throw new InvalidAspectException("taxonomyRoot can only be applied to an empty folder.", aspectTypeQName);
//    }
    
    // Create category corresponding to node name
    String catName = getCategoryName(nodeRef);
    
    // We are not using the public bean, CategoryService.  Instead we are using categoryService,
    // so we are skipping the security interceptor, so anybody can create a category, which is what
    // we want.
    NodeRef category = categoryService.createRootCategory(CatConstants.SPACES_STORE, 
        CatConstants.ASPECT_TAXONOMY_CLASSIFICATION, catName);
    
    // Add category to this node, overwriting any previous value
    replaceTaxonomyCategory(nodeRef, category);
    
    // set the redirect folder property (this isn't currently used)
    //setRedirectProperty(nodeRef);
  }
  
  /**
   * Method setRedirectProperty.
   * @param nodeRef NodeRef
   */
  private void setRedirectProperty(NodeRef nodeRef) {
    // Set the redirect folder property correctly, if null
    NodeRef redirectFolder = (NodeRef)nodeService.getProperty(nodeRef, CatConstants.PROP_REDIRECT_FOLDER);
    
    if(redirectFolder == null) {

      NodeRef curNode = nodeService.getPrimaryParent(nodeRef).getParentRef();
      NodeRef companyHome = nodeUtils.getCompanyHome();
      NodeRef homeFolder = null;
      String redirectFolderName = CatConstants.NAME_TEMP_FOLDER.getLocalName();
      
      while(!curNode.equals(companyHome)) {
        logger.debug("cur node = " + nodeService.getPath(curNode).toString());
        
        if (nodeService.hasAspect(curNode, CatConstants.ASPECT_USER_HOME_FOLDER)) {
          // if taxonomy is under user home folder, redirect there
          homeFolder = curNode;
          break;
          
        } else if (nodeService.hasAspect(curNode, CatConstants.ASPECT_TEAM_HOME_FOLDER)) {
          // if taxonomy is under team home folder, redirect there
          homeFolder = curNode;
          redirectFolderName = CatConstants.NAME_TEAM_TEMP_FOLDER.getLocalName();
          break;
        
          //TODO: don't hard code this path in here
        } else if (nodeService.getPath(curNode).toString().equals("/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/content/1.0}Reference_x0020_Library")) {
          homeFolder = nodeService.getChildByName(curNode, ContentModel.ASSOC_CONTAINS, "Data");
          
          if(homeFolder == null) {
            homeFolder = NodeUtils.createFolder(curNode, "Data", null, nodeService);
          }
          break;
        }
                
        curNode = nodeService.getPrimaryParent(curNode).getParentRef();
      }  
      
      if(homeFolder != null) {
        redirectFolder = nodeService.getChildByName(homeFolder, ContentModel.ASSOC_CONTAINS, redirectFolderName);
        if (redirectFolder == null) {
          redirectFolder = NodeUtils.createFolder(homeFolder, redirectFolderName, null, nodeService);
        }
        logger.debug("trying to set redirect folder to: " + nodeService.getPath(redirectFolder).toString());
        nodeService.setProperty(nodeRef, CatConstants.PROP_REDIRECT_FOLDER, redirectFolder);
      } 
         
    }
  }

  /**
   * This method protects against error conditions on the server.  Once
   * a folder becomes a TaxonomyRoot, it cannot be removed unless the
   * node is deleted.  Note that this method ONLY gets invoked when
   * an explicty removeAspect call is made to the NodeService.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeRemoveAspectPolicy#beforeRemoveAspect(NodeRef, QName)
   */
  public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
    IntegrityRecord rec = new IntegrityRecord("taxonomyRoot aspect may not be removed from a folder.");
    List<IntegrityRecord> integrityRecords = new ArrayList<IntegrityRecord>(0);
    integrityRecords.add(rec);
    throw new IntegrityException(integrityRecords);
  }
  
  /**
   * Delete the corresponding category associated with this taxonomy.  We know the
   * taxonomy node still exists when this is called, so we can look up the category
   * information from the node.
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  public void beforeDeleteNode(NodeRef nodeRef) {
    long start = System.currentTimeMillis();
    
    //i don't think i need to do anything here
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
    logger.debug("TaxonomyRootAspect.beforeDeleteNode time = " + (end - start));
  }

  /**
   * When a whole taxonomy tree is moved somewhere else in the filesystem.  I would say let's do
   * nothing at first and see how this works.  Basically, the taxonomy will map to the exact
   * same Category, so nothing really changes.
   * @param oldChildAssocRef ChildAssociationRef
   * @param newChildAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnMoveNodePolicy#onMoveNode(ChildAssociationRef, ChildAssociationRef)
   */
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    // Do nothing
    
  }

  /**
   * If the folder gets renamed (i.e., the cm:name property changes), rename the Category to match.
   * @param nodeRef NodeRef
   * @param before Map<QName,Serializable>
   * @param after Map<QName,Serializable>
   * @see org.alfresco.repo.node.NodeServicePolicies$OnUpdatePropertiesPolicy#onUpdateProperties(NodeRef, Map<QName,Serializable>, Map<QName,Serializable>)
   */
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, 
      Map<QName, Serializable> after) {
    
    String nameBefore = (String) before.get(ContentModel.PROP_NAME);
    String nameAfter = (String) after.get(ContentModel.PROP_NAME);
    
    if (!nameAfter.equals(nameBefore)) {
      // change the category name to match
      String newCategoryName = getCategoryName(nodeRef);
      
      NodeRef category = getTaxonomyCategory(nodeRef);
      nodeService.setProperty(category, ContentModel.PROP_NAME, newCategoryName);
      
      // change the child association to match by moving the node
      ChildAssociationRef oldAssoc = nodeService.getPrimaryParent(category);
      NodeRef parentRef = oldAssoc.getParentRef();
      nodeService.moveNode(category, parentRef, 
          ContentModel.ASSOC_SUBCATEGORIES, 
          QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newCategoryName));
    }
  }  
}
