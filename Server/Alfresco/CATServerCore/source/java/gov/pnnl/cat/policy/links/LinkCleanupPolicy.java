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
package gov.pnnl.cat.policy.links;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Because Alfresco doesn't already do it, this class implements link-related integrity
 * checks so that we don't end up with orphan links on the server.
 * 
 * @version $Revision: 1.0 $
 */
public class LinkCleanupPolicy extends ExtensiblePolicyAdapter implements TransactionListener {

  private static Log logger = LogFactory.getLog(LinkCleanupPolicy.class);

  /* Flag indicating whether we have bound ourself as a listener yet */
  private static final String TRANSACTION_BOUND = "gov.pnl.cat.policy.links.ContentType.TransactionBoundFlag";

  /* A set of the content nodes to check for links */
  private static final String DELETED_NODES = "gov.pnl.cat.policy.links.ContentType.DeletedNodes";
  private static final String RENAMED_NODES = "gov.pnl.cat.policy.links.ContentType.RenamedNodes";

  /**
   * Spring init method used to register the policy behaviors
   */
  public void init() {
    // Register the policy behaviours
    this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
        this,
        new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.FIRST_EVENT));

    this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
        this,
        new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT)); 

  }

  /**
   * Alfresco doesn't clean up links - doh!  So, add code to do this here,
   * so we don't end up with orphaned links lying around.
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  @Override
  public void beforeDeleteNode(NodeRef nodeRef) {
    // TODO: this recursive delete logic is too expensive to implement in policy
    // If orphaned links turn out to be a problem, we can add a OrphanedLinkCleaner
    // background cron job to periodically purge links that have no target
    
//    if(logger.isDebugEnabled())
//      logger.debug("calling beforeDeleteNode");
//
//    QName type = nodeService.getType(nodeRef);
//    if(type.equals(ContentModel.TYPE_CONTENT) || type.equals(ContentModel.TYPE_FOLDER)) {
//
//      // Because the search to find links takes longer as the server gets more data,
//      // this code needs to be run after the transaction commits in a separate transaction
//      bindTransactionListener();
//      List<NodeRef> children = nodeUtils.getAllFolderChildren(nodeRef);
//      
//      getDeletedNodes().addAll(children);
//    }
  }

  /**
   * If the node has been renamed, then we need to check if any
   * links are pointing to it and rename the links too
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
   */
  @Override
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    // TODO: this policy is too expensive to run on every node change.  Instead if link name synchronization is a big deal (I doubt it),
    // we should create a LinkNameSync background cron job that periodically checks to make sure link names match
    
//    // Other policy may have deleted this node before it gets here
//    if(!nodeService.exists(nodeRef)) {
//      return;
//    }
//
//    QName type = nodeService.getType(nodeRef);
//
//    // only need to check for renames on these types for now, since these
//    // are the only types we allow in taxonomies at the moment
//    // TODO: if we want to put any arbitrary object in taxonomies, we may need
//    // to take out this if statement
//    if(type.equals(ContentModel.TYPE_CONTENT) || type.equals(ContentModel.TYPE_FOLDER)) {
//
//      String nameBefore = (String) before.get(ContentModel.PROP_NAME);
//      String nameAfter = (String) after.get(ContentModel.PROP_NAME);
//
//      if (nameBefore != null && !EqualsHelper.nullSafeEquals(nameBefore, nameAfter)) {
//        // Because the search to find links takes longer as the server gets more data,
//        // this code needs to be run after the transaction commits in a separate transaction
//        bindTransactionListener();
//        getRenamedNodes().add(nodeRef);
//      }
//    }
  }
  
  /**
   * Only bind the transaction listener once for each transaction
   * (to make more efficient).
   */
  private void bindTransactionListener() {
    Boolean transactionBound = (Boolean)AlfrescoTransactionSupport.getResource(TRANSACTION_BOUND);
    if(transactionBound == null) {
      AlfrescoTransactionSupport.bindListener(this);
      AlfrescoTransactionSupport.bindResource(TRANSACTION_BOUND, new Boolean(true));
    }
  }

  /**
   * Method getDeletedNodes.
   * @return Set<NodeRef>
   */
  private Set<NodeRef> getDeletedNodes() {
    @SuppressWarnings("unchecked")
    Set<NodeRef> deletedNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(DELETED_NODES);

    if (deletedNodes == null) {
      deletedNodes = new HashSet<NodeRef>();
      AlfrescoTransactionSupport.bindResource(DELETED_NODES, deletedNodes);
    }

    return deletedNodes;
  }

  /**
   * Method getRenamedNodes.
   * @return Set<NodeRef>
   */
  private Set<NodeRef> getRenamedNodes() {
    @SuppressWarnings("unchecked")
    Set<NodeRef> renamedNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(RENAMED_NODES);

    if (renamedNodes == null) {
      renamedNodes = new HashSet<NodeRef>();
      AlfrescoTransactionSupport.bindResource(RENAMED_NODES, renamedNodes);
    }

    return renamedNodes;
  }

  /**
   * Execute an action to actually do the search and delete so it runs in a separate thread.
   * This way we can take advantage of the Thread pool used by the ActionService.
   * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
   */
  @SuppressWarnings("unchecked")
  public void afterCommit() {
    if(logger.isDebugEnabled())
      logger.debug("Calling afterCommit");

    Set<NodeRef> deletedNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(DELETED_NODES);
    if(deletedNodes != null) {
      CheckLinksToDeletedNodesJob deleteJob = new CheckLinksToDeletedNodesJob(deletedNodes);
      // link checking can be lower priority
      lowPriorityThreadPool.execute(deleteJob); 
    }
    Set<NodeRef> renamedNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(RENAMED_NODES);
    if(renamedNodes != null) {
      CheckLinksToRenamedNodesJob renameJob = new CheckLinksToRenamedNodesJob(renamedNodes);
      // link checking can be lower priority
      lowPriorityThreadPool.execute(renameJob); 
    }    
  }


  /**
   * Method afterRollback.
   * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
   */
  public void afterRollback() {
    // TODO Auto-generated method stub

  }

  /**
   * Method beforeCommit.
   * @param readOnly boolean
   * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
   */
  public void beforeCommit(boolean readOnly) {
    // TODO Auto-generated method stub

  }

  /**
   * Method beforeCompletion.
   * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
   */
  public void beforeCompletion() {
    // TODO Auto-generated method stub

  }

  /**
   * Method flush.
   * @see org.alfresco.repo.transaction.TransactionListener#flush()
   */
  public void flush() {
    // TODO Auto-generated method stub

  }

  /**
   */
  private class CheckLinksToDeletedNodesJob implements Runnable {

    private Set<NodeRef> contentNodes;

    /**
     * Constructor for CheckLinksToDeletedNodesJob.
     * @param contentNodes Set<NodeRef>
     */
    public CheckLinksToDeletedNodesJob(Set<NodeRef> contentNodes) {
      this.contentNodes = contentNodes;
    }

    /**
     * Method run.
     * @see java.lang.Runnable#run()
     */
    public void run() {
      if(logger.isDebugEnabled())
        logger.debug("trying to run link delete check thread");

      // run this as admin
      AuthenticationUtil.setRunAsUserSystem();

      try {
        RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
        {
          public Object execute() throws Throwable 
          {
            ResultSet results = null;
            try {
              // Create the query to find all the links in one call
              // TODO: if this query is too large, we might have to break it into chunks, especially if
              // deleting a huge folder tree with many files.  If the query results exceed the max query
              // size set in the Alfresco config files, results will be truncated
              //yup - this query got too long and resulted in, "org.alfresco.repo.search.SearcherException: Failed to parse query"
              //in fact, I think having a query string this long is running alfresco out of memory, eek!
              //now we need to do this in batches:
              NodeRef[] nodes = contentNodes.toArray(new NodeRef[contentNodes.size()]);
              int batchSize = 50;
              double batches = Math.ceil(nodes.length / (batchSize * 1.0));
              for(int i = 0; i < batches; i++){
                StringBuffer query = new StringBuffer();
                for(int j = i * batchSize; j < (i * batchSize + batchSize) && j < nodes.length; j++){
                  NodeRef nodeRef = nodes[j];
                  if(query.length() > 0) {
                    query.append(" OR ");
                  }
                  query.append("@cm\\:destination:\"");
                  query.append(nodeRef.toString());
                  query.append("\"");
                }
                if(logger.isDebugEnabled())
                  logger.debug("query = " + query.toString());
  
                // Find all nodes with a destination property that equals the nodeRefs
                results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());
  
                List<NodeRef> links = results.getNodeRefs();
  
                for(NodeRef link : links) {
                  // link may have already been deleted as part of recursive delete
                  if(nodeService.exists(link)) {
                    if(logger.isDebugEnabled())
                      logger.debug("trying to delete node: " + nodeService.getPath(link).toString());
                    nodeService.deleteNode(link);
                  }
                }
              }
              return null;     
            } finally {
              if(results != null) {
                results.close();
              }
            }
          }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);

      } catch(Throwable e) {
        logger.error("Could not clean up links on removed content nodes.", e);

      } finally {
        LinkCleanupPolicy.this.authenticationComponent.clearCurrentSecurityContext();
      }
    }

  }


  /**
   */
  private class CheckLinksToRenamedNodesJob implements Runnable {

    private Set<NodeRef> nodes;

    /**
     * Constructor for CheckLinksToRenamedNodesJob.
     * @param contentNodes Set<NodeRef>
     */
    public CheckLinksToRenamedNodesJob(Set<NodeRef> contentNodes) {
      this.nodes = contentNodes;
    }

    /**
     * Method run.
     * @see java.lang.Runnable#run()
     */
    public void run() {
      if(logger.isDebugEnabled())
        logger.debug("trying to run link rename check job");

      // run this as admin
      AuthenticationUtil.setRunAsUserSystem();

      try {
        RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
        {
          public Object execute() throws Throwable 
          {
            ResultSet results = null;
            try {
              // Create the query to find all the links in one call
              // TODO: if this query is too large, we might have to break it into chunks, especially if
              // deleting a huge folder tree with many files.  If the query results exceed the max query
              // size set in the Alfresco config files, results will be truncated
              StringBuffer query = new StringBuffer();
              for(NodeRef nodeRef : nodes) {
                if(query.length() > 0) {
                  query.append(" OR ");
                }
                query.append("@cm\\:destination:\"");
                query.append(nodeRef.toString());
                query.append("\"");
              }
              if(logger.isDebugEnabled())
                logger.debug("query = " + query.toString());

              // Find all nodes with a destination property that equals the nodeRefs
              results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());

              List<NodeRef> links = results.getNodeRefs();

              for(NodeRef link : links) {
                if(nodeService.exists(link)) {
                  if(logger.isDebugEnabled())
                    logger.debug("trying to rename node: " + nodeService.getPath(link).toString());
                  
                  NodeRef target = (NodeRef)nodeService.getProperty(link, ContentModel.PROP_LINK_DESTINATION);
                  String newName = (String)nodeService.getProperty(target, ContentModel.PROP_NAME);
                  NodeUtils.renameNode(link, newName, nodeService);
                }
              }
              return null;     
            } finally {
              if(results != null) {
                results.close();
              }
            }
          }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);

      } catch(Throwable e) {
        logger.error("Could not rename links on renamed content nodes.", e);

      } finally {
        LinkCleanupPolicy.this.authenticationComponent.clearCurrentSecurityContext();
      }
    }

  }
}
