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
package gov.pnnl.cat.actions;


import gov.pnnl.cat.util.CatConstants;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Remove an aspect from nodes in a subtree.
 *
 * @version $Revision: 1.0 $
 */
public class CountFilesAction extends ActionExecuterAbstractBase
{

  /**
   * The logger
   */
  private static Log logger = LogFactory.getLog(CountFilesAction.class);   
  private SearchService searchService;

  /**
   * @param searchService the searchService to set
   */
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * Add parameter definitions
   * @param paramList List<ParameterDefinition>
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
  {
  }

  /**
   * Actioned upon node ref is our .category file
  
   * @param action Action
   * @param actionedUponNodeRef NodeRef
   * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef) */
  @Override
  protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
    ResultSet results = null;
    try {
      if(logger.isDebugEnabled()) {
        logger.debug("calling executeImpl");
      }

      // do a search to find out the number of documents and write it in the log
      String query = "TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
      results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);
      logger.error("Number of files in the repository = " + results.length());
    } finally {
      if(results != null) {
        results.close();
      }
    }
  }
  
}
