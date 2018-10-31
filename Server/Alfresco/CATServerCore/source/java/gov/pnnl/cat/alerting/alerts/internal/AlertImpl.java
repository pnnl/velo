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
package gov.pnnl.cat.alerting.alerts.internal;

import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.subscriptions.Frequency;

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 */
public class AlertImpl implements Alert {

	private List<Event> events;
	private Frequency frequency;
	private List<Actor> recipients;
	private Actor sender;
	private QName SubscriptionType;
	private String summary;
	private String title;
	
	/**
	 * Method getEvents.
	 * @return List<Event>
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getEvents()
	 */
	public List<Event> getEvents() {
		return events;
	}
	/**
	 * Method setEvents.
	 * @param events List<Event>
	 */
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	/**
	 * Method getFrequency.
	 * @return Frequency
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getFrequency()
	 */
	public Frequency getFrequency() {
		return frequency;
	}
	/**
	 * Method setFrequency.
	 * @param frequency Frequency
	 */
	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}
	/**
	 * Method getRecipients.
	 * @return List<Actor>
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getRecipients()
	 */
	public List<Actor> getRecipients() {
		return recipients;
	}
	/**
	 * Method setRecipients.
	 * @param recipients List<Actor>
	 */
	public void setRecipients(List<Actor> recipients) {
		this.recipients = recipients;
	}
	/**
	 * Method getSender.
	 * @return Actor
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getSender()
	 */
	public Actor getSender() {
		return sender;
	}
	/**
	 * Method setSender.
	 * @param sender Actor
	 */
	public void setSender(Actor sender) {
		this.sender = sender;
	}
	/**
	 * Method getSubscriptionType.
	 * @return QName
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getSubscriptionType()
	 */
	public QName getSubscriptionType() {
		return SubscriptionType;
	}
	/**
	 * Method setSubscriptionType.
	 * @param subscriptionType QName
	 */
	public void setSubscriptionType(QName subscriptionType) {
		SubscriptionType = subscriptionType;
	}
	/**
	 * Method getSummary.
	 * @return String
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getSummary()
	 */
	public String getSummary() {
		return summary;
	}
	/**
	 * Method setSummary.
	 * @param summary String
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}
	/**
	 * Method getTitle.
	 * @return String
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getTitle()
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * Method setTitle.
	 * @param title String
	 */
	public void setTitle(String title) {
		this.title = title;
	}


}
