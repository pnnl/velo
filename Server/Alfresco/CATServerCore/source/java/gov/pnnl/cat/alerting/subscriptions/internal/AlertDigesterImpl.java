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
package gov.pnnl.cat.alerting.subscriptions.internal;

import gov.pnnl.cat.alerting.alerts.AlertManagementService;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.TemporaryAlert;
import gov.pnnl.cat.alerting.detection.internal.repository.RepositoryEventProcessor;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;
import gov.pnnl.cat.alerting.subscriptions.AlertDigester;
import gov.pnnl.cat.alerting.subscriptions.Frequency;
import gov.pnnl.cat.alerting.subscriptions.Subscription;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionService;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;


/**
 */
public class AlertDigesterImpl implements AlertDigester, InitializingBean{

	private SubscriptionService subscriptionService;
	private AlertManagementService alertManagementService;
	private AuthenticationComponent authenticationComponent;
	private TransactionService transactionService;
	private RepositoryEventProcessor repositoryEventProcessor;
	private boolean beanPropertiesSet = false;
	
	private static Log logger = LogFactory.getLog(AlertDigesterImpl.class); 

	/**
	 * A job needs to be scheduled for each Frequency interval. Maybe use the
	 * Quartz scheduler that comes with Spring.
	 * For each job, need to walk the subscriptions list, and pick out only those
	 * subscriptions that apply to that Frequency.  Then, need to get the
	 * temporary alerts for each applicable subscription, and combine them into one
	 * alert.  Then need to remove the temporary alerts from the subscription.
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		beanPropertiesSet = true;

	}

	/**
	 * For each subscription, digests temporary alerts into one alert. 
	 * For each digested alert, creates the alert object and sends it to all
	 * the delivery channels registered for that subscription.
	 * @see gov.pnnl.cat.alerting.subscriptions.AlertDigester#digestDailyAlerts()
	 */
	public void digestDailyAlerts() {
		performDigest(Frequency.DAILY);
	}

	/**
	 * For each subscription, digests temporary alerts into one alert. 
	 * For each digested alert, creates the alert object and sends it to all
	 * the delivery channels registered for that subscription.
	 * 
	
	 * @see gov.pnnl.cat.alerting.subscriptions.AlertDigester#digestWeeklyAlerts()
	 */  
	public void digestWeeklyAlerts() {
		performDigest(Frequency.WEEKLY); 
	}

	/**
	 * For each subscription, digests temporary alerts into one alert. 
	 * For each digested alert, creates the alert object and sends it to all
	 * the delivery channels registered for that subscription.
	 * 
	
	 * @see gov.pnnl.cat.alerting.subscriptions.AlertDigester#digestHourlyAlerts()
	 */
	public void digestHourlyAlerts() {
		performDigest(Frequency.HOURLY);
	}
	
	/**
	 * Common logic for performing the digesting function.
	 * @param frequency Frequency
	 */
	private void performDigest(final Frequency frequency) {
		if (beanPropertiesSet == false) {
			// the bean isn't done initializing.  Maybe quartz started running jobs
			// before Spring was done setting up all of the beans in the system.
			// Not sure if this could happen, but guard against it anyway.  
			return;
		}
		// wrap in a transaction
		TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, 
				new TransactionUtil.TransactionWork<Object>() {
					public Object doWork() throws Exception {
						// get all subscriptions
						AuthenticationUtil.setRunAsUserSystem();
						List<Subscription> subscriptions = subscriptionService.getSubscriptions();
						for (Subscription subscription : subscriptions) {
							// if this subscription matches the desired frequency, process it
							if (subscription.getFrequency().equals(frequency)) {
								digestSubscription(subscription);
							}
						 } 
						return null;
					}
		});
	}



	/**
	 * Method setRepositoryEventProcessor.
	 * @param repositoryEventProcessor RepositoryEventProcessor
	 */
	public void setRepositoryEventProcessor(
		RepositoryEventProcessor repositoryEventProcessor) {
	  this.repositoryEventProcessor = repositoryEventProcessor;
	}

	/**
	 * Method setTransactionService.
	 * @param transactionService TransactionService
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * Needs to be injected
	 * @see gov.pnnl.cat.alerting.subscriptions.AlertDigester#setSubscriptionService(gov.pnnl.cat.alerting.subscriptions.SubscriptionService)
	 */
	public void setSubscriptionService(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;

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
	 * Method setAlertManagementService.
	 * @param alertManagementService AlertManagementService
	 */
	public void setAlertManagementService(AlertManagementService alertManagementService) {
		this.alertManagementService = alertManagementService;
	}


	/**
	 * Method digestSubscription.
	 * @param subscription Subscription
	 */
	private void digestSubscription(Subscription subscription) {
		List<TemporaryAlert> tempAlerts = subscriptionService.getTemporaryAlerts(subscription);
		if (tempAlerts == null || tempAlerts.size() == 0) {
			// no temp alerts to process.  nothing to do.  exit.
			return;
		}
		try {
			// iterate through temp alerts, extract all of the Events, add to the alert
			List<Event> events = new ArrayList<Event>();
			for (TemporaryAlert tempAlert : tempAlerts) {
			  Event event = tempAlert.getEvent();
			  events.add(event);
			}
			
			repositoryEventProcessor.createAlert(subscription, events);
		  
//			List<QName> deliveryChannelQNames = subscription.getDeliveryChannels();
//			List<DeliveryChannel> deliveryChannels = subscriptionService.getDeliveryChannels();
//
//			AlertImpl alert = new AlertImpl();
//			alert.setFrequency(subscription.getFrequency());
//			// TODO: fix for groups
//			List<Actor> recipients = new ArrayList<Actor>();
//			recipients.add(new ActorImpl(subscription.getOwner().getAccountId()));
//			alert.setRecipients(recipients);
//			alert.setSender(sender);
//			alert.setSubscriptionType(subscription.getType());
//			alert.setTitle(subscription.getTitle());
//			
//			// iterate through temp alerts, extract all of the Events, add to the alert
//			List<Event> events = new ArrayList<Event>();
//			for (TemporaryAlert tempAlert : tempAlerts) {
//				Event event = tempAlert.getEvent();
//				events.add(event);
//			}
//			alert.setEvents(events);			
//
//			for (QName deliveryChannelQName : deliveryChannelQNames) {
//				for (DeliveryChannel deliveryChannel : deliveryChannels) {
//					if (deliveryChannel.getName().equals(deliveryChannelQName)) {
//						deliveryChannel.send(alert);
//						
//						SubscriptionImpl subscriptionImpl = (SubscriptionImpl)subscription;
//						subscriptionImpl.setLastAlertSent(new Date());
//						try {
//							subscriptionService.updateSubscription(subscription);
//						} catch (InvalidSubscriptionException ise) {
//							logger.warn("Exception setting lastAlertSent for subscription " + subscription.getNodeRef(), ise);
//						}
//					}
//				}
//			}
		  
			// last thing: clear the temp alerts from this subscription
			// assumption: transaction isolation will prevent unprocessed
			// temp alerts from being cleared that might have been added
			// in between  the getTempAlerts() method above and this point.
			subscriptionService.clearTemporaryAlerts(subscription);
		} catch (DeliveryException de) {
			;
		}
	}

}
