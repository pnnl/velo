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

import java.util.Calendar;

/**
 */
public class Alert implements IAlert {
  private boolean read = false;
  private String name;
  private String title;
  private String summary;
  private String id;
  private Calendar created;
  private IEvent[] events;
  private Frequency frequency;
  private Type subscriptionType;
  private IUser sender;
  private IUser[] recipients;

  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#isRead()
   */
  @Override
  public boolean isRead() {
    return read;
  }

  /**
   * @param read the read to set
   */
  public void setRead(boolean read) {
    this.read = read;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#getTitle()
   */
  public String getTitle() {
    return title;
  }
  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }
  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#getSummary()
   */
  public String getSummary() {
    return summary;
  }
  /**
   * @param summary the summary to set
   */
  public void setSummary(String summary) {
    this.summary = summary;
  }
  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#getPath()
   */
  /**
   * Method getId.
   * @return String
   * @see gov.pnnl.cat.alerts.model.IAlert#getId()
   */
  public String getId() {
    return id;
  }
  /**
  
   * @param uuid String
   */
  public void setId(String uuid) {
    this.id = uuid;
  }
  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#getCreated()
   */
  public Calendar getCreated() {
    return created;
  }
  /**
   * @param created the created to set
   */
  public void setCreated(Calendar created) {
    this.created = created;
  }
  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#getEvents()
   */
  public IEvent[] getEvents() {
    return events;
  }
  /**
   * @param events the events to set
   */
  public void setEvents(IEvent[] events) {
    this.events = events;
  }
  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#getFrequency()
   */
  public Frequency getFrequency() {
    return frequency;
  }
  /**
   * @param frequency the frequency to set
   */
  public void setFrequency(Frequency frequency) {
    this.frequency = frequency;
  }
  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#getSubscriptionType()
   */
  public Type getSubscriptionType() {
    return subscriptionType;
  }
  /**
   * @param subscriptionType the subscriptionType to set
   */
  public void setSubscriptionType(Type subscriptionType) {
    this.subscriptionType = subscriptionType;
  }
  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#getSender()
   */
  public IUser getSender() {
    return sender;
  }
  /**
   * @param sender the sender to set
   */
  public void setSender(IUser sender) {
    this.sender = sender;
  }
  /* (non-Javadoc)
   * @see gov.pnnl.cat.alerts.model.IAlert#getRecipients()
   */
  public IUser[] getRecipients() {
    return recipients;
  }
  /**
   * @param recipients the recipients to set
   */
  public void setRecipients(IUser[] recipients) {
    this.recipients = recipients;
  }

  /**
   * Method toString.
   * @return String
   */
  @Override
  public String toString() {
    return title;
  }

  /**
   * Method compareTo.
   * @param alert IAlert
   * @return int
   */
  @Override
  public int compareTo(IAlert alert) {
    return created.compareTo(alert.getCreated());
  }
}
