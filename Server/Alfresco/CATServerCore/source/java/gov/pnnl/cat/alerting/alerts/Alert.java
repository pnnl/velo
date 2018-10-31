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


import gov.pnnl.cat.alerting.subscriptions.Frequency;

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 */
public interface Alert {
  
  /**
   * To whom the alert was sent.  Abstract the recipient so we
   * can extend the options in the future.
  
   * @return List<Actor>
   */
  public List<Actor> getRecipients();
  
  /**
   * A summary of information about this alert, that could be used
   * as the header of the alert message.
  
   * @return String
   */
  public String getSummary();
  
  /**
   * The frequency (e.g., Hourly) with which this
   * alert was sent.
  
   * @return Frequency
   */
  public Frequency getFrequency();
  
  
  /**
   * TODO: Who sent the alert.  This could be 
   * used for one-time alerts.  Abstract the sender so we can
   * extend the options in the future.
  
   * @return Actor
   */
  public Actor getSender();
  
  /**
   * What the alert is about.  Use the title of the subscription as
   * the subject.
  
   * @return String
   */
  public String getTitle();
    
  /**
   * The type of subscription who created this alert.  We might want to
   * format the alerts differently, depending on the type. 
  
   * @return QName
   */
  public QName getSubscriptionType();
  
  /**
   * The events that comprise this Alert
  
   * @return List<Event>
   */
  public List<Event> getEvents();
  
}
