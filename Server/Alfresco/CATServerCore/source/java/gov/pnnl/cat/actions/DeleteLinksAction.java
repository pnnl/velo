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
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * When we clean up links for deleted content nodes, we have to perform
 * a search, and this takes too long to embed inside a transaction.
 * Therefore, we call this action so we can do the cleanup 
 * asynchronously.
 * @version $Revision: 1.0 $
 */
public class DeleteLinksAction extends ActionExecuterAbstractBase {

  private static final Log logger = LogFactory.getLog(DeleteLinksAction.class);
  private NodeService nodeService;
  private SearchService searchService;

  // FYI - the NAME property is the bean name as registered in the Spring config files

  /**
   * Set the node service
   * 
   * @param nodeService  set the node service
   */
  public void setNodeService(NodeService nodeService) 
  {
    this.nodeService = nodeService;
  }

  /**
   * Method setSearchService.
   * @param searchService SearchService
   */
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * Define the parameters that can be passed into this action
   * @param paramList List<ParameterDefinition>
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    if (logger.isDebugEnabled())
      logger.debug("setting parameter list");

  }

  /**
   * Method executeImpl.
   * @param ruleAction Action
   * @param nodeActedUpon NodeRef
   */
  @Override
  protected void executeImpl(Action ruleAction, NodeRef nodeActedUpon) {
    ResultSet results = null;
    try {
      if (logger.isDebugEnabled())
        logger.debug("Trying to delete links action");

      // Find all nodes with a destination property that equals the nodeRef
      String query = "@cm\\:destination:\"" + nodeActedUpon.toString() +"\"";
      results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());

      List<NodeRef> links = results.getNodeRefs();

      for(NodeRef link : links) {
        // link may have already been deleted as part of recursive delete
        if(nodeService.exists(link)) {
          if (logger.isDebugEnabled())
            logger.debug("trying to delete node: " + nodeService.getPath(link).toString());
          nodeService.deleteNode(link);
        }
      }
    } finally {
      if(results != null) {
        results.close();
      }
    }
  }


}
