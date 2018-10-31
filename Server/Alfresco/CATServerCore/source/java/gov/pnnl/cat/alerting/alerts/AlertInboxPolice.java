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
package gov.pnnl.cat.alerting.alerts;

import gov.pnnl.cat.alerting.delivery.DeliveryChannel;

import org.springframework.beans.factory.InitializingBean;

/**
 * Class that periodically (once per day?) crawls the alert inboxes to see which ones are over the size limit.
 * If a box is over the size limit, then a warning email is sent to the admins and to the user. 
 * Later, this class can also check for expired alerts and  throw them out.
 * @version $Revision: 1.0 $
 */
public class AlertInboxPolice implements InitializingBean {

  private AlertManagementService alertService;
  private DeliveryChannel emailChannel;
  
  /**
   * Default constructor
   */
  public AlertInboxPolice() {
    
  }
  
  
  
  /**
   * Need to create a job (maybe use Quartz scheduler that comes with Spring?) that crawls
   * the persisted alerts and makes sure they are in compliance with size policy.
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  public void afterPropertiesSet() throws Exception {
    // TODO Auto-generated method stub
    
  }


  /**
   * Needs to be injected by Spring
   * @param alertService
   */
  public void setAlertService(AlertManagementService alertService) {
    this.alertService = alertService;
  }
  
  /**
   * Need the email channel to communicate.
   * @param emailChannel
   */
  public void setEmailChannel(DeliveryChannel emailChannel) {
    this.emailChannel = emailChannel;
  }
                                                          
}
