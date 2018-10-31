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
package gov.pnnl.cat.alerting.subscriptions;

import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.alerts.TemporaryAlert;
import gov.pnnl.cat.alerting.delivery.DeliveryChannel;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;
import gov.pnnl.cat.alerting.exceptions.InvalidSubscriptionException;
import gov.pnnl.cat.alerting.exceptions.TooManySubscriptionsException;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Service used to manage user subscriptions. Maybe TODO: If we decide to run the alerting/subscription pieces on a separate server that is not running Alfresco, then we need to add subscription event listeners so the other beans can keep track of what is going on. If we run in a clustered Alfresco configuration (where the subscription beans are running on  multiple Alfresco servers) - probably not a good idea - we need to make the caches distributed. Subscription service bean needs to be wrapped with a tx interceptor Subscription service bean might need to be wrapped with a security interceptor (for granting access to methods and subscription objects).  If NodeService and SearchService security are ok, then we might not need this.
 * @version $Revision: 1.0 $
 */
public interface SubscriptionService {
  
  /**
   * Set the max number of subscriptions a user can have at any one time.
   * @param maxSubscriptions
   */
  public void setMaxSubscriptions(int maxSubscriptions);
  
  /**
   * Gets all the subscriptions that the current user has
   * access to see.
  
   * @return List<Subscription>
   */
  public List<Subscription> getSubscriptions();

  /**
   * Look up the persistent subscription at that NodeRef.
   * @param subscriptionRef
  
   * @return Subscription
   */
  public Subscription getSubscription(NodeRef subscriptionRef);

  /**
   * Get all the subscriptions for the current user.
   * TODO: add support for group subscriptions
   * @param username
  
   * @return List<Subscription>
   */
  public List<Subscription> getSubscriptions(String username);

  /**
   * Create a new subscription.  New subscription needs to be
   * validated against the SubscriptionType definition.
   * @param newSubscription
  
  
   * @return the subscription that was just created * @throws InvalidSubscriptionException
   * @throws TooManySubscriptionsException
   * @throws InvalidSubscriptionException, TooManySubscriptionsException */
  public Subscription createSubscription(Subscription newSubscription) 
    throws InvalidSubscriptionException, TooManySubscriptionsException;
  
  /**
   * Delete the selected subscriptions.
   * @param subscriptions
   */
  public void deleteSubscriptions(List<Subscription> subscriptions);
  
  /**
   * Update an existing subscription
   * @param subscription
  
  
   * @return the subscription that was just updated * @throws InvalidSubscriptionException */
  public Subscription updateSubscription(Subscription subscription)
    throws InvalidSubscriptionException;
   
  /**
   * Given the QName of a subscription type, get the SubscriptionType object
   * @param subscriptionTypeName
  
   * @return SubscriptionType
   */
	public SubscriptionType getSubscriptionType(QName subscriptionTypeName);
	
  /**
   * Get all the subscription types that have been registered with the service.
  
   * @return List<SubscriptionType>
   */
  public List<SubscriptionType> getSubscriptionTypes();
  
  /**
   * SubscriptionTypes should be injected.
   * @param subscriptionTypes
   */
  public void setSubscriptionTypes(List<SubscriptionType> subscriptionTypes);
 
  /**
   * The delivery channels possible to choose when creating a new
   * subscription.  Any subscription type can use any delivery channel.
  
   * @return List<DeliveryChannel>
   */
  public List<DeliveryChannel> getDeliveryChannels();

  /**
   * DeliveryChannels should be injected.
   * @param deliveryChannels
   */
  public void setDeliveryChannels(List<DeliveryChannel> deliveryChannels);

  /**
   * Add a new listener to subscription events.
   * @param listener
   */
  public void addSubscriptionListener(SubscriptionListener listener);
  
  /**
   * Remove a subscription event listener.
   * @param listener
   */
  public void removeSubscriptionListener(SubscriptionListener listener);
  
  /**
   * Add this alert to the set of temporary alerts queued up for
   * the digester.
   * @param alert
   * @param subscription Subscription
   */
  public void addTemporaryAlert(Subscription subscription, TemporaryAlert alert);

  /**
   * Get the temporary alerts for the digester. Return object up
   * for discussion.  Not sure if we should be creating a separate
   * alert node for every temporary alert, or if we should be concatenating
   * them into a single object.  I'm thinking the latter.
   * 
   * Since any given subscription can only have one frequency, we don't have to
   * worry about managing digests for different frequencies.
   * @param subscription Subscription
   * @return List<TemporaryAlert>
   */
  public List<TemporaryAlert> getTemporaryAlerts(Subscription subscription);
  
  /**
   * This should be called by the digester
   *
   * @param subscription Subscription
   */
  public void clearTemporaryAlerts(Subscription subscription);
 
  /**
   * Sends the alert to all the delivery channels set for this Subscription.
   * Once the alerts have been sent, update the lastAlertTime.
   * 
   * @param alert
  
   * @param subscription Subscription
   * @throws DeliveryException */
  public void send(Subscription subscription, Alert alert) throws DeliveryException;
  
  /**
   * Method newSubscriptionInstance.
   * @param subscriptionType SubscriptionType
   * @return Subscription
   */
  public Subscription newSubscriptionInstance(SubscriptionType subscriptionType);
}
