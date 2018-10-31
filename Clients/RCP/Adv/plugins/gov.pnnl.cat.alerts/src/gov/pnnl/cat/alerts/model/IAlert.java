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
package gov.pnnl.cat.alerts.model;

import gov.pnnl.cat.alerts.model.ISubscription.Frequency;
import gov.pnnl.cat.alerts.model.ISubscription.Type;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.velo.model.CmsPath;

import java.util.Calendar;

/**
 */
public interface IAlert extends Comparable<IAlert> {
  public final static CmsPath PATH_ALERT_ROOT = new CmsPath("/app:company_home/alrt:Alerts");

  /**
  
   * @return <tt>true</tt> if the alert has been read, and <tt>false</tt> otherwise. */
  public boolean isRead();

  /**
  
   * @return the name */
  public String getName();

  /**
  
   * @return the title */
  public String getTitle();

  /**
  
   * @return the summary */
  public String getSummary();

  /**
  
   * @return the uuid */
  public String getId();

  /**
  
   * @return the created */
  public Calendar getCreated();

  /**
  
   * @return the events */
  public IEvent[] getEvents();

  /**
  
   * @return the frequency */
  public Frequency getFrequency();

  /**
  
   * @return the subscriptionType */
  public Type getSubscriptionType();

  /**
  
   * @return the sender */
  public IUser getSender();

  /**
  
   * @return the recipients */
  public IUser[] getRecipients();

}
