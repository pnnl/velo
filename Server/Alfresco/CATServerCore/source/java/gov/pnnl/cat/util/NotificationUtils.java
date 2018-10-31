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
package gov.pnnl.cat.util;

import gov.pnnl.cat.policy.notifiable.NotifiableAspectBehavior;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEvent;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventList;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;

import java.util.List;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 */
public class NotificationUtils {

  private static final Log logger = LogFactory.getLog(NotificationUtils.class);

  private PrioritizedThreadPoolExecutor notificationThreadPool;
  private SearchService searchService;
  private JmsTemplate template;
  private Destination destination;
  private TransactionService transactionService;
  private AuthenticationComponent authenticationComponent;
  private NodeService nodeService;
  private NamespacePrefixResolver namespacePrefixResolver;
  private boolean notificationEnabled;

  // public util methods
  
  
  /**
   * Method sendRepositoryEventList.
   * @param eventList RepositoryEventList
   */
  public void sendRepositoryEventList(RepositoryEventList eventList) {
    if(notificationEnabled) {
      ProcessRegularMessageJob messageJob = new ProcessRegularMessageJob(eventList);
      notificationThreadPool.execute(messageJob);
    }
  }
  
  public void setNotificationEnabled(boolean notificationEnabled) {
    this.notificationEnabled = notificationEnabled;
  }

  /**
   * Method sendMovedNodesEvents.
   * @param movedNodeRefs Set<NodeRef>
   */
  public void sendMovedNodesEvents(Set<NodeRef> movedNodeRefs) {
    if(notificationEnabled) {
      ProcessMovedNodesJob movedJob = new ProcessMovedNodesJob(movedNodeRefs);
      notificationThreadPool.execute(movedJob);
    }
  }
  
//  public void sendLinkPropertyChangedEvents(Set<NodeRef> linkNodeRefs) {
//	ProcessLinkPropertyChangePropagationJob propagationJob = new ProcessLinkPropertyChangePropagationJob(linkNodeRefs);
//	notificationThreadPool.execute(propagationJob);
//  }
  
  /**
   * Method getNodePath.
   * @param ref NodeRef
   * @return String
   */
  public String getNodePath(NodeRef ref) {
    return nodeService.getPath(ref).toString();
  }
  
  /**
   * Use this method to temporary disable, and possibly re-enable notifications
   * for the current transaction.  This method has the effect of setting a
   * variable in the AlfrescoTransactionSupport area, which acts as a signal
   * to the NotifiableAspectBehavior to queue up notifications for the actions
   * that are about to be performed by this transaction.  This setting has no 
   * effect on notifications for other transactions or other threads.  This setting
   * will automatically clear itself (to re-enable transactions) so the caller does not
   * need to manage this
   * @param enabled false = no notifications.  true = notifications will be sent
   */
  public void setNotificationsEnabledForThisTransaction(boolean enabled) {
    if(enabled){
      AlfrescoTransactionSupport.unbindResource(NotifiableAspectBehavior.NOTIFICATIONS_DISABLED);
    }else{
      AlfrescoTransactionSupport.bindResource(NotifiableAspectBehavior.NOTIFICATIONS_DISABLED, Boolean.TRUE);
    }
      
  }
  
  // private methods
  
  /**
   * Method getNodesLinkedToTarget.
   * @param targetNodeRef NodeRef
   * @return List<NodeRef>
   */
  private List<NodeRef> getNodesLinkedToTarget(NodeRef targetNodeRef) {
    ResultSet results = null;
    try {
      // Find all nodes with a destination property that equals the nodeRef
      String query = "@cm\\:destination:\"" + targetNodeRef.toString() +"\"";
      results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);

      List<NodeRef> links = results.getNodeRefs();

      return links;
    } finally {
      if(results != null) {
        results.close();
      }
    }
  }

 /**
  * Method sendEvents.
  * @param eventList RepositoryEventList
  */
 private void sendEvents(RepositoryEventList eventList) {   
   if(notificationEnabled) {

     if(logger.isDebugEnabled())
       logger.debug("number of events sent = " + eventList.size());
     RepositoryEventMessage message = new RepositoryEventMessage();
     message.setEvents(eventList);

     final String xml = message.toString();


     template.send(destination, new MessageCreator() {
       public Message createMessage(Session session) throws JMSException {
         TextMessage message = session.createTextMessage(xml);
         message.setStringProperty("payloadClass", RepositoryEventMessage.class.getName());
         message.setLongProperty("sent", System.currentTimeMillis());
         return message;
       }
     });

     if(logger.isDebugEnabled())
       logger.debug(xml);
     logger.debug("message generated");
   }
  }

  /**
   * Send messages in a separate thread so transactions complete faster
   * @version $Revision: 1.0 $
   */
  protected class ProcessRegularMessageJob implements Runnable {

    private RepositoryEventList eventList;
    private long queueTime;

    /**
     * Constructor for ProcessRegularMessageJob.
     * @param eventList RepositoryEventList
     */
    public ProcessRegularMessageJob(RepositoryEventList eventList) {
      this.eventList = eventList;
      queueTime = System.currentTimeMillis();
    }

    /**
     * Method run.
     * @see java.lang.Runnable#run()
     */
    public void run() {
      long runTime = System.currentTimeMillis();

      if(logger.isDebugEnabled())
        logger.debug("Took " + (runTime - queueTime) + " ms between queue and execution.");

      // run this as admin
      AuthenticationUtil.setRunAsUserSystem();
      try {
        RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
        {
          public Object execute() throws Throwable {
            sendEvents(eventList);
            return null;
          }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);

      } catch (Exception e) {
        // TODO: might we need to retry the tx for any reason?
        logger.error("Failed to send message digest: " + e.toString());

      } finally {
        NotificationUtils.this.authenticationComponent.clearCurrentSecurityContext();
      }
      
      long endTime = System.currentTimeMillis();
      if(logger.isDebugEnabled()) {
        logger.debug("time to send jms notification: " + (endTime - runTime)/1000 + " seconds");
      }
    }

  }

  /**
   * Process links for moved nodes separately since this may take a while
   * because they have to do a search
   * @version $Revision: 1.0 $
   */
  private class ProcessMovedNodesJob implements Runnable {

    private Set<NodeRef> movedNodes;

    /**
     * Constructor for ProcessMovedNodesJob.
     * @param movedNodes Set<NodeRef>
     */
    public ProcessMovedNodesJob(Set<NodeRef> movedNodes) {
      this.movedNodes = movedNodes;
    }

    /**
     * Method run.
     * @see java.lang.Runnable#run()
     */
    public void run() {
      // run this as admin
      AuthenticationUtil.setRunAsUserSystem();
      try {
        RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
        {
          public Object execute() throws Throwable {
            RepositoryEventList eventList = new RepositoryEventList();

            for(NodeRef node : movedNodes) {
              String newChildPath = getNodePath(node);

              List<NodeRef> nodesLinkedToTarget = getNodesLinkedToTarget(node);
              for (NodeRef link : nodesLinkedToTarget) {
                if (nodeService.exists(link)) {
                  if (logger.isDebugEnabled())        
                    logger.debug("processing linked node " + link.getId());
                  RepositoryEvent targetMovedEvent = new RepositoryEvent(RepositoryEvent.TYPE_TARGET_NODE_MOVED);
                  targetMovedEvent.setNodeId(link.getId());
                  targetMovedEvent.setNodePath(getNodePath(link)); 
                  targetMovedEvent.setPropertyName(RepositoryEvent.PROPERTY_NEW_TARGET_LOCATION);
                  targetMovedEvent.setPropertyValue(newChildPath);
                  targetMovedEvent.setEventPerpetrator(authenticationComponent.getCurrentUserName());
                  targetMovedEvent.setEventTimestamp(System.currentTimeMillis());
                  eventList.add(targetMovedEvent);
                }
              }
            }  
            sendEvents(eventList); 
            return null;
          }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);

      } catch (Exception e) {
        // TODO: might we need to retry the tx for any reason?
        logger.error("Failed to process moved link targets.", e);

      } finally {
    	NotificationUtils.this.authenticationComponent.clearCurrentSecurityContext();
      }
    }
  }  

//  /**
//   * Process links for moved nodes separately since this may take a while
//   * because they have to do a search
//   */
//  private class ProcessLinkPropertyChangePropagationJob implements Runnable {
//
//    private Set<NodeRef> propChangeNodes;
//
//    public ProcessLinkPropertyChangePropagationJob(Set<NodeRef> propChangeNodes) {
//      this.propChangeNodes = propChangeNodes;
//    }
//
//    public void run() {
//      // run this as admin
//      AuthenticationUtil.setRunAsUserSystem();
//      try {
//        RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
//        {
//          public Object execute() throws Throwable {
//            RepositoryEventList eventList = new RepositoryEventList();
//
//            for(NodeRef node : propChangeNodes) {
//
//              List<NodeRef> nodesLinkedToTarget = getNodesLinkedToTarget(node);
//              for (NodeRef link : nodesLinkedToTarget) {
//                if (nodeService.exists(link)) {
//                  if (logger.isDebugEnabled())        
//                    logger.debug("processing linked node " + link.getId());
//                  RepositoryEvent propChangeEvent = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED);
//                  propChangeEvent.setNodeId(link.getId());
//                  propChangeEvent.setNodePath(getNodePath(link)); 
//                  propChangeEvent.setPropertyName(ContentModel.PROP_MODIFIED.toString());
//                  propChangeEvent.setPropertyValue(new Date().toString());
//                  propChangeEvent.setEventPerpetrator(authenticationComponent.getCurrentUserName());
//                  propChangeEvent.setEventTimestamp(System.currentTimeMillis());
//                  eventList.add(propChangeEvent);
//                }
//              }
//            }  
//            sendEvents(eventList); 
//            return null;
//          }
//        };
//        transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);
//
//      } catch (Exception e) {
//        // TODO: might we need to retry the tx for any reason?
//        logger.error("Failed to process property changes link targets.", e);
//
//      } finally {
//    	NotificationUtils.this.authenticationComponent.clearCurrentSecurityContext();
//      }
//    }
//  }

  /**
   * Method setNotificationThreadPool.
   * @param notificationThreadPool PrioritizedThreadPoolExecutor
   */
  public void setNotificationThreadPool(
  	PrioritizedThreadPoolExecutor notificationThreadPool) {
    this.notificationThreadPool = notificationThreadPool;
  }

  /**
   * Method setSearchService.
   * @param searchService SearchService
   */
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * Method setTemplate.
   * @param template JmsTemplate
   */
  public void setTemplate(JmsTemplate template) {
    this.template = template;
  }

  /**
   * Method setDestination.
   * @param destination Destination
   */
  public void setDestination(Destination destination) {
    this.destination = destination;
  }

  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Method setAuthenticationComponent.
   * @param authenticationComponent AuthenticationComponent
   */
  public void setAuthenticationComponent(
  	AuthenticationComponent authenticationComponent) {
    this.authenticationComponent = authenticationComponent;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method setNamespacePrefixResolver.
   * @param namespacePrefixResolver NamespacePrefixResolver
   */
  public void setNamespacePrefixResolver(
  	NamespacePrefixResolver namespacePrefixResolver) {
    this.namespacePrefixResolver = namespacePrefixResolver;
  }  
  
  // setter methods
  



}
