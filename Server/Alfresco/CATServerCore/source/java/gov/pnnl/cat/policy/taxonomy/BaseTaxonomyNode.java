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
import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.TaxonomyUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains behavior methods used for all taxonomy
 * types and aspects.
 *
 * @version $Revision: 1.0 $
 */
public abstract class BaseTaxonomyNode extends ExtensiblePolicyAdapter {
 
  protected static Log logger = LogFactory.getLog(BaseTaxonomyNode.class);
  
  /**
   * Gets the category assigned to the given taxonomy node.
   * There should only be one, but this could get messed up if people use the 
   * Alfresco UI to assign more categories.
   * We may need to put our categories not under cm:generalclassifiable
   * so this won't happen.
   * @param taxonomyNodeRef NodeRef
   * @return NodeRef
   */
  protected NodeRef getTaxonomyCategory(NodeRef taxonomyNodeRef) {
    return TaxonomyUtils.getTaxonomyCategory(taxonomyNodeRef, nodeService);      
  }

  /**
   * Implements the policy for generating a unique category name based on
   * the taxonomy folder the category corresponds to.
   * Concatenate the node's uuid, so the category name will be unique, in case
   * of same-name siblings
   * @param taxonomyFolder
  
   * @return String
   */
  protected String getCategoryName(NodeRef taxonomyFolder) {

    // We assume the name property and the child association name will be the same
    String folderName = (String)nodeService.getProperty(taxonomyFolder, ContentModel.PROP_NAME);
    String uuid = taxonomyFolder.getId();
    
    String categoryName = folderName + "_" + uuid;
    return categoryName;    
  }

  
  /**
   * If this taxonomy has a redirect property, then use that.  Else
   * we need some convention for picking it out.
   * 
   * @param taxFolder - a folder in a taxonomy from which incoming files need to
   * be redirected
  
   * @return NodeRef
   */
  protected NodeRef getRedirectFolder(NodeRef taxFolder) {
  
    // First find the taxonomy root
    NodeRef taxRoot = getTaxonomyRoot(taxFolder);
    
    NodeRef redirectFolder = (NodeRef)nodeService.getProperty(taxRoot, CatConstants.PROP_REDIRECT_FOLDER);
    
    if(redirectFolder == null) {
    
      // Put it in the current user's temp folder
      String userName = authenticationComponent.getCurrentUserName();
      NodeRef user = personService.getPerson(userName);
      
      // This property can't be null because it is mandatory
      NodeRef homeFolder = (NodeRef)nodeService.getProperty(user, ContentModel.PROP_HOMEFOLDER);
    
      redirectFolder = nodeService.getChildByName(homeFolder, ContentModel.ASSOC_CONTAINS, 
          CatConstants.NAME_TEMP_FOLDER.getLocalName());
           
      if (redirectFolder == null) {
        redirectFolder = NodeUtils.createFolder(homeFolder, CatConstants.NAME_TEMP_FOLDER.getLocalName(), null, nodeService);        
      } 
    }
    // TODO: need a utility method to create a folder path - this is ugly!
    
    // Add a taxonomy name subfolder
    String taxonomyName = (String)nodeService.getProperty(taxRoot, ContentModel.PROP_NAME);
    NodeRef tempFolder = nodeService.getChildByName(redirectFolder, ContentModel.ASSOC_CONTAINS, taxonomyName);
    
    if (tempFolder == null) {
      redirectFolder = NodeUtils.createFolder(redirectFolder,  taxonomyName, null, nodeService);
    } else {
      redirectFolder = tempFolder;
    }
    
    // Add the taxonomy path subtree
    Path rootPath = nodeService.getPath(taxRoot);
    Path folderPath = nodeService.getPath(taxFolder);

    for (int i = rootPath.size(); i < folderPath.size(); i++) {
      Path.Element element = folderPath.get(i);
      if (element instanceof Path.ChildAssocElement) {
         ChildAssociationRef childRef = ((Path.ChildAssocElement)element).getRef();
         String folderName = childRef.getQName().getLocalName();
         tempFolder = nodeService.getChildByName(redirectFolder, ContentModel.ASSOC_CONTAINS, folderName);
         if (tempFolder == null) {
           redirectFolder = NodeUtils.createFolder(redirectFolder,  folderName, null, nodeService);
         } else {
           redirectFolder = tempFolder;
         }
      }
    }
    
    // Add a date subfolder
    return nodeUtils.getDatedFolder(redirectFolder);
  }
  
  /** 
   * Keep walking up the tree until we find the root taxonomy node
   * @param taxNode
  
   * @return NodeRef
   */
  protected NodeRef getTaxonomyRoot(NodeRef taxNode) {
    return nodeUtils.getTaxonomyRoot(taxNode);
  }

  /**
   * Adds a taxonomy category to a node, replacing any previous value.  
   * Since this sets the category for a taxonomy node, only one category
   * can be assigned.
   * If the node doesn't have the generalclassifiable aspect, it is added.
   * @param taxonomyNodeRef - must be a taxonomy node
   * @param category
   */
  protected void replaceTaxonomyCategory(NodeRef taxonomyNodeRef, NodeRef category) {
    
    // Create the categories property value
    List<NodeRef> categories = new ArrayList<NodeRef>(1);
    categories.add(category);
    
    if(!nodeService.hasAspect(taxonomyNodeRef, CatConstants.ASPECT_TAXONOMY_CLASSIFICATION)) {
      // Add the classifiable aspect w/ property attached
      Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
      properties.put(CatConstants.PROP_CATEGORIES, (Serializable)categories);
      nodeService.addAspect(taxonomyNodeRef, CatConstants.ASPECT_TAXONOMY_CLASSIFICATION, properties);
    
    } else {
      // Add category to this node, overwriting any previous value
      nodeService.setProperty(taxonomyNodeRef, CatConstants.PROP_CATEGORIES, (Serializable)categories); 
    }
  }
  
 /**
  * Replaces oldCategory with newCategory in the categories property.  The rest of the
  * list remains unchanged.  This should only be called on non-taxonomy nodes.
  * @param regularFileRef
  * @param oldCategory
  * @param newCategory
  */
  protected void replaceCategory(NodeRef regularFileRef, NodeRef oldCategory, NodeRef newCategory) {
  
    Serializable value = nodeService.getProperty(regularFileRef, CatConstants.PROP_CATEGORIES);
    if (value != null) {
      Collection<NodeRef> categories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, value);       
      categories.remove(oldCategory);
      categories.add(newCategory);
      nodeService.setProperty(regularFileRef, CatConstants.PROP_CATEGORIES, (Serializable)categories);        
    }
  }
  
  /**
   * Inserts this category into the categories list for a regular file.  The target
   * node should not be a taxonomy node, so multiple categories are allowed.
   * This method adds the general classifiable aspect if necessary.
   * @param regularFileRef
   * @param category
   */
  protected void appendCategory(NodeRef regularFileRef, NodeRef category) {
  
    if (!nodeService.hasAspect(regularFileRef, CatConstants.ASPECT_TAXONOMY_CLASSIFICATION)) {
      
        // Add the aspect and set the category property value
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(CatConstants.PROP_CATEGORIES, category);
        nodeService.addAspect(regularFileRef, CatConstants.ASPECT_TAXONOMY_CLASSIFICATION, properties);

    } else {

        // Append the category value to the existing values
        Serializable value = nodeService.getProperty(regularFileRef, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> categories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, value);
        if (!categories.contains(category)) {
        
            categories.add(category);
            nodeService.setProperty(regularFileRef, CatConstants.PROP_CATEGORIES, (Serializable)categories);
        }
    }
  }
  
  /**
   * Removes this category from the categories list on the file.
   * @param regularFileRef
   * @param category
   */
  protected void removeCategory(NodeRef regularFileRef, NodeRef category) {
  
    // First see if the original file has already been deleted by another policy
    if (nodeService.exists(regularFileRef)) {
      Serializable value = nodeService.getProperty(regularFileRef, CatConstants.PROP_CATEGORIES);
      if (value != null) {
        Collection<NodeRef> categories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, value);       
        categories.remove(category);
        nodeService.setProperty(regularFileRef, CatConstants.PROP_CATEGORIES, (Serializable)categories);        
      }
    }
  }
}
