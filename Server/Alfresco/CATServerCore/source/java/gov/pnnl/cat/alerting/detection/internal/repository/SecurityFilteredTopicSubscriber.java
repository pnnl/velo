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
package gov.pnnl.cat.alerting.detection.internal.repository;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.AlertManagementService;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.TemporaryAlert;
import gov.pnnl.cat.alerting.alerts.internal.ActorImpl;
import gov.pnnl.cat.alerting.subscriptions.Subscription;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionService;
import gov.pnnl.cat.jms.common.IEventMessageListener;
import gov.pnnl.cat.jms.common.RepositoryEventMessageHandler;
import gov.pnnl.cat.jms.common.UnmanagedListenerRegistrar;
import gov.pnnl.cat.policy.notifiable.filter.IFilterFunctionality;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEvent;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.Destination;
import javax.jms.MessageConsumer;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 */
public class SecurityFilteredTopicSubscriber implements IEventMessageListener {

  /** List of property names for which a CHANGE_TYPE_MODIFIED event is sent for */
  protected List<QName> applicableContentProperties = new ArrayList<QName>();
  
	private Destination destination;
	private UnmanagedListenerRegistrar unmanagedListenerRegistrar;
	private String selectionCriteria;
	private String username;
	private IFilterFunctionality filterFunctionality;
	private MessageConsumer consumer;
	private SubscriptionService subscriptionService;
	private AlertManagementService alertManagementService;
	private Subscription subscription;
	private NodeService nodeService;
	private NodeUtils nodeUtils;
	private TransactionService transactionService;
	
	private static Log logger = LogFactory.getLog(SecurityFilteredTopicSubscriber.class); 


	/**
	 * Method setTransactionService.
	 * @param transactionService TransactionService
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * Method setNodeUtils.
	 * @param nodeUtils NodeUtils
	 */
	public void setNodeUtils(NodeUtils nodeUtils) {
		this.nodeUtils = nodeUtils;
	}

	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Method setAlertManagementService.
	 * @param alertManagementService AlertManagementService
	 */
	public void setAlertManagementService(
			AlertManagementService alertManagementService) {
		this.alertManagementService = alertManagementService;
	}

	/**
	 * Method setSubscription.
	 * @param subscription Subscription
	 */
	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
	}

	/**
	 * Method setSubscriptionService.
	 * @param subscriptionService SubscriptionService
	 */
	public void setSubscriptionService(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	/**
	 * Method setDestination.
	 * @param destination Destination
	 */
	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	/**
	 * Method setUnmanagedListenerRegistrar.
	 * @param unmanagedListenerRegistrar UnmanagedListenerRegistrar
	 */
	public void setUnmanagedListenerRegistrar(UnmanagedListenerRegistrar unmanagedListenerRegistrar) {
		this.unmanagedListenerRegistrar = unmanagedListenerRegistrar;
	}

	/**
	 * Method setFilterFunctionality.
	 * @param filterFunctionality IFilterFunctionality
	 */
	public void setFilterFunctionality(IFilterFunctionality filterFunctionality) {
		this.filterFunctionality = filterFunctionality;
	}

	/**
	 * Method setUsername.
	 * @param username String
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Method setSelectionCriteria.
	 * @param selectionCriteria String
	 */
	public void setSelectionCriteria(String selectionCriteria) {
		this.selectionCriteria = selectionCriteria;
	}

	/**
	 * Method getSelectionCriteria.
	 * @return String
	 */
	public String getSelectionCriteria() {
		return selectionCriteria;
	}

	/**
	 * Subscribe to topic
	 */
	public void startListening(){

		filterFunctionality.setUser(username);


		RepositoryEventMessageHandler handler = new RepositoryEventMessageHandler();
		handler.setListener(this);
		consumer = unmanagedListenerRegistrar.newMessageListener(handler, destination, selectionCriteria);
	}

	/**
	 * Stop subscribing
	 *
	 */
	public void stopListening() {
		unmanagedListenerRegistrar.removeMessageListener(consumer);
	}

	/**
	 * Method handleEventMessage.
	 * @param message RepositoryEventMessage
	 * @see gov.pnnl.cat.jms.common.IEventMessageListener#handleEventMessage(RepositoryEventMessage)
	 */
	public void handleEventMessage(final RepositoryEventMessage message) {
	  RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
	  {
      public Object execute() throws Throwable 
      {
        try {
          RepositoryEventMessage userFilteredMessage = filterFunctionality.filterMessage(message);
          if (userFilteredMessage.getEvents().size() == 0) {
            return null;
          }

          // passed security filter
          
          // there really should only be one event, but loop just in case 
          for (RepositoryEvent repEvent : userFilteredMessage.getEvents()) {
            // for each event passed to us, see if we want to create a temporary alert
            // temporary alerts should be created if a new node is created, a node is
            // deleted, or the content property has changed

            NodeRef nodeRef = new NodeRef(CatConstants.SPACES_STORE, repEvent.getNodeId());

            // create an Event object to store what's happened
            Event event = alertManagementService.newEvent();
            
            // first, look at the type of event and see if we need to continue
            event.setEventTime(new Date(repEvent.getEventTimestamp()));
            event.setUUID(repEvent.getNodeId());
            
            // we can't read the name property because the node may not exist anymore
            // so read the name as the last parameter on the path
            String filename = getFilenameFromQualifiedPath(repEvent.getNodePath());
            event.setResourceName(filename);
            event.setResourceURL(nodeUtils.getBrowseUrlForNodeRef(nodeRef));

            // see if this is an event we care about
            String changeType = null;
            String eventType = repEvent.getEventType();
            if (eventType.equals(RepositoryEvent.TYPE_NODE_ADDED)) {
              // we want all node creation events
              changeType = AlertingConstants.CHANGE_TYPE_NEW;
              
            } else if (eventType.equals(RepositoryEvent.TYPE_NODE_REMOVED)) {
              // we want all node removal events
              changeType = AlertingConstants.CHANGE_TYPE_DELETED;
              
            } else if (eventType.equals(RepositoryEvent.TYPE_PROPERTY_CHANGED)) {
              // we only want prop changed events if it is a content property registered with the bean
              for (QName contentPropName : applicableContentProperties) {
                if (repEvent.getPropertyName().equals(contentPropName.toString())) {
                  changeType = AlertingConstants.CHANGE_TYPE_MODIFIED;
                }
              }
            }
            
            // compare this change type with the ones the subscriber wants to be told about
            // based on the PROP_SUB_REP_CHANGE_TYPE property of the subscription
            List<String> desiredChangeTypes = (List<String>)subscription.getParameters().get(AlertingConstants.PROP_SUB_REP_CHANGE_TYPE);
            
            if (changeType != null && desiredChangeTypes.contains(changeType)) {
              // we have an event we care about, so create a temp alert from it
              event.setChangeType(changeType);

              String eventPerpetrator = repEvent.getEventPerpetrator();


              Actor actor = new ActorImpl();
              ((ActorImpl)actor).setUsername(eventPerpetrator);
              event.setEventPerpetrator(actor);

              TemporaryAlert tempAlert = alertManagementService.newTemporaryAlert();

              tempAlert.setNodeRef(nodeRef);
              tempAlert.setEvent(event);

              subscriptionService.addTemporaryAlert(subscription, tempAlert); 
            }
          }
        } catch (Exception e) {
          logger.error("Exception", e);
        }
        return null;
      }
	  };
	  transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);
		
	}


	/**
	 * Method getFilenameFromQualifiedPath.
	 * @param path String
	 * @return String
	 */
	private String getFilenameFromQualifiedPath(String path) {
		if (path == null || path.length() < 2) {
			return null;
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		int index = path.lastIndexOf('/');
		if (index < 0) {
			return null;
		}

		String filenameWithPrefix = ISO9075.decode(path.substring(index + 1));
		
		index = filenameWithPrefix.indexOf(':');
		if (index < 0) {
			return filenameWithPrefix;
		}
		return filenameWithPrefix.substring(index + 1);
	}

	
	/**
   * Set the list of types for which this action is applicable
   * 
   * @param applicableTypes   arry of applicable types
   */
  public void setApplicableContentProperties(String[] applicableTypes)
  {
      for (String type : applicableTypes)
      {
          this.applicableContentProperties.add(QName.createQName(type));
      }
  }

}
