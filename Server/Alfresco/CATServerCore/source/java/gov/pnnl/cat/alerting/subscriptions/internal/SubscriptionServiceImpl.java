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
package gov.pnnl.cat.alerting.subscriptions.internal;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.TemporaryAlert;
import gov.pnnl.cat.alerting.alerts.internal.TemporaryAlertImpl;
import gov.pnnl.cat.alerting.delivery.DeliveryChannel;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;
import gov.pnnl.cat.alerting.exceptions.InvalidSubscriptionException;
import gov.pnnl.cat.alerting.exceptions.TooManySubscriptionsException;
import gov.pnnl.cat.alerting.subscriptions.Frequency;
import gov.pnnl.cat.alerting.subscriptions.Subscription;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionListener;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionOwner;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionService;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionType;
import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.XmlUtility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class SubscriptionServiceImpl extends ExtensiblePolicyAdapter implements SubscriptionService, OnUpdateNodePolicy,
BeforeDeleteNodePolicy, OnCreateNodePolicy, TransactionListener, ApplicationContextAware, ApplicationListener {

	/**
	 */
	protected enum SubscriptionEventType {
		CREATED, UPDATED, DELETED 
	}
	private static final String SUBSCRIPTION_EVENT_LIST = "subscriptionEventList";
  
	//Logger
  private static final Log logger = LogFactory.getLog(SubscriptionServiceImpl.class);
  
	private List<SubscriptionListener> subscriptionListeners = new ArrayList<SubscriptionListener>();
	private Map<QName, DeliveryChannel> deliveryChannelMap = new HashMap<QName, DeliveryChannel>();
	private Map<String, String> subscriptionTypeMimeTypeMap;
	private List<SubscriptionType> subscriptionTypes;
	private int maxSubscriptions;
	private boolean applicationStarted = false;
  private ApplicationContext applicationContext = null;

	/**
	 * Need to bind to the subscription type policy for OnCreateNodePolicy, OnUpdateNodePolicy, and
	 * OnDeleteNodePolicy on TRANSACTION_COMMIT.  Use ExtensiblePolicy Framework for this.
	 * 
	 * Check to see if the server contains the proper container for storing subscriptions
	 * (i.e., /Company Home/Subscriptions).  If not, create it.  Should be a system folder.
	 * 
	
	 * @see gov.pnl.dmi.policy.ExtensiblePolicyAdapter.init() */
	public void init() {

	     this.policyComponent.bindClassBehaviour(
	             QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
	             this,
	             new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.FIRST_EVENT));
	     
	     this.policyComponent.bindClassBehaviour(
	             QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateNode"),
	             this,
	             new JavaBehaviour(this, "onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT));
	     
	     this.policyComponent.bindClassBehaviour(
	             QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
	             this,
	             new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
	}

	/**
	 * Method isSubscriptionNode.
	 * @param nodeRef NodeRef
	 * @return boolean
	 */
	private boolean isSubscriptionNode(NodeRef nodeRef) {
	  if(nodeService.exists(nodeRef)) {
	    QName type = nodeService.getType(nodeRef);
	    SubscriptionTypeImpl subType = new SubscriptionTypeImpl(type);
	    return getSubscriptionTypes().contains(subType);
	  }
	  return false;
	}
	
	/**
	 * Need to bind this class as the tx listener.  Then need to add the NodeRef and an event flag to
	 * the tx context.  See NotifiableAspectBehavior for an example of how to do this.
	 * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
	 */
	@SuppressWarnings({ "unchecked" })
  public void onCreateNode(ChildAssociationRef childAssocRef) {
	  if(!applicationStarted) {
	    return;
	  }
	  	  
	  
	  if(isSubscriptionNode(childAssocRef.getChildRef())){
  	  // validate that this node being created is not going to cause the user
  		// to have too many subscriptions.  throw exception if this is the case
  
  		// make sure our afterCommit() method gets called when the transaction commits
  		AlfrescoTransactionSupport.bindListener(this);
  
  		// get the list of any other pending actions to perform at commit time
  		List<TransactionPendingAction> eventList = (List<TransactionPendingAction>)AlfrescoTransactionSupport.getResource(SUBSCRIPTION_EVENT_LIST);
  		if (eventList == null) {
  			eventList = new ArrayList<TransactionPendingAction>();
  			AlfrescoTransactionSupport.bindResource(SUBSCRIPTION_EVENT_LIST, eventList);
  		}
  		// add a new action to the list
  		TransactionPendingAction action = new TransactionPendingAction();
      action.setSubscription(getSubscription(childAssocRef.getChildRef()));
  		action.setType(SubscriptionEventType.CREATED);
  		eventList.add(action);
	  }
	}

	/**
	 * Need to bind this class as the tx listener.  Then need to check if the event flag has been set
	 * to CREATE.  If so, then just ignore this method.  Otherwise, need to add the NodeRef and an event flag to
	 * the tx context.  See NotifiableAspectBehavior for an example of how to do this.
	 * @see org.alfresco.repo.node.NodeServicePolicies$OnUpdateNodePolicy#onUpdateNode(org.alfresco.service.cmr.repository.NodeRef)
	 */
	@SuppressWarnings("unchecked")
  public void onUpdateNode(NodeRef nodeRef) {
	  if(!applicationStarted) {
	    return;
	  }

	  if(isSubscriptionNode(nodeRef)) {
  	  // make sure our afterCommit() method gets called when the transaction commits
  		AlfrescoTransactionSupport.bindListener(this);
  
  		// get the list of any other pending actions to perform at commit time
  		List<TransactionPendingAction> eventList = (List<TransactionPendingAction>)AlfrescoTransactionSupport.getResource(SUBSCRIPTION_EVENT_LIST);
  		if (eventList == null) {
  			eventList = new ArrayList<TransactionPendingAction>();
  			AlfrescoTransactionSupport.bindResource(SUBSCRIPTION_EVENT_LIST, eventList);
  		}
  		// make sure this event is not already queued
  		// look for any action of the same noderef that is either a creation or update event
  		boolean found = false;
  		for (TransactionPendingAction action : eventList) {
  			if (action.getSubscription().getNodeRef().equals(nodeRef) && 
  					(action.getType() == SubscriptionEventType.CREATED || 
  							action.getType() == SubscriptionEventType.UPDATED)) {
  				found = true;
  				break;
  			}
  		}
  		// add a new action to the list if it isn't there already
  		if (!found) {
  			TransactionPendingAction action = new TransactionPendingAction();
        action.setSubscription(getSubscription(nodeRef));
  			action.type = SubscriptionEventType.UPDATED;
  			eventList.add(action);
  		}
	  }
	}

	
	/* (non-Javadoc)
	 * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public void beforeDeleteNode(NodeRef deletedNode) {
	  if(!applicationStarted) {
	    return;
	  }
	  
	  if(isSubscriptionNode(deletedNode)){
  	  // make sure our afterCommit() method gets called when the transaction commits
  		AlfrescoTransactionSupport.bindListener(this);
  
  		// get the list of any other pending actions to perform at commit time
  		List<TransactionPendingAction> eventList = (List<TransactionPendingAction>)AlfrescoTransactionSupport.getResource(SUBSCRIPTION_EVENT_LIST);
  		if (eventList == null) {
  			eventList = new ArrayList<TransactionPendingAction>();
  			AlfrescoTransactionSupport.bindResource(SUBSCRIPTION_EVENT_LIST, eventList);
  		}
  		// add a new action to the list
  		TransactionPendingAction action = new TransactionPendingAction();
  		action.setSubscription(getSubscription(deletedNode));
  		action.setType(SubscriptionEventType.DELETED);
  		eventList.add(action);
		}
	}


	/**
	 * Make sure events are only sent out after all policy has ran, and the tx is completing OK.
	
	 * @see org.alfresco.repo.transaction.TransactionListener#afterCommit */
	public void afterCommit() {

		// retrieve the list of pending events
		List<TransactionPendingAction> eventList = (List<TransactionPendingAction>)AlfrescoTransactionSupport.getResource(SUBSCRIPTION_EVENT_LIST);
		if (eventList == null) {
			return;
		}

		// Run this in a background job because we need to be in a transaction
		UpdateSubscriptionCacheJob job = new UpdateSubscriptionCacheJob(eventList);
    lowPriorityThreadPool.execute(job); 
	}
	
	/**
	 * Run this as a background job in a new tx so it can have access to the hibernate transactional caches.
	 * @version $Revision: 1.0 $
	 */
	private class UpdateSubscriptionCacheJob implements Runnable {
	  private List<TransactionPendingAction> eventList;

	  /**
	   * Constructor for UpdateSubscriptionCacheJob.
	   * @param eventList List<TransactionPendingAction>
	   */
	  public UpdateSubscriptionCacheJob(List<TransactionPendingAction> eventList) {
	    this.eventList = eventList;
	  }

	  /**
	   * Method run.
	   * @see java.lang.Runnable#run()
	   */
	  public void run() {
	    
      try {
        // This needs to run in a tx since it calls the NodeService
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
          public Object execute() throws Throwable {
            
            AuthenticationUtil.setRunAsUserSystem();
            
            // iterate through the pending events
            for (TransactionPendingAction action : eventList) {
              NodeRef subscriptionNode = action.getSubscription().getNodeRef();

              // inform all of the SubscriptionListener objects
              for (SubscriptionListener listener : subscriptionListeners) {

                // invoke the method to handle the event
                switch (action.getType()) {
                case CREATED:
                  listener.subscriptionCreated(subscriptionNode);
                  break;
                case UPDATED:
                  listener.subscriptionChanged(subscriptionNode);
                  break;
                case DELETED:
                  listener.subscriptionDeleted(action.getSubscription());
                  break;
                }
              }
            }
            return null;
          }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

      } catch (Throwable e) {
        logger.error("Failed to update subscription service cache.", e);
      }
	  }

	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
	 */
	public void afterRollback() {
		// no implementation needed

	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
	 */
	public void beforeCommit(boolean readOnly) {
		// no implementation needed

	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
	 */
	public void beforeCompletion() {
		// no implementation needed

	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.transaction.TransactionListener#flush()
	 */
	public void flush() {
		// no implementation needed

	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#addSubscriptionListener(gov.pnl.dmi.alerting.subscriptions.SubscriptionListener)
	 */
	public void addSubscriptionListener(SubscriptionListener listener) {
		subscriptionListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#createSubscription(gov.pnl.dmi.alerting.subscriptions.Subscription)
	 */
	public Subscription createSubscription(Subscription newSubscription) throws InvalidSubscriptionException, TooManySubscriptionsException {

		String username = newSubscription.getOwner().getAccountId();
		// TODO: change this so it handles groups

		NodeRef userSubscriptionFolder = getUserSubscriptionsFolder(username);

		// check to make sure we won't exceed the limit on number of subscriptions
		int childrenCount = nodeService.getChildAssocs(userSubscriptionFolder).size();
		if (childrenCount >= maxSubscriptions) {
			throw new TooManySubscriptionsException();
		}

		validateSubscription(newSubscription);

		QName subscriptionType = newSubscription.getType();
		String namespace = subscriptionType.getNamespaceURI();

		Map<QName, Serializable> properties = newSubscription.getParameters();

		NodeRef subscriptionNode = nodeService.createNode(
				userSubscriptionFolder,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(AlertingConstants.NAMESPACE_SUBSCRIPTION, newSubscription.getName()),
				subscriptionType,
				properties).getChildRef();

		newSubscription.setNodeRef(subscriptionNode);


		/*
		 * get Subscription Owner
		 * locate Owner's subscription folder
		 * compare count of children to maxSubscriptions, throw exception if true
		 * 
		 * note: onCreateNode policy is also performing this check.  might as well throw an exception
		 *     now rather than wait until transaction commit time.
		 * 
		 * lookup subscription type based on type in newSubscription
		 * check constraints on subscription type against newsubscription (like frequency)
		 * throw InvalidSubscriptionException if fails constraint
		 * 
		 * create new subscription node in owner's subscription folder
		 * set properties as appropriate
		 * save node
		 * when transaction commits, policy will run to finish the process
		 */
		return newSubscription;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#deleteSubscriptions(java.util.List)
	 */
	public void deleteSubscriptions(List<Subscription> subscriptions) {
		// just delete the nodes associated with the subscriptions
		// onDeleteNode policy will take care of the rest
		for (Subscription subscription : subscriptions) {
			NodeRef subNodeRef = subscription.getNodeRef();
			nodeService.deleteNode(subNodeRef);
		}

	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#getDeliveryChannels()
	 */
	public List<DeliveryChannel> getDeliveryChannels() {
		if (deliveryChannelMap == null) {
			return null;
		}
		return new ArrayList<DeliveryChannel>(deliveryChannelMap.values());
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#getSubscription(org.alfresco.service.cmr.repository.NodeRef)
	 */
	public Subscription getSubscription(NodeRef subscriptionRef) {
		Map<QName, Serializable> properties = nodeService.getProperties(subscriptionRef);
		QName subscriptionType = nodeService.getType(subscriptionRef);
		
		SubscriptionImpl subscription = new SubscriptionImpl(subscriptionType);
		subscription.setNodeRef(subscriptionRef);
		subscription.setParameters(properties);
		subscription.setSubscriptionOwner(getSubscriptionOwner(subscriptionRef));
		
		return subscription;
		
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#getSubscriptions()
	 */
	public List<Subscription> getSubscriptions() {
	  ResultSet results = null;
	  try {
	    // perform a search for all nodes with sub:subscription type
	    String query = "TYPE:\"" + AlertingConstants.TYPE_SUBSCRIPTION.toString() + "\"";
	    results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());

	    List<NodeRef> subscriptionNodeRefs = results.getNodeRefs();
	    List<Subscription> subscriptions = new ArrayList<Subscription>();
	    for (NodeRef nodeRef : subscriptionNodeRefs) {
	      Subscription subscription = getSubscription(nodeRef);
	      subscriptions.add(subscription);
	    }
	    return subscriptions;
	  } finally {
	    if(results != null) {
	      results.close();
	    }
	  }
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#getSubscriptions(java.lang.String)
	 */
	public List<Subscription> getSubscriptions(String username) {
		NodeRef usernameSubscriptionFolder = getUserSubscriptionsFolder(username);
		List<ChildAssociationRef> children = nodeService.getChildAssocs(usernameSubscriptionFolder);
		List<Subscription> subscriptions = new ArrayList<Subscription>();
		for (ChildAssociationRef child : children) {
			NodeRef childNode = child.getChildRef();
			Subscription subscription = getSubscription(childNode);
			subscriptions.add(subscription);
		}
		return subscriptions;
	}
	
	/**
	 * Method getSubscriptionType.
	 * @param subscriptionTypeName QName
	 * @return SubscriptionType
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionService#getSubscriptionType(QName)
	 */
	public SubscriptionType getSubscriptionType(QName subscriptionTypeName) {
		List<SubscriptionType> types = getSubscriptionTypes();
		for (SubscriptionType type : types) {
			if (type.getName().equals(subscriptionTypeName)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * These can be created dynamically by querying the Alfresco Dictionary to see which types are
	 * subtypes of sub:subscription.  For each subtype, you can instantiate a new SubscriptionType
	 * object.  You can fill out the "allowed frequencies" by querying the frequencyConstraint
	 * for that subtype (note you need to use the right namespace).
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionService#getSubscriptionTypes()
	 */
	public List<SubscriptionType> getSubscriptionTypes() {
		if (this.subscriptionTypes != null) {
			return this.subscriptionTypes;
		}
		
		Collection<QName> allTypes = dictionaryService.getAllTypes();
		List<SubscriptionType> subscriptionTypes = new ArrayList<SubscriptionType>();
		
		// iterate through all types, looking for subtypes of TYPE_SUBSCRIPTION
		// for some reason, isSubClass() returns true if the class is the same as the subclass
		// so make sure we filter that one out
		for (QName type : allTypes) {
			if (dictionaryService.isSubClass(type, AlertingConstants.TYPE_SUBSCRIPTION) &&
					(type.equals(AlertingConstants.TYPE_SUBSCRIPTION) == false)) {
				// found a subtype
				SubscriptionTypeImpl subscriptionType = new SubscriptionTypeImpl();
				subscriptionType.setName(type);
				TypeDefinition typeDef = dictionaryService.getType(type);
				
				// get all properties of this type
				Map<QName, PropertyDefinition> properties = typeDef.getProperties();
				for (QName propQName : properties.keySet()) {
					if (propQName.getNamespaceURI().equals(type.getNamespaceURI())) {
						// this property shares the same namespace as our subscription type
						// so add it to the SubscriptionType object
						PropertyDefinition propDefinition = dictionaryService.getProperty(propQName);
						if (propQName.getLocalName().equals(AlertingConstants.PROP_SUB_FREQUENCY_LOCALNAME)) {
							// this is the frequency property, treat it special
							// look up from the constraint what the allowed frequencies are
							// set in the subscriptionType

							List<ConstraintDefinition> constraints = propDefinition.getConstraints();
							for (ConstraintDefinition constraintDef : constraints) {
								Constraint constraint = constraintDef.getConstraint();
								if (constraint instanceof ListOfValuesConstraint) {
									List<Frequency> frequencies = new ArrayList<Frequency>();
									ListOfValuesConstraint listConstraint = (ListOfValuesConstraint)constraint;
									List<String> allowedFrequencyStringList = listConstraint.getAllowedValues();
									for (String allowedFrequency : allowedFrequencyStringList) {
										frequencies.add(new Frequency(allowedFrequency));
									}
									subscriptionType.setAllowedFrequencies(frequencies);
								}
							}
						} else {
							subscriptionType.getParameterDefinitions().put(propQName, propDefinition);
						}
					}
				}
				String mimetype = subscriptionTypeMimeTypeMap.get(type.toPrefixString());
				subscriptionType.setMimetype(mimetype);
				
				subscriptionTypes.add(subscriptionType);
			}
		}
		this.subscriptionTypes = subscriptionTypes;
		return subscriptionTypes;
		
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#removeSubscriptionListener(gov.pnl.dmi.alerting.subscriptions.SubscriptionListener)
	 */
	public void removeSubscriptionListener(SubscriptionListener listener) {
		subscriptionListeners.remove(listener);
	}

	/**
	 * Delivery channels need to be injected by spring
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionService#setDeliveryChannels(java.util.List)
	 */
	public void setDeliveryChannels(List<DeliveryChannel> deliveryChannels) {
		for (DeliveryChannel deliveryChannel : deliveryChannels) {
			QName channelName = deliveryChannel.getName();
			deliveryChannelMap.put(channelName, deliveryChannel);
		}

	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#setMaxSubscriptions(int)
	 */
	public void setMaxSubscriptions(int maxSubscriptions) {
		this.maxSubscriptions = maxSubscriptions;

	}

	/**
	 * Method setSubscriptionTypeMimeTypeMap.
	 * @param subscriptionTypeMimeTypeMap Map<String,String>
	 */
	public void setSubscriptionTypeMimeTypeMap(
			Map<String, String> subscriptionTypeMimeTypeMap) {
		this.subscriptionTypeMimeTypeMap = subscriptionTypeMimeTypeMap;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#setSubscriptionTypes(java.util.List)
	 */
	public void setSubscriptionTypes(List<SubscriptionType> subscriptionTypes) {
		this.subscriptionTypes = subscriptionTypes;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionService#updateSubscription(gov.pnl.dmi.alerting.subscriptions.Subscription)
	 */
	public Subscription updateSubscription(Subscription subscription) throws InvalidSubscriptionException {
		NodeRef subNodeRef = subscription.getNodeRef();
		Map<QName, Serializable> properties = nodeService.getProperties(subNodeRef);

		// overwrite any properties using the subscription object provided
		properties.putAll(subscription.getParameters());

		// update node properties.  Node policy will do the rest
		nodeService.setProperties(subNodeRef, properties);
		return subscription;
	}


	/**
	 * Method addTemporaryAlert.
	 * @param subscription Subscription
	 * @param alert TemporaryAlert
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionService#addTemporaryAlert(Subscription, TemporaryAlert)
	 */
	public void addTemporaryAlert(Subscription subscription, TemporaryAlert alert) {
		NodeRef subscriptionNode = subscription.getNodeRef();
		NodeRef tempAlertFolder = getTempAlertsFolder(subscriptionNode);
		
		String name = alert.getEvent().getResourceName() + "-" + System.currentTimeMillis();

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, name);

		String eventXML = XmlUtility.serialize(alert.getEvent());
		
		NodeRef tempAlertNode = nodeService.createNode(
				tempAlertFolder,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(AlertingConstants.NAMESPACE_ALERT, name),
				AlertingConstants.TYPE_TEMPORARY_ALERT,
				properties).getChildRef();
		
		ContentWriter writer = contentService.getWriter(tempAlertNode, AlertingConstants.PROP_CONTENT, true);
	    writer.setMimetype(MimetypeMap.MIMETYPE_XML);
	    writer.setEncoding("UTF-8");
		writer.putContent(eventXML);
	}

	/**
	 * Method clearTemporaryAlerts.
	 * @param subscription Subscription
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionService#clearTemporaryAlerts(Subscription)
	 */
	public void clearTemporaryAlerts(Subscription subscription) {
		// get all of the child nodes of the subscription node
		NodeRef subscriptionNode = subscription.getNodeRef();
		List<ChildAssociationRef> tempAlertFolders = nodeService.getChildAssocs(subscriptionNode);

		for (ChildAssociationRef tempAlertFolderCARef : tempAlertFolders) {
			// if the child association is of the type for connecting a subscription to the temp_alerts_folder
			// remove all children nodes from that folder
			if (tempAlertFolderCARef.getTypeQName().equals(AlertingConstants.ASSOC_TEMP_ALERTS_CONTAINER)) {
				NodeRef tempAlertsFolder = tempAlertFolderCARef.getChildRef();
				List<ChildAssociationRef> tempAlerts = nodeService.getChildAssocs(tempAlertsFolder);
				for (ChildAssociationRef tempAlertCARef: tempAlerts) {
					nodeService.deleteNode(tempAlertCARef.getChildRef());
				}
			}
		}
	}

	/**
	 * Method getTemporaryAlerts.
	 * @param subscription Subscription
	 * @return List<TemporaryAlert>
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionService#getTemporaryAlerts(Subscription)
	 */
	public List<TemporaryAlert> getTemporaryAlerts(Subscription subscription) {
		List<TemporaryAlert> temporaryAlerts = new ArrayList<TemporaryAlert>();
		
		// get all of the child nodes of the subscription node
		NodeRef subscriptionNode = subscription.getNodeRef();
		List<ChildAssociationRef> children = nodeService.getChildAssocs(subscriptionNode);
		
		for (ChildAssociationRef childRef : children) {
			// if the child association is of the type for connecting a subscription to the temp_alerts_folder
			// dive into it and create a Temp Alert object for each
			if (childRef.getTypeQName().equals(AlertingConstants.ASSOC_TEMP_ALERTS_CONTAINER)) {
				NodeRef tempAlertFolder = childRef.getChildRef();
				List<ChildAssociationRef> tempAlertChildren = nodeService.getChildAssocs(tempAlertFolder);
				
				for (ChildAssociationRef tempAlertChildRef : tempAlertChildren) {
					NodeRef tempAlertNodeRef = tempAlertChildRef.getChildRef();
					TemporaryAlert tempAlert = createTemporaryAlertFromNodeRef(tempAlertNodeRef);
					temporaryAlerts.add(tempAlert);
				}
			}
		}
		return temporaryAlerts;
	}
	
	/**
	 * Method createTemporaryAlertFromNodeRef.
	 * @param nodeRef NodeRef
	 * @return TemporaryAlert
	 */
	private TemporaryAlert createTemporaryAlertFromNodeRef(NodeRef nodeRef) {

		ContentReader reader = contentService.getReader(nodeRef, AlertingConstants.PROP_CONTENT);
		reader.setEncoding("UTF-8");
		reader.setMimetype(MimetypeMap.MIMETYPE_XML);
		String eventXml = reader.getContentString();
	    
		Event event = XmlUtility.deserialize(eventXml);
		
		TemporaryAlert tempAlert = new TemporaryAlertImpl();
		tempAlert.setEvent(event);
		tempAlert.setNodeRef(nodeRef);
		
		return tempAlert;
	}

	/**
	 * Method send.
	 * @param subscription Subscription
	 * @param alert Alert
	 * @throws DeliveryException
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionService#send(Subscription, Alert)
	 */
	public void send(Subscription subscription, Alert alert)
	throws DeliveryException {
		// get all of the delivery channels for this subscription
		// pass the alert to each for delivery
		List<QName> subDeliveryChannels = subscription.getDeliveryChannels();
		for (QName subDeliveryChannel : subDeliveryChannels) {
			DeliveryChannel deliveryChannel = deliveryChannelMap.get(subDeliveryChannel);
			deliveryChannel.send(alert);
		}
		

	}

	/**
	 * Method newSubscriptionInstance.
	 * @param type SubscriptionType
	 * @return Subscription
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionService#newSubscriptionInstance(SubscriptionType)
	 */
	public Subscription newSubscriptionInstance(SubscriptionType type) {
		return new SubscriptionImpl(type.getName());
	}



	/**
	 * Get the Company Home/Subscriptions folder.  Create if it doesn't exist.
	
	 * @return NodeRef
	 */
	private NodeRef getSubscriptionsFolder() {
		NodeRef companyHomeNode = nodeUtils.getCompanyHome();
		NodeRef subscriptionsNode = nodeUtils.getChildByName(companyHomeNode, AlertingConstants.NAME_SUBSCRIPTIONS_FOLDER.getLocalName());

		if (subscriptionsNode == null) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, AlertingConstants.NAME_SUBSCRIPTIONS_FOLDER.getLocalName());

			String currentUser = authenticationComponent.getCurrentUserName();
			// switch to admin privs
			AuthenticationUtil.setRunAsUserSystem();

			subscriptionsNode = nodeService.createNode(
					companyHomeNode,
					ContentModel.ASSOC_CONTAINS,
					AlertingConstants.NAME_SUBSCRIPTIONS_FOLDER,
					ContentModel.TYPE_SYSTEM_FOLDER,
					properties).getChildRef();

			// go back to user privs
			if (currentUser != null) {
				AuthenticationUtil.setRunAsUser(currentUser);
			}
		}

		return subscriptionsNode;
	}

	/**
	 * Method getUserSubscriptionsFolder.
	 * @param username String
	 * @return NodeRef
	 */
	private NodeRef getUserSubscriptionsFolder(String username) {
		NodeRef subscriptionsFolder = getSubscriptionsFolder();
		NodeRef userSubscriptionNode = nodeUtils.getChildByName(subscriptionsFolder, username);

		if (userSubscriptionNode == null) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, username);

			String currentUser = authenticationComponent.getCurrentUserName();
			// switch to admin privs
			AuthenticationUtil.setRunAsUserSystem();

			userSubscriptionNode = nodeService.createNode(
					subscriptionsFolder,
					ContentModel.ASSOC_CONTAINS,
					QName.createQName(AlertingConstants.NAMESPACE_SUBSCRIPTION, username),
					ContentModel.TYPE_SYSTEM_FOLDER,
					properties).getChildRef();
			
			permissionService.setPermission(userSubscriptionNode, username, PermissionService.ALL_PERMISSIONS, true);
			permissionService.setInheritParentPermissions(userSubscriptionNode, false);

			// go back to user privs
			if (currentUser != null) {
				AuthenticationUtil.setRunAsUser(currentUser);
			}
		}

		return userSubscriptionNode;

	}
	
	/**
	 * Method getTempAlertsFolder.
	 * @param subscriptionNode NodeRef
	 * @return NodeRef
	 */
	private NodeRef getTempAlertsFolder(NodeRef subscriptionNode) {
		NodeRef tempAlertsFolder = nodeUtils.getChildByName(subscriptionNode, AlertingConstants.NAME_TEMPORARY_ALERTS_FOLDER.getLocalName());

		if (tempAlertsFolder == null) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, AlertingConstants.NAME_TEMPORARY_ALERTS_FOLDER.getLocalName());

			tempAlertsFolder = nodeService.createNode(
					subscriptionNode,
					AlertingConstants.ASSOC_TEMP_ALERTS_CONTAINER,
					AlertingConstants.NAME_TEMPORARY_ALERTS_FOLDER,
					ContentModel.TYPE_SYSTEM_FOLDER,
					properties).getChildRef();
		}

		return tempAlertsFolder;

	}

	/**
	 * Method getDeliveryChannelsFromQName.
	 * @param deliveryChannelQNames List<QName>
	 * @return List<DeliveryChannel>
	 */
	private List<DeliveryChannel> getDeliveryChannelsFromQName(List<QName> deliveryChannelQNames) {
		List<DeliveryChannel> deliveryChannels = new ArrayList<DeliveryChannel>();
		for (QName qname : deliveryChannelQNames) {
			deliveryChannels.add(deliveryChannelMap.get(qname));
		}
		return deliveryChannels;
	}

	/**
	 * Method getSubscriptionOwner.
	 * @param subscriptionNode NodeRef
	 * @return SubscriptionOwner
	 */
	private SubscriptionOwner getSubscriptionOwner(NodeRef subscriptionNode) {
		// TODO: Make better to handle groups
		NodeRef ownerFolder = nodeService.getPrimaryParent(subscriptionNode).getParentRef();
		final String accountId = (String)nodeService.getProperty(ownerFolder, ContentModel.PROP_NAME);
		final String accountType = SubscriptionOwner.ACCOUNT_USER;
		
		return new SubscriptionOwner() {

			public String getAccountId() {
				return accountId;
			}

			public String getAccountType() {
				return accountType;
			}
			
		};
	}
	
	/**
	 * Method validateSubscription.
	 * @param subscription Subscription
	 * @throws InvalidSubscriptionException
	 */
	private void validateSubscription(Subscription subscription) throws InvalidSubscriptionException {
		SubscriptionType subType = this.getSubscriptionType(subscription.getType());
		
		// check frequency against allowed frequencies
		List<Frequency> allowedFrequencies = subType.getAllowedFrequencies();
		Frequency subFrequency = subscription.getFrequency();
		if (allowedFrequencies.contains(subFrequency) == false) {
			// selected frequency not a permitted one.  throw exception.
			throw new InvalidSubscriptionException("Frequency '" + subFrequency + 
					"' not permitted for this subscription type");
		}
		
		// make sure required properties are filled in
		// throw exception if criteria not met
		Map<QName, PropertyDefinition> requiredParams = subType.getParameterDefinitions();
		Map<QName, Serializable> subParams = subscription.getParameters();
		
		for (QName requiredQName : requiredParams.keySet()) {
			if (subParams.containsKey(requiredQName) == false) {
				throw new InvalidSubscriptionException("Missing required parameter: '" + requiredQName + "'");
			}
		}
		
		// TODO do we need more checking?  The integrity checker will handle more when
		// the node is saved
	}


	// private class to store a pending event to be performed at transaction commit
	/**
	 */
	private class TransactionPendingAction {
		private SubscriptionEventType type; // type of event to perform
		private Subscription subscription; // node to perform the event on
		/**
		 * Method getType.
		 * @return SubscriptionEventType
		 */
		public SubscriptionEventType getType() {
			return type;
		}
		/**
		 * Method setType.
		 * @param type SubscriptionEventType
		 */
		public void setType(SubscriptionEventType type) {
			this.type = type;
		}
		/**
		 * Method getSubscription.
		 * @return Subscription
		 */
		public Subscription getSubscription() {
			return subscription;
		}
		/**
		 * Method setSubscription.
		 * @param subscription Subscription
		 */
		public void setSubscription(Subscription subscription) {
			this.subscription = subscription;
		}

	}


  /**
   * Method onApplicationEvent.
   * @param event ApplicationEvent
   */
  @Override
  public void onApplicationEvent(ApplicationEvent event) {

    if (event instanceof ContextRefreshedEvent)
    {
      ContextRefreshedEvent refreshEvent = (ContextRefreshedEvent)event;
      ApplicationContext refreshContext = refreshEvent.getApplicationContext();
      if (refreshContext != null && refreshContext.equals(applicationContext))
      {
         applicationStarted = true;
      }
    }

  }
  
  /* (non-Javadoc)
   * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
   */
  public void setApplicationContext(ApplicationContext applicationContext)
      throws BeansException
  {
      this.applicationContext = applicationContext;
  }


}
