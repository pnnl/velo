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
package gov.pnnl.cat.alerting.detection;

import gov.pnnl.cat.alerting.subscriptions.SubscriptionListener;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionService;

import java.util.List;


/**
 * Each SubscriptionType should map to one EventProcessor bean.  For each
 * subscription of that type, this class 
 * decides when alerts should be generated and when temporary alerts
 * should be added for later digest.  When/how to process events is 
 * left up to the implementor.
 * 
 * When it starts up, it should review all the subscriptions in the system
 * and only monitor the ones that apply to this subscription type.  It should
 * also register as a SubscriptionListener to the SubscriptionService, so it can
 * monitor new subscriptions that get added and/or subscriptions that are deleted.
 * 
 * Only one copy of an EventProcessor bean should be running, so they need to
 * be commented out from a second Alfresco server.
 * @version $Revision: 1.0 $
 */
public interface EventProcessor extends SubscriptionListener {
  
  /**
   * The SubscriptionType associated with this event processor.  Should
   * be injected.
   * @param subscriptionTypes List<Object>
   */
  public void setSubscriptionTypes(List<Object> subscriptionTypes);
  
  /**
   * The SubscriptionService needs to be injected.
   * @param subscriptionService
   */
  public void setSubscriptionService(SubscriptionService subscriptionService);
}
