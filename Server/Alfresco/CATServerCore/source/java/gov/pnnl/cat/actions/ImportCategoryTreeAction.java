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
/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package gov.pnnl.cat.actions;

import gov.pnnl.cat.util.CatConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Import any category tree into the repository.
 *
 * @version $Revision: 1.0 $
 */
public class ImportCategoryTreeAction extends ActionExecuterAbstractBase
{

  /**
   * The logger
   */
  private static Log logger = LogFactory.getLog(ImportCategoryTreeAction.class); 

  /**
   * Action constants
   */
  public static final String PARAM_ASPECT_NAME = "aspect-name";
  public static final String DISPLAY_NAME_CLASSIFICATION_ASPECT = "Classification Aspect";

  private CategoryService categoryService;
  private ContentService contentService;
  private TransactionService transactionService;
  

  /**
   * @param transactionService the transactionService to set
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * @param contentService the contentService to set
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * @param categoryService the categoryService to set
   */
  public void setCategoryService(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  /**
   * Add parameter definitions
   * @param paramList List<ParameterDefinition>
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
  {
    ParameterDefinitionImpl param = new ParameterDefinitionImpl(
        PARAM_ASPECT_NAME, 
        DataTypeDefinition.QNAME,
        true,
        DISPLAY_NAME_CLASSIFICATION_ASPECT);
    paramList.add(param);
  }

  /**
   * Actioned upon node ref is our .category file
  
   * @param action Action
   * @param actionedUponNodeRef NodeRef
   * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef) */
  @Override
  protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
    if(logger.isDebugEnabled()) {
      logger.debug("calling executeImpl");
    }

    ContentReader contentReader = 
      contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);

    QName aspect = (QName)action.getParameterValue(PARAM_ASPECT_NAME);
        
    BufferedReader reader = null;
    String categoryName = null;
    try {
      // make sure the classification is created
      NodeRef classificationRoot = getClassification(aspect);
      
      String encoding = contentReader.getEncoding();
      // create a reader from the input stream
      if (encoding == null){
          reader = new BufferedReader(new InputStreamReader(contentReader.getContentInputStream()));
      } else{
          reader = new BufferedReader(new InputStreamReader(contentReader.getContentInputStream(), encoding));
      }      

//      // First line is root category name
//      categoryName = reader.readLine().trim();
//
//      // create the root category
//      logger.debug("creating root category " + categoryName);
//      NodeRef categoryNode = categoryService.createRootCategory(CatConstants.SPACES_STORE, aspect, categoryName);

      // load the taxonomy tree     
      Vector<NodeRef> segments = new Vector<NodeRef>();
      segments.add(classificationRoot);
      String[] tokens; 
      NodeRef parentCategory;
      NodeRef categoryNode;
      
      String line = reader.readLine();
      while(line != null) {
        tokens = line.split("\\t");
        int level = tokens.length - 1;
        categoryName = line.trim();
        // Get rid of / characters
        categoryName = categoryName.replaceAll("\\/", " or ");
        categoryName = categoryName.replaceAll(":", ",");
 
        parentCategory = segments.elementAt(level);
        categoryNode = categoryService.createCategory(parentCategory, categoryName);

        int nextSegment = level + 1;
        if(nextSegment >= segments.size()) {
          segments.addElement(categoryNode);
        } else {
          segments.setElementAt(categoryNode, nextSegment);
        }
        line = reader.readLine();
      }

    } catch(Exception e) {
     
      logger.error("Failed to import category: " + categoryName, e);

    } finally {
      if (reader != null) {
        try { 
          reader.close(); 
        } catch(Exception ex) {
          logger.error("failed closing stream", ex);
        }
      }  
//      // delete the tax file, as it's not needed anymore
//      try {
//        nodeService.deleteNode(actionedUponNodeRef);
//      } catch (Exception e) {
//        logger.error("failed to delete tax file", e);
//      }
    }
  }

  /**
   * Create the classification if it doesn't exist
  
   * @param aspect QName
   * @return NodeRef
   */
  protected NodeRef getClassification(final QName aspect) {
    // TODO: do we need to put each category in its own tx or not?
//    return (NodeRef)TransactionUtil.executeInNonPropagatingUserTransaction(
//        transactionService,
//        new TransactionUtil.TransactionWork<Object>()
//        {
//            public Object doWork()
//            {
              NodeRef classification = null;
              Collection<ChildAssociationRef> classifications = categoryService.getClassifications(CatConstants.SPACES_STORE);
              for (ChildAssociationRef childAssociationRef : classifications) {
                if(childAssociationRef.getQName().equals(aspect)) {
                  classification = childAssociationRef.getChildRef();
                  break;
                }      
              }

              if(classification == null) {
                logger.info("Creating new classification: " + aspect);
                // note the attribute property is not used
                classification = categoryService.createClassification(CatConstants.SPACES_STORE, aspect, "categories");
              }
              return classification;
//            }
//        });
  }
}
