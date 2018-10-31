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

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 */
public class FakeAlert implements RepositoryAlert {

  private String name;
  private List<Event> events;
  private Date date;
  private boolean alertRead;
  
  /**
   * Constructor for FakeAlert.
   * @param name String
   * @param events List<Event>
   * @param date Date
   */
  public FakeAlert(String name, List<Event> events, Date date) {
    this.name = name;
    this.events = events;
    this.date = date;
  }

  /**
   * Method getCreated.
   * @return Date
   * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#getCreated()
   */
  public Date getCreated() {
    // TODO Auto-generated method stub
    return date;
  }

  /**
   * Method getName.
   * @return String
   * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#getName()
   */
  public String getName() {
    // TODO Auto-generated method stub
    return name;
  }

  /**
   * Method getNodeRef.
   * @return NodeRef
   * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#getNodeRef()
   */
  public NodeRef getNodeRef() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getSubscription.
   * @return NodeRef
   * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#getSubscription()
   */
  public NodeRef getSubscription() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method isAlertRead.
   * @return boolean
   * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#isAlertRead()
   */
  public boolean isAlertRead() {
    // TODO Auto-generated method stub
    return this.alertRead;
  }

  /**
   * Method setAlertRead.
   * @param alertRead boolean
   */
  public void setAlertRead(boolean alertRead) {
    // TODO Auto-generated method stub
    this.alertRead = alertRead;
  }

  /**
   * Method getEvents.
   * @return List<Event>
   * @see gov.pnnl.cat.alerting.alerts.Alert#getEvents()
   */
  public List<Event> getEvents() {
    // TODO Auto-generated method stub
    return events;
  }

  /**
   * Method getFrequency.
   * @return Frequency
   * @see gov.pnnl.cat.alerting.alerts.Alert#getFrequency()
   */
  public Frequency getFrequency() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getRecipients.
   * @return List<Actor>
   * @see gov.pnnl.cat.alerting.alerts.Alert#getRecipients()
   */
  public List<Actor> getRecipients() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getSender.
   * @return Actor
   * @see gov.pnnl.cat.alerting.alerts.Alert#getSender()
   */
  public Actor getSender() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getSubscriptionType.
   * @return QName
   * @see gov.pnnl.cat.alerting.alerts.Alert#getSubscriptionType()
   */
  public QName getSubscriptionType() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getSummary.
   * @return String
   * @see gov.pnnl.cat.alerting.alerts.Alert#getSummary()
   */
  public String getSummary() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getTitle.
   * @return String
   * @see gov.pnnl.cat.alerting.alerts.Alert#getTitle()
   */
  public String getTitle() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method toString.
   * @return String
   */
  public String toString() {
    return name;
  }

  /**
   * Method equals.
   * @param o Object
   * @return boolean
   */
  public boolean equals(Object o) {
    if (o != null && o.getClass().equals(getClass())) {
      FakeAlert alert = (FakeAlert) o;
      boolean equal = alert.getName().equals(name);
      return equal;
    }
    return false;
  }

  /**
   * Method hashCode.
   * @return int
   */
  public int hashCode() {
    return name.hashCode();
  }

/**
 * Method getBkmsDisplay.
 * @return String
 * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#getBkmsDisplay()
 */
public String getBkmsDisplay() {
	// TODO Auto-generated method stub
	return null;
}
  
}
