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
package gov.pnnl.velo.bootstrap;

import gov.pnnl.cat.actions.crawler.TreeCrawlerActionExecutor;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.util.VeloServerConstants;

import java.util.List;

import org.alfresco.repo.node.index.NodeIndexer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 */
public class VeloReindexer extends AbstractLifecycleBean implements InitializingBean {
  
  private static Log logger = LogFactory.getLog(VeloReindexer.class);

  private NodeUtils nodeUtils;
  private TransactionService transactionService;
  private NodeIndexer nodeIndexer;
  private SearchService searchService;
  private NodeService nodeService;
  protected ActionService actionService;
  

  /*
   * (non-Javadoc)
   * 
   * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
   */
  /**
   * Method onBootstrap.
   * @param event ApplicationEvent
   */
  @Override
  protected void onBootstrap(ApplicationEvent event) {
    reindexNodes(nodeService, searchService, transactionService, nodeUtils, actionService, nodeIndexer);
  }
  
  /**
   * Method reindexNodes.
   * @param nodeService NodeService
   * @param searchService SearchService
   * @param transactionService TransactionService
   * @param nodeUtils NodeUtils
   * @param actionService ActionService
   * @param nodeIndexer NodeIndexer
   */
  public static void reindexNodes(final NodeService nodeService, final SearchService searchService,
      final TransactionService transactionService, final NodeUtils nodeUtils, final ActionService actionService, final NodeIndexer nodeIndexer) {

    try {
      RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
        public Object execute() throws Exception {
          logger.info("Velo starting reindexing for namespace migration");
          AuthenticationUtil.setRunAsUserSystem();
          
          // First determine if we need a reindex (do we find any properties with old namespace ascem when we search)
          // @\\{http\\://www.pnl.gov/velo/model/content/1.0\\}mimetype:*
          logger.info("Checking to see if there are old namespaces in lucene");
          String query = "@\\{http\\://www.pnl.gov/ascem/model/content/1.0\\}mimetype:*";
          ResultSet results = null;
          List<NodeRef> oldNamespaceNodes = null;
          
          try {
            results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);
            oldNamespaceNodes = results.getNodeRefs();

          } finally {
            if(results != null) {
              // MUST close the results or Alfresco will keep the index file handle open
              results.close();
            }
          }

          if(oldNamespaceNodes != null && oldNamespaceNodes.size() > 0) {
            logger.info("Found old namespaces.  Reindexing nodes.");
            
            String actionName = "tree-crawler";
            String visitorName = "reindexerNodeVisitor";
            Action action = actionService.createAction(actionName);
            action.setParameterValue(TreeCrawlerActionExecutor.VISITOR_ID, visitorName);
            action.setParameterValue(TreeCrawlerActionExecutor.RUN_EACH_NODE_IN_TRANSACTION, "true");
            // not sure if i should run this async or not
            //action.setParameterValue(TreeCrawlerActionExecutor.RUN_ASYNC, "true");
            
            // Crawl through everything under User Documents forcing a reindex
            NodeRef userDocuments = nodeUtils.getNodeByXPath(CatConstants.XPATH_USER_DOCUMENTS);
            actionService.executeAction(action, userDocuments, false, false);
            
            // Crawl through everything under Team Documents forcing a reindex
            NodeRef teamDocuments = nodeUtils.getNodeByXPath(CatConstants.XPATH_TEAM_DOCUMENTS);
            actionService.executeAction(action, teamDocuments, false, false);

            // Crawl through everything under Velo forcing a reindex
            NodeRef projects = nodeUtils.getNodeByXPath(VeloServerConstants.XPATH_VELO);
            actionService.executeAction(action, projects, false, false);
            
            // Change the user home space template
            NodeRef spaceTemplates = nodeUtils.getNodeByXPath(VeloServerConstants.XPATH_SPACE_TEMPLATES);
            nodeIndexer.indexUpdateNode(spaceTemplates);
            NodeRef userHomeFolder = nodeUtils.getNodeByXPath(VeloBootstrap.XPATH_HOME_FOLDER_TEMPLATE);
            nodeIndexer.indexUpdateNode(userHomeFolder);
            
          } else {
            logger.info("Did not find nodes to reindex.");
          }
          return null;
        }

      };

      transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, false);
      logger.info("Velo finished reindexing for namespace migration");

    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Velo failed reindexing for namespace migration", e);
    }
  
  }
  
  /**
   * Method setNodeUtils.
   * @param nodeUtils NodeUtils
   */
  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  /**
   * Method onShutdown.
   * @param arg0 ApplicationEvent
   */
  @Override
  protected void onShutdown(ApplicationEvent arg0) {
    // TODO Auto-generated method stub

  }
  
  /**
   * Method setSearchService.
   * @param searchService SearchService
   */
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * Method setNodeIndexer.
   * @param nodeIndexer NodeIndexer
   */
  public void setNodeIndexer(NodeIndexer nodeIndexer) {
    this.nodeIndexer = nodeIndexer;
  }

  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method afterPropertiesSet.
   * @throws Exception
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    logger.info("initializing velo reindexer");
    
  }

  /**
   * Method setActionService.
   * @param actionService ActionService
   */
  public void setActionService(ActionService actionService) {
    this.actionService = actionService;
  }
}
