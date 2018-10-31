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
/**
 * 
 */
package gov.pnnl.cat.alerting.detection.internal.repository;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.detection.internal.AbstractEventProcessor;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;
import gov.pnnl.cat.alerting.subscriptions.Subscription;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;
import gov.pnnl.cat.util.NodeUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;



/**
 * We decided that this class should detect all of its events based on
 * JMS subscriptions so the user can be notified of all changes made to a file 
 * within the digest period.  If we used repository searches to detect changes,
 * we couldn't pick up deletes, and we would only know who made the last change. 
 * 
 * This class should create a SecurityFilteredTopicSubscriber for each repository 
 * subscription type.
 * @version $Revision: 1.0 $
 */
public class RepositoryEventProcessor extends AbstractEventProcessor implements ApplicationContextAware  {

  private ApplicationContext ctx;
  private String securityFilterTopicSubscriberBeanName;
  private Map<String, SecurityFilteredTopicSubscriber> subscriptions = new HashMap<String, SecurityFilteredTopicSubscriber>();
  private NamespacePrefixResolver namespacePrefixResolver;
  private Actor sender;

  
  public RepositoryEventProcessor(){
    //System.out.println("RepositoryEventProcessor constructed");
  }

  /**
   * Method setSender.
   * @param sender Actor
   */
  public void setSender(Actor sender) {
	this.sender = sender;
  }


  /**
   * Method setApplicationContext.
   * @param applicationContext ApplicationContext
   * @throws BeansException
   * @see org.springframework.context.ApplicationContextAware#setApplicationContext(ApplicationContext)
   */
  public void setApplicationContext(ApplicationContext applicationContext)
  throws BeansException {
    super.setApplicationContext(applicationContext);
	ctx = applicationContext;

  }

  /**
   * Method setSecurityFilterTopicSubscriberBeanName.
   * @param securityFilterTopicSubscriberBeanName String
   */
  public void setSecurityFilterTopicSubscriberBeanName(
	  String securityFilterTopicSubscriberBeanName) {
	this.securityFilterTopicSubscriberBeanName = securityFilterTopicSubscriberBeanName;
  }


  /**
   * Method setNamespacePrefixResolver.
   * @param namespacePrefixResolver NamespacePrefixResolver
   */
  public void setNamespacePrefixResolver(
	  NamespacePrefixResolver namespacePrefixResolver) {
	this.namespacePrefixResolver = namespacePrefixResolver;
  }

  /* (non-Javadoc)
   * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionListener#subscriptionChanged(org.alfresco.service.cmr.repository.NodeRef)
   */
  public void subscriptionChanged(NodeRef subscription) {
	// no need to check subscription type.  we assume the subscription type of a subscription
	// cannot be changed after it is created.  we're either listening already, or we never
	// will at all
  	SecurityFilteredTopicSubscriber subscriber = subscriptions.get(subscription.getId());
  	if (subscriber != null) {
  		// check to see if we changed what we are monitoring
  		// if so, stop and start the listener
  		// if the criteria hasn't changed, then the listener shouldn't be affected
  		String oldCriteria = subscriber.getSelectionCriteria();
  		String newCriteria = createSubscriptionCriteria(subscription);
  		if (oldCriteria.equals(newCriteria) == false) {
  			subscriber.stopListening();
  			subscriber.setSelectionCriteria(newCriteria);
  			subscriber.startListening();
  		}
  	}
  }

  /* (non-Javadoc)
   * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionListener#subscriptionCreated(org.alfresco.service.cmr.repository.NodeRef)
   */
  public void subscriptionCreated(NodeRef subscription) {
	Subscription subscriptionObj = subscriptionService.getSubscription(subscription);
	if (!isHandledSubscriptionType(subscriptionObj.getType())) {
	  // this subscription type is handled by someone else
	  return; 
	}
	String criteria = createSubscriptionCriteria(subscription);

	// TODO: make more flexible to handle groups.  
	String username = subscriptionObj.getOwner().getAccountId();
	SecurityFilteredTopicSubscriber subscriber = (SecurityFilteredTopicSubscriber)ctx.getBean(securityFilterTopicSubscriberBeanName);
	subscriber.setUsername(username);
	subscriber.setSelectionCriteria(criteria);
	subscriber.setSubscription(subscriptionObj);
	subscriber.startListening();

	subscriptions.put(subscription.getId(), subscriber);

  }

  /* (non-Javadoc)
   * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionListener#subscriptionDeleted(org.alfresco.service.cmr.repository.NodeRef)
   */
  /**
   * Method subscriptionDeleted.
   * @param subscription Subscription
   * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionListener#subscriptionDeleted(Subscription)
   */
  public void subscriptionDeleted(Subscription subscription) {
	// we can't check subscription type since the node has been deleted.  so just check the id
	// to see if we were previously listening.  if so, stop listening.  otherwise ignore.
	SecurityFilteredTopicSubscriber subscriber = subscriptions.remove(subscription.getNodeRef().getId());
	if (subscriber != null) {
	  subscriber.stopListening();
	}
  }

  /**
   * Method createSubscriptionCriteria.
   * @param subscription NodeRef
   * @return String
   */
  private String createSubscriptionCriteria(NodeRef subscription) {
	Map<QName, Serializable> subscriptionProps = nodeService.getProperties(subscription);
	NodeRef noderef = (NodeRef)subscriptionProps.get(AlertingConstants.PROP_SUB_REP_SUBSCRIPTION_NODE);
	String path =  nodeService.getPath(noderef).toString();
	String criteria;

	Boolean includeChildren = (Boolean)subscriptionProps.get(AlertingConstants.PROP_SUB_REP_INCLUDE_CHILDREN);
	if (includeChildren.equals(Boolean.TRUE)) {
	  String pathWithSep = path;
	  if (path.endsWith(NodeUtils.PATH_SEPARATOR) == false) {
	    pathWithSep += NodeUtils.PATH_SEPARATOR;
	  }
	  criteria = RepositoryEventMessage.JMSPROP_DMI_NODEPATH + " LIKE '" + pathWithSep + 
	      "%' OR " + RepositoryEventMessage.JMSPROP_DMI_NODEPATH + " = '" + path + "' ";
	} else {
	  criteria = RepositoryEventMessage.JMSPROP_DMI_NODEPATH + " = '" + path + "' ";
	}
	return criteria;

  }

  /*
   * Increase visibility of this method so it can be invoked from the AlertDigester
   * (non-Javadoc)
   * @see gov.pnl.dmi.alerting.detection.internal.AbstractEventProcessor#createAlert(gov.pnl.dmi.alerting.subscriptions.Subscription, java.util.List, gov.pnl.dmi.alerting.alerts.Actor)
   */
  /**
   * Method createAlert.
   * @param subscription Subscription
   * @param events List<Event>
   * @throws DeliveryException
   */
  public void createAlert(Subscription subscription, List<Event> events) throws DeliveryException { 
	super.createAlert(subscription, events, sender);
  }








}
