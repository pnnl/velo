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
package gov.pnnl.cat.patches;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This patch converts old taxonomy categories under cm:generalclassifiable to
 * new taxonomy categories under tax:classifiable
 *
 * @version $Revision: 1.0 $
 */
public class TaxonomyClassifiablePatch extends AbstractPatch {

  //Logger

  private static final Log logger = LogFactory.getLog(TaxonomyClassifiablePatch.class);

  protected NodeUtils nodeUtils;
  private CategoryService categoryService;

  /**
   * @param categoryService the categoryService to set
   */
  public void setCategoryService(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  /**
   * Method setNodeUtils.
   * @param nodeUtils NodeUtils
   */
  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  /**
   * Ensure that required properties have been set
   * @throws Exception
   */
  protected void checkRequiredProperties() throws Exception
  {
    checkPropertyNotNull(nodeService, "nodeService");
    checkPropertyNotNull(searchService, "searchService");
    checkPropertyNotNull(categoryService, "categoryService");
    checkPropertyNotNull(nodeUtils, "nodeUtils");
  }


  /**
   * Method applyInternal.
   * @return String
   * @throws Exception
   */
  @Override
  protected String applyInternal() throws Exception {
    logger.debug("Trying to execute taxonomy category patch");
    System.out.println("Calling taxonomy patch");

    //common properties must be set before we can continue
    checkRequiredProperties();

    //Make sure the new classification exists
    NodeRef taxClassification = createTaxonomyClassification();

    Collection<ChildAssociationRef> rootCategories = categoryService.getRootCategories(CatConstants.SPACES_STORE, ContentModel.ASPECT_GEN_CLASSIFIABLE);
    String uuidRegex = ".*_[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";

    for (ChildAssociationRef rootCategoryRef : rootCategories) {     
      NodeRef rootCategory = rootCategoryRef.getChildRef();

      // ONLY convert categories that are not taxonomies
      // This can be determined by finding a match on the UUID name pattern in the category name
      String name = (String)nodeService.getProperty(rootCategory, ContentModel.PROP_NAME);
      if(name.matches(uuidRegex)) {
        logger.info("Converting taxonomy: " + name);
        convertCategory(rootCategory, taxClassification);
      } else {
        logger.info("Skipping category: " + name + " because it is not a taxonomy.");
      }

    }

    return "OK";
  }

  /**
   * Method convertCategory.
   * @param oldCategory NodeRef
   * @param newParent NodeRef
   */
  protected void convertCategory(NodeRef oldCategory, NodeRef newParent) {
    final String categoryName = (String)nodeService.getProperty(oldCategory, ContentModel.PROP_NAME);
    RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();

    //1. create new category
    final NodeRef oldCategoryFinal = oldCategory;
    final NodeRef newParentFinal = newParent;

    RetryingTransactionCallback<NodeRef> cb = new RetryingTransactionCallback<NodeRef>()
    {
      public NodeRef execute() throws Throwable 
      {
        return categoryService.createCategory(newParentFinal, categoryName);
      }
    };
    final NodeRef newCategory = helper.doInTransaction(cb, false, true);

    //2. Convert files to point to new category
    cb = new RetryingTransactionCallback<NodeRef>()
    {
      public NodeRef execute() throws Throwable 
      {
        convertFiles(oldCategoryFinal, newCategory);
        return null;
      }
    };
    helper.doInTransaction(cb, false, true);

    //3. Convert each child of oldCategory
    Collection<ChildAssociationRef> children = categoryService.getChildren(oldCategory, Mode.SUB_CATEGORIES, Depth.IMMEDIATE);
    for (ChildAssociationRef childAssociationRef : children) {
      NodeRef oldCategoryChild = childAssociationRef.getChildRef();
      convertCategory(oldCategoryChild, newCategory);
    }

    //4. Remove old category (this will NOT clean up old category properties on files)
    cb = new RetryingTransactionCallback<NodeRef>()
    {
      public NodeRef execute() throws Throwable 
      {
        categoryService.deleteCategory(oldCategoryFinal);
        return null;
      }
    };
    helper.doInTransaction(cb, false, true);
  }

  /**
   * Method convertFiles.
   * @param oldCategory NodeRef
   * @param newCategory NodeRef
   */
  private void convertFiles(NodeRef oldCategory, NodeRef newCategory) {
    ResultSet results = null;
    try {
      // Create the query to find all files using the old category
      StringBuffer query = new StringBuffer();

      query.append("@cm\\:categories:\"");
      query.append(oldCategory.toString());
      query.append("\"");
      if(logger.isDebugEnabled()) {
        logger.debug("query = " + query.toString());
      }
      System.out.println("Trying to find files in category");
      System.out.println("Query = " + query.toString());

      results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());
      System.out.println("Found " + results.length() +" categories to replace.");

      if (results.length() == 0)
      {
        return;
      }   
      List<NodeRef> files = results.getNodeRefs();

      // *******************************************************************************
      // NOTE: calling categoryService.getChildren DOES NOT WORK if this patch is run during
      // a server upgrade (i.e., going from 2.0 to 2.1).  The luceneIndexerAndSearcher used
      // by the category service is returning NO HITS.  Whereas the searchService used
      // below seems to return the hits just fine.  Need help from Alfresco why this is 
      // happening.
      //Collection<ChildAssociationRef> fileRefs = categoryService.getChildren(oldCategory, Mode.MEMBERS, Depth.IMMEDIATE);   
      //    for(ChildAssociationRef fileRef : fileRefs) {
      // ********************************************************************************
      for (NodeRef file : files) {
        //NodeRef file = fileRef.getChildRef();

        // make sure node still exists
        if(nodeService.exists(file)) {
          if(logger.isDebugEnabled())
            logger.debug("trying to convert category on : " + nodeService.getPath(file).toString());

          // Add the classifiable aspect if it doesn't exist
          if(!nodeService.hasAspect(file, CatConstants.ASPECT_TAXONOMY_CLASSIFICATION)) {
            nodeService.addAspect(file, CatConstants.ASPECT_TAXONOMY_CLASSIFICATION, null);
          }

          //b) add the new <tax>categories multivalued property - if not already there
          //c) set a value of property to newnode.toString
          Serializable value = nodeService.getProperty(file, CatConstants.PROP_CATEGORIES);
          Collection<NodeRef> categories;
          if (value != null) {
            categories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, value);  
          } else {
            categories = new ArrayList<NodeRef>(1);
          }
          categories.add(newCategory);
          nodeService.setProperty(file, CatConstants.PROP_CATEGORIES, (Serializable)categories);        

          // Remove the old category property from the node
          // it looks like Alfresco isn't cleaning this up!
          value = nodeService.getProperty(file, ContentModel.PROP_CATEGORIES);
          if (value != null) {
            categories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, value);  
            // get rid of the old category
            categories.remove(oldCategory);

            // if there are no more cm:categories, remove the 
            // cm:generalclassifiable aspect
            if(categories.size() == 0) {
              nodeService.removeAspect(file, ContentModel.ASPECT_GEN_CLASSIFIABLE);
            } else {
              nodeService.setProperty(file, ContentModel.PROP_CATEGORIES, (Serializable)categories);
            }
          }      
        }
      }
    } finally {
      if(results != null) {
        results.close();
      }
    }
  }

  /**
   * Method createTaxonomyClassification.
   * @return NodeRef
   */
  protected NodeRef createTaxonomyClassification() {
    // Make sure the taxonomy classification has been created
    RetryingTransactionCallback<NodeRef> cb = new RetryingTransactionCallback<NodeRef>()
    {
      public NodeRef execute() throws Throwable 
      {
        NodeRef taxClassification = nodeUtils.getNodeByXPath(CatConstants.XPATH_TAXONOMY_CLASSIFICATION);
        if(taxClassification == null) {
          logger.info("Creating new taxonomy classification.");
          // note the attribute property is not used
          taxClassification = categoryService.createClassification(CatConstants.SPACES_STORE, CatConstants.ASPECT_TAXONOMY_CLASSIFICATION, "categories");
        }
        return taxClassification;
      }
    };
    return transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);
  }


}
