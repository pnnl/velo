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
package gov.pnnl.cat.alerting.detection.internal;

import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.AlertManagementService;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.internal.ActorImpl;
import gov.pnnl.cat.alerting.alerts.internal.AlertImpl;
import gov.pnnl.cat.alerting.delivery.DeliveryChannel;
import gov.pnnl.cat.alerting.detection.EventProcessor;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;
import gov.pnnl.cat.alerting.exceptions.InvalidSubscriptionException;
import gov.pnnl.cat.alerting.subscriptions.Subscription;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionService;
import gov.pnnl.cat.alerting.subscriptions.internal.SubscriptionImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Base class for EventProcessors.  Implements common functionality.
 *
 * @version $Revision: 1.0 $
 */
public abstract class AbstractEventProcessor extends AbstractLifecycleBean implements EventProcessor, InitializingBean {

  protected SubscriptionService subscriptionService;
  protected AlertManagementService alertManagementService;
  protected TransactionService transactionService;
  protected AuthenticationComponent authenticationComponent;


  protected List<QName> subscriptionTypes;
  protected NodeService nodeService;

  private static Log logger = LogFactory.getLog(AbstractEventProcessor.class); 


  /**
   * Method onBootstrap.
   * @param arg0 ApplicationEvent
   */
  @Override
  protected void onBootstrap(ApplicationEvent arg0) {
    logger.debug("bootstrapping: " + this.getClass());
    // with alfresco 4, this code MUST run on bootstrap instead of in afterPropertiesSet
    // because of the order in which beans are executed
    // On bootstrap, the afterPropertiesSet code is getting executed before the
    // schema bootstrap bean is loaded, so the database doesn't exist
    subscriptionService.addSubscriptionListener(this);  

    // get all of the subscriptions, fire the event that they have been created
    // note that the subscriptionCreated method will only process the ones
    // that should be handled by this class
    RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>() {
      public Object execute() throws Throwable  {
        AuthenticationUtil.setRunAsUserSystem();
        try {
          List<Subscription> subscriptions = subscriptionService.getSubscriptions();
          for (Subscription subscription: subscriptions) {
            NodeRef nodeRef = subscription.getNodeRef();
            subscriptionCreated(nodeRef);
          }
        } catch (Exception e) {
          logger.error("Failed to query subscriptions", e);
          e.printStackTrace();
        }
        return null;
      }
    };
    transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);  

  }

  /**
   * Method subscriptionChanged.
   * @param subscription NodeRef
   * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionListener#subscriptionChanged(NodeRef)
   */
  @Override
  public void subscriptionChanged(NodeRef subscription) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method subscriptionCreated.
   * @param subscription NodeRef
   * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionListener#subscriptionCreated(NodeRef)
   */
  @Override
  public void subscriptionCreated(NodeRef subscription) {
    // TODO Auto-generated method stub
    
  }



  /**
   * Method subscriptionDeleted.
   * @param subscription Subscription
   * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionListener#subscriptionDeleted(Subscription)
   */
  @Override
  public void subscriptionDeleted(Subscription subscription) {
    // TODO Auto-generated method stub
    
  }



  /**
   * Method onShutdown.
   * @param arg0 ApplicationEvent
   */
  @Override
  protected void onShutdown(ApplicationEvent arg0) {
    // TODO Auto-generated method stub
    
  }



  /* (non-Javadoc)
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  public void afterPropertiesSet() throws Exception {

  }


  /**
   * Needs to be injected by Spring
   * @param subscriptionService SubscriptionService
   * @see gov.pnnl.cat.alerting.detection.EventProcessor#setSubscriptionService(SubscriptionService)
   */
  public void setSubscriptionService(SubscriptionService subscriptionService) {
	this.subscriptionService = subscriptionService;
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
  public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
	  this.authenticationComponent = authenticationComponent;
  }


/**
   * Needs to be injected by Spring
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
   * Needs to be injected by Spring
   * @param subscriptionTypes List<Object>
   * @see gov.pnnl.cat.alerting.detection.EventProcessor#setSubscriptionTypes(List<Object>)
   */
  public void setSubscriptionTypes(List<Object> subscriptionTypes) {
	this.subscriptionTypes = new ArrayList<QName>();
	for (Object subType : subscriptionTypes) {
	  if (subType instanceof String) {
		this.subscriptionTypes.add(QName.createQName((String)subType));
	  } else if (subType instanceof QName) {
		this.subscriptionTypes.add((QName)subType);
	  }
	}
  }

  /**
   * Inject via Spring, either as a QName or a String 
   * @param subscriptionType
   */
  public void setSubscriptionType(QName subscriptionType) {
	this.subscriptionTypes = new ArrayList<QName>();
	this.subscriptionTypes.add(subscriptionType);
  }

  /**
   * Method setSubscriptionType.
   * @param subscriptionType String
   */
  public void setSubscriptionType(String subscriptionType) {
	this.subscriptionTypes = new ArrayList<QName>();
	this.subscriptionTypes.add(QName.createQName(subscriptionType));
  }

  /**
   * Method isHandledSubscriptionType.
   * @param subscriptionType QName
   * @return boolean
   */
  protected boolean isHandledSubscriptionType(QName subscriptionType) {
	if (subscriptionTypes == null) {
	  return false;
	}
	return subscriptionTypes.contains(subscriptionType);
  }

  /**
   * Convenience method for subclasses to invoke to cause an alert to be created, 
   * based on a group of events associated with a subscription
   * @param subscription
   * @param events
   * @param sender
  
   * @throws DeliveryException */
  protected void createAlert(Subscription subscription, List<Event> events, Actor sender) throws DeliveryException {
	List<QName> deliveryChannelQNames = subscription.getDeliveryChannels();
	List<DeliveryChannel> deliveryChannels = subscriptionService.getDeliveryChannels();

	AlertImpl alert = new AlertImpl();
	alert.setFrequency(subscription.getFrequency());
	// TODO: fix for groups
	List<Actor> recipients = new ArrayList<Actor>();
	
	ActorImpl actor = new ActorImpl();
  actor.setUsername(subscription.getOwner().getAccountId());
  
	recipients.add(actor);
	alert.setRecipients(recipients);
	alert.setSender(sender);
	alert.setSubscriptionType(subscription.getType());
	alert.setTitle(subscription.getTitle());

	alert.setEvents(events);			

	for (QName deliveryChannelQName : deliveryChannelQNames) {
	  for (DeliveryChannel deliveryChannel : deliveryChannels) {
		if (deliveryChannel.getName().equals(deliveryChannelQName)) {
		  try{
		    deliveryChannel.send(alert);
		    SubscriptionImpl subscriptionImpl = (SubscriptionImpl)subscription;
		    subscriptionImpl.setLastAlertSent(new Date());
		  }catch (DeliveryException de) {
		    //keep trying other channels(email might fail, but repo should always work)
		  }
		  try {
		  	subscriptionService.updateSubscription(subscription);
		  } catch (InvalidSubscriptionException ise) {
		  	logger.warn("Exception setting lastAlertSent for subscription " + subscription.getNodeRef(), ise);
		  }
		}
	  }
	}

  }








}
