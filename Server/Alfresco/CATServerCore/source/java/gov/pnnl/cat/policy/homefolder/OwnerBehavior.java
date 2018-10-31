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
package gov.pnnl.cat.policy.homefolder;

import gov.pnnl.cat.actions.crawler.TreeCrawlerActionExecutor;
import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Make the owner property inheritable.  This will perpetuate owners for nodes
 *  created under user and team home folders.  The owner property is not
 *  inheritable by default.
 *
 * @version $Revision: 1.0 $
 */
public class OwnerBehavior extends ExtensiblePolicyAdapter implements TransactionListener {

  //Logger
  private static final Log logger = LogFactory.getLog(OwnerBehavior.class);

  // Key for moved node param
  private static final String MOVED_NODES = "gov.pnnl.cat.policy.homefolder.OwnerBehavior.movedNodes";

  /**
   * Bind to all types, since all types created under a team folder should be
   * owned by the team.
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  @Override
  public void init() {
    logger.debug("initializing");
    policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
        this, // binds to all types
        new JavaBehaviour(this, "onCreateNode"));  

    policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"),
        this, // binds to all types
        new JavaBehaviour(this, "onMoveNode"));  
  }

  /**
   * If the node is under an ownable container, inherit the owner.
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef node = childAssocRef.getChildRef();
    NodeRef parent = childAssocRef.getParentRef();

    if(nodeService.hasAspect(parent, ContentModel.ASPECT_OWNABLE))
    { 
      // Get the owner
      ownableService.setOwner(node, ownableService.getOwner(parent));
    }
  }

  /**
   * If the node is moved under an ownable container, inherit the new owner.
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    NodeRef node = newChildAssocRef.getChildRef();
    NodeRef parent = newChildAssocRef.getParentRef();

    // Run this in an asynchronous thread so it doesn't delay the tx
    if(nodeService.hasAspect(parent, ContentModel.ASPECT_OWNABLE))
    {
      String currentOwner = ownableService.getOwner(node);
      String newOwner = ownableService.getOwner(parent);

      if(!newOwner.equals(currentOwner))
      {
        // this code needs to be run after the transaction commits in a separate transaction
        AlfrescoTransactionSupport.bindListener(this);
        getMovedNodes().add(new MovedNode(node, newOwner));

      }
    }
  }

  /**
   * Method getMovedNodes.
   * @return Set<MovedNode>
   */
  private Set<MovedNode> getMovedNodes() {
    @SuppressWarnings("unchecked")
    Set<MovedNode> movedNodes = (Set<MovedNode>)AlfrescoTransactionSupport.getResource(MOVED_NODES);

    if (movedNodes == null) {
      movedNodes = new HashSet<MovedNode>();
      AlfrescoTransactionSupport.bindResource(MOVED_NODES, movedNodes);
    }

    return movedNodes;
  }

  /* (non-Javadoc)
   * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
   */
  @Override
  public void afterCommit() {
    @SuppressWarnings("unchecked")
    Set<MovedNode> movedNodes = (Set<MovedNode>)AlfrescoTransactionSupport.getResource(MOVED_NODES); 
    ReassignOwnerJob job = new ReassignOwnerJob(movedNodes);
    lowPriorityThreadPool.execute(job);    
  }

  /* (non-Javadoc)
   * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
   */
  @Override
  public void afterRollback() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
   */
  @Override
  public void beforeCommit(boolean readOnly) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
   */
  @Override
  public void beforeCompletion() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.alfresco.repo.transaction.TransactionListener#flush()
   */
  @Override
  public void flush() {
    // TODO Auto-generated method stub

  }

  /**
   */
  private class MovedNode {
    private NodeRef node;
    private String newOwner;

    /**
     * Constructor for MovedNode.
     * @param node NodeRef
     * @param newOwner String
     */
    public MovedNode(NodeRef node, String newOwner) {
      this.node = node;
      this.newOwner = newOwner;
    }

  }

  /**
   */
  private class ReassignOwnerJob implements Runnable {
    private Set<MovedNode> movedNodes;

    /**
     * Constructor for ReassignOwnerJob.
     * @param movedNodes Set<MovedNode>
     */
    public ReassignOwnerJob(Set<MovedNode> movedNodes) {
      this.movedNodes = movedNodes;
    }

    /**
     * Method run.
     * @see java.lang.Runnable#run()
     */
    public void run() {

      for(MovedNode movedNode : movedNodes) {
        final NodeRef nodeRef = movedNode.node;
        try {
          RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
          {
            public Object execute() throws Throwable {
              // switch to admin privs
              AuthenticationUtil.setRunAsUserSystem();
              
              String actionName = "tree-crawler";
              String visitorName = "ownableNodeVisitor";
              Action action = actionService.createAction(actionName);
              action.setParameterValue(TreeCrawlerActionExecutor.VISITOR_ID, visitorName);
              action.setParameterValue(TreeCrawlerActionExecutor.RUN_EACH_NODE_IN_TRANSACTION, "true");

              actionService.executeAction(action, nodeRef, false, false);
              return null;
            }
          };
          transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        } catch (Throwable e) {
          logger.error("Failed to reassign owner for node: " + movedNode.node + " to " + movedNode.newOwner, e);
        }

      }
    }

  }
}
