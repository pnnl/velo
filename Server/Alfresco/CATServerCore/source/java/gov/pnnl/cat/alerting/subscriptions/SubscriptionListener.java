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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * So other classes can be alerted when a subscription changes.
 * This prevents other classes from having to bind to the subscription
 * type, thus forcing the use of ExtensiblePolicyAdapter to prevent
 * listeners from getting overwritten.
 * 
 * These methods will only be called after a tx commits, thus ensuring that
 * listeners won't get notified on something that is rolled back.
 *
 * @version $Revision: 1.0 $
 */
public interface SubscriptionListener {

  /**
   * An existing subscription has been modified in some way.
   * @param subscription
   */
  public void subscriptionChanged(NodeRef subscription);
  
  /**
   * A new subscription has been created.
   * @param subscription
   */
  public void subscriptionCreated(NodeRef subscription);
  
  /**
   * A subscription has been deleted.
   * @param subscription Subscription
   */
  public void subscriptionDeleted(Subscription subscription);
  
}
