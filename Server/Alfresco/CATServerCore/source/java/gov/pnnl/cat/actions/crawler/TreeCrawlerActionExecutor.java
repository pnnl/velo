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
package gov.pnnl.cat.actions.crawler;

import gov.pnnl.cat.util.PrioritizedThreadPoolExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This action will recursively execute an action on every node in the 
 * repository hierarchy 
 *
 * @version $Revision: 1.0 $
 */
public class TreeCrawlerActionExecutor extends ActionExecuterAbstractBase implements ApplicationContextAware {


  /**
   * The logger
   */
  protected static Log logger = LogFactory.getLog(TreeCrawlerActionExecutor.class); 

  /**
   * Action constants
   */
  public static final String NAME = "tree-crawler";
  public static final String VISITOR_ID = "visitor-id";
  public static final String RUN_ASYNC = "asynchronous";
  public static final String RUN_EACH_NODE_IN_TRANSACTION = "transaction-mode";
  public static final String NAVIGATION_STRATEGY = "navigation-strategy";
  public static final String TOP_DOWN = "top-down";
  public static final String BOTTOM_UP = "bottom-up";

  // the public node service w/ security filtering
  protected NodeService nodeService;
  
  protected ApplicationContext applicationContext;
  protected AuthenticationComponent authenticationComponent;
  protected TransactionService transactionService;
  protected PrioritizedThreadPoolExecutor mediumPriorityThreadPool;

  /**
   * Set the "public" node service (i.e., the one wrapped w/ security)
   * 
   * @param nodeService  set the node service
   */
  public void setNodeService(NodeService nodeService) 
  {
    this.nodeService = nodeService;
  }

  /**
   * @param mediumPriorityThreadPool the mediumPriorityThreadPool to set
   */
  public void setMediumPriorityThreadPool(PrioritizedThreadPoolExecutor mediumPriorityThreadPool) {
    this.mediumPriorityThreadPool = mediumPriorityThreadPool;
  }

  /**
   * Method setApplicationContext.
   * @param applicationContext ApplicationContext
   * @throws BeansException
   * @see org.springframework.context.ApplicationContextAware#setApplicationContext(ApplicationContext)
   */
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  /**
   * Method setAuthenticationComponent.
   * @param authenticationComponent AuthenticationComponent
   */
  public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
    this.authenticationComponent = authenticationComponent;
  }

  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Add parameter definitions
   * @param paramList List<ParameterDefinition>
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
  {
    // params as defined above, but I guess you don't have to declare them here
    // in order to use them - you can send any param you wish, but I don't think they 
    // are validated unless you define them in this method
    
//    public static final String VISITOR_ID = "visitor-id";
//    public static final String RUN_ASYNC = "asynchronous";
//    public static final String RUN_EACH_NODE_IN_TRANSACTION = "transaction-mode";
//    public static final String NAVIGATION_STRATEGY = "navigation-strategy";
   
  }

  /**
  
   * @param action Action
   * @param startingNode NodeRef
   * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef) */
  @Override
  protected void executeImpl(Action action, NodeRef startingNode) {

    if (!this.nodeService.exists(startingNode)){
      // node doesn't exist - can't do anything
      return;
    }

    String visitorId = (String)action.getParameterValue(VISITOR_ID);
    String asynchronousParam = (String)action.getParameterValue(RUN_ASYNC);
    String separateTransactionsParam = (String)action.getParameterValue(RUN_EACH_NODE_IN_TRANSACTION);
    String navigationStrategy = (String)action.getParameterValue(NAVIGATION_STRATEGY);
    INodeVisitor nodeVisitor = (INodeVisitor)applicationContext.getBean(visitorId);

    if (nodeVisitor == null) {
      logger.error("Node visitor bean is null (" + visitorId + "), exiting tree crawler.");
      return;
    }

    boolean asynchronous = false;
    if (asynchronousParam != null && asynchronousParam.equalsIgnoreCase("true")) {
      asynchronous = true;
    }

    boolean separateTransactions = false;
    if(separateTransactionsParam != null && separateTransactionsParam.equalsIgnoreCase("true")) {
      separateTransactions = true;
    }

    Runnable crawlerJob = new CrawlerJob(startingNode, nodeVisitor, separateTransactions, action, navigationStrategy);
    if (asynchronous) {    
      // Run the tree crawl in a separate thread - use medium priority thread pool
      mediumPriorityThreadPool.execute(crawlerJob);

    } else {
      crawlerJob.run();
    }

  }

  /**
   */
  protected class CrawlerJob implements Runnable
  {
    // number of nodes to visit in a batch tx
    final static int BATCH_SIZE = 50;

    protected NodeRef startingNodeRef;
    protected INodeVisitor nodeVisitor;
    protected boolean separateTransactions;
    protected String navigationStrategy = TreeCrawlerActionExecutor.TOP_DOWN;
    protected Action action;
    protected Map<String, Boolean> visitedNodes = new HashMap<String, Boolean>();

    // Nodes to visit in a batch transaction (for efficiency)
    protected List<NodeRef> nodesToVisit = new ArrayList<NodeRef>();

    /**
     * Constructor for CrawlerJob.
     * @param startingNodeRef NodeRef
     * @param nodeVisitor INodeVisitor
     * @param separateTransactions boolean
     * @param action Action
     * @param navigationStrategy String
     */
    public CrawlerJob(NodeRef startingNodeRef, INodeVisitor nodeVisitor, boolean separateTransactions, Action action, String navigationStrategy) {
      this.startingNodeRef = startingNodeRef;
      this.nodeVisitor = nodeVisitor;
      this.separateTransactions = separateTransactions;
      this.action = action;
      if(navigationStrategy != null) {
        this.navigationStrategy = navigationStrategy;
      }
    }

    /**
     * Perform the crawl
     * @see java.lang.Runnable#run()
     */
    public void run() 
    {
      String startingNodeName = (String)nodeService.getProperty(startingNodeRef, ContentModel.PROP_NAME);
      logger.debug("Starting tree  crawl at " + startingNodeName + " using visitor " + nodeVisitor.getName());
      try {

        // Action will automatically run as the current user, so we don't want to change
        // this or security may not be enforced
        //AuthenticationUtil.setRunAsUserSystem();

        // do each operation in own transaction
        if (separateTransactions) {

          RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
          {
            public Object execute() throws Throwable 
            {
              // Give the visitor a chance to set up his own parameters
              nodeVisitor.setup(action.getParameterValues());
              return null;
            }
          };
          transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);

          // Crawl the tree
          recursivelyVisitNode(startingNodeRef);

        } else {
          // wrap everything in a single transaction
          RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
          {
            public Object execute() throws Throwable 
            {
              nodeVisitor.setup(action.getParameterValues());
              recursivelyVisitNode(startingNodeRef);
              return null;
            }
          };
          transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);
        }

        // run the last batch, if using batched transactions
        if(nodesToVisit.size() > 0)
        {
          executeBatchedVisit();
        }
        
      } finally {
        RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
        {
          // Give the visitor a chance to free resources
          public Object execute() throws Throwable 
          {
            nodeVisitor.teardown();
            return null;
          }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);
        logger.debug("Ending crawl with visitor " + nodeVisitor.getName());
      }    
    }

    /**
     * Visits several nodes in a batched transaction
     */
    private void executeBatchedVisit()
    {
      // run the tx
      RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
      {
        public Object execute() throws Throwable 
        { 
          logger.debug("running batch transaction with " + nodesToVisit.size() + " nodes");
          for(NodeRef node : nodesToVisit)
          {
            nodeVisitor.visitNode(node);
          }
          return null;
        }
      };
      transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);

      // wipe the list
      nodesToVisit.clear();      
    }
    
    /**
     * Method nodeVisited.
     * @param node NodeRef
     */
    private void nodeVisited(NodeRef node) {
      visitedNodes.put(node.getId(), Boolean.TRUE);
    }

    /**
     * Method isNodeVisited.
     * @param node NodeRef
     * @return boolean
     */
    private boolean isNodeVisited(NodeRef node) {
      String uuid = node.getId();
      return visitedNodes.containsKey(uuid);
    }

    /**
     * Method recursivelyVisitNode.
     * @param node NodeRef
     */
    private void recursivelyVisitNode(final NodeRef node) 
    {

      // First check to see if it's time to run the transactions
      if(nodesToVisit.size() >= BATCH_SIZE)
      {
        executeBatchedVisit();
      }

      // Visit the current node first if we are doing top-down
      if(navigationStrategy.equals(TreeCrawlerActionExecutor.TOP_DOWN))
      {
        logger.debug("Visiting node: " + node.toString());
        if (!isNodeVisited(node)) {
          logger.debug("First visit - proceeding");

          if (separateTransactions) {
            nodesToVisit.add(node);

          } else {
            nodeVisitor.visitNode(node);
          }
          nodeVisited(node);
        } else {
          logger.debug("Skipping node - already visited");
        }
      }
      // get the children
      List<ChildAssociationRef> children = null;
      if(separateTransactions)
      {
        RetryingTransactionCallback<List<ChildAssociationRef>> cb2 = new RetryingTransactionCallback<List<ChildAssociationRef>>()
        {
          public List<ChildAssociationRef> execute() throws Throwable 
          { 
            return nodeVisitor.getNodeChildren(node);
          }
        };
        children = transactionService.getRetryingTransactionHelper().doInTransaction(cb2, true, true);
      }
      else
      {
        children = nodeVisitor.getNodeChildren(node);
      }

      // visit the children
      for (ChildAssociationRef childNode : children) {
        recursivelyVisitNode(childNode.getChildRef());
      }

      // Visit the current node after if we are doing bottom-up
      if(navigationStrategy.equals(TreeCrawlerActionExecutor.BOTTOM_UP))
      {
        logger.debug("Visiting node: " + node.toString());
        if (!isNodeVisited(node)) {
          logger.debug("First visit - proceeding");

          if (separateTransactions) {
            nodesToVisit.add(node);
          } else {
            nodeVisitor.visitNode(node);
          }
          nodeVisited(node);
        } else {
          logger.debug("Skipping node - already visited");
        }
      }
    }
  }

}
