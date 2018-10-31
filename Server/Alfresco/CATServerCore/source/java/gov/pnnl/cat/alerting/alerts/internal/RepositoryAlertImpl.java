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

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.AlertManagementService;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.RepositoryAlert;
import gov.pnnl.cat.alerting.subscriptions.Frequency;
import gov.pnnl.cat.util.XmlUtility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 */
public class RepositoryAlertImpl implements RepositoryAlert {

	private Map<QName, Serializable> properties;
	private NodeRef nodeRef;
	private AlertManagementService alertManagementService;
	private ContentService contentService;
	
	/**
	 * Method setAlertManagementService.
	 * @param alertManagementService AlertManagementService
	 */
	protected void setAlertManagementService(AlertManagementService alertManagementService) {
		this.alertManagementService = alertManagementService;
	}
	
	/**
	 * Method setContentService.
	 * @param contentService ContentService
	 */
	protected void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	public RepositoryAlertImpl() {
		properties = new HashMap<QName, Serializable>();
	}
	
	/**
	 * Constructor for RepositoryAlertImpl.
	 * @param nodeRef NodeRef
	 * @param properties Map<QName,Serializable>
	 * @param alertManagementService AlertManagementService
	 * @param contentService ContentService
	 */
	public RepositoryAlertImpl(NodeRef nodeRef, Map<QName, Serializable> properties, 
	    AlertManagementService alertManagementService, ContentService contentService) {
	  if(properties == null) {
	    this.properties = new HashMap<QName, Serializable>();
	  } else {
	    this.properties = properties;
	  }
		this.nodeRef = nodeRef;
		this.alertManagementService = alertManagementService;
		this.contentService = contentService;
	}
	
	/**
	 * Method setProperties.
	 * @param props Map<QName,Serializable>
	 */
	protected void setProperties(Map<QName, Serializable> props) {
		this.properties = props;
	}
	
	/**
	 * Method getCreated.
	 * @return Date
	 * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#getCreated()
	 */
	public Date getCreated() {
		return (Date)properties.get(ContentModel.PROP_CREATED);
	}

	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#getName()
	 */
	public String getName() {
		return (String)properties.get(ContentModel.PROP_NAME);
	}

	/**
	 * Method setNodeRef.
	 * @param nodeRef NodeRef
	 */
	protected void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	
	/**
	 * Method getNodeRef.
	 * @return NodeRef
	 * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#getNodeRef()
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	/**
	 * Method getSubscription.
	 * @return NodeRef
	 * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#getSubscription()
	 */
	public NodeRef getSubscription() {
		return (NodeRef)properties.get(AlertingConstants.PROP_ALERT_SUBSCRIPTION);
	}

	/**
	 * Method isAlertRead.
	 * @return boolean
	 * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#isAlertRead()
	 */
	public boolean isAlertRead() {
		Boolean propValue = (Boolean)properties.get(AlertingConstants.PROP_ALERT_WAS_READ);
		if (propValue == null) {
			propValue = Boolean.FALSE;
		}
		return propValue.booleanValue();
	}

	/**
	 * Method setAlertRead.
	 * @param alertRead boolean
	 */
	public void setAlertRead(boolean alertRead) {
		// update the in-memory property definition
		properties.put(AlertingConstants.PROP_ALERT_WAS_READ, new Boolean(alertRead));
		// write the property to the repository
		alertManagementService.setAlertRead(this.nodeRef, alertRead);
	}
	
	/* make sure to call this only after the node has been created! */
	/**
	 * Method setEvents.
	 * @param events List<Event>
	 */
	protected void setEvents(List<Event> events) {
		String eventXML = XmlUtility.serialize(events);
				
		ContentWriter writer = contentService.getWriter(nodeRef, AlertingConstants.PROP_CONTENT, true);
	    writer.setMimetype(MimetypeMap.MIMETYPE_XML);
	    writer.setEncoding("UTF-8");
		writer.putContent(eventXML);
	}

	/**
	 * Method getEvents.
	 * @return List<Event>
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getEvents()
	 */
	public List<Event> getEvents() {
		ContentReader reader = contentService.getReader(nodeRef, AlertingConstants.PROP_CONTENT);
		reader.setEncoding("UTF-8");
		reader.setMimetype(MimetypeMap.MIMETYPE_XML);
		String eventXml = reader.getContentString();

		List<Event> events = XmlUtility.deserialize(eventXml);
		
		return events;

	}

	/**
	 * Method getFrequency.
	 * @return Frequency
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getFrequency()
	 */
	public Frequency getFrequency() {
		String frequencyString = (String)properties.get(AlertingConstants.PROP_ALERT_FREQUENCY);
		return new Frequency(frequencyString);
	}

	/**
	 * Method getRecipients.
	 * @return List<Actor>
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getRecipients()
	 */
	public List<Actor> getRecipients() {
		List<String> recipients = (List<String>)properties.get(AlertingConstants.PROP_ALERT_RECIPIENTS);
		List<Actor> recipientActors = new ArrayList<Actor>();
		if (recipients == null) {
		  ActorImpl actor = new ActorImpl();
      actor.setUsername("");
			recipientActors.add(actor);
			return recipientActors;
		}
		for (String recipient: recipients) {
      ActorImpl actor = new ActorImpl();
      actor.setUsername(recipient);
			recipientActors.add(actor);
		}
		return recipientActors;
	}

	/**
	 * Method getSender.
	 * @return Actor
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getSender()
	 */
	public Actor getSender() {
		String sender = (String)properties.get(AlertingConstants.PROP_ALERT_SENDER);
    ActorImpl actor = new ActorImpl();
    actor.setUsername(sender);
		
		return actor;
	}

	/**
	 * Method getSubscriptionType.
	 * @return QName
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getSubscriptionType()
	 */
	public QName getSubscriptionType() {
		return (QName)properties.get(AlertingConstants.PROP_ALERT_SUBSCRIPTION_TYPE);
	}

	/**
	 * Method getSummary.
	 * @return String
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getSummary()
	 */
	public String getSummary() {
		return (String)properties.get(AlertingConstants.PROP_ALERT_SUMMARY);

	}

	/**
	 * Method getTitle.
	 * @return String
	 * @see gov.pnnl.cat.alerting.alerts.Alert#getTitle()
	 */
	public String getTitle() {
		return (String)properties.get(ContentModel.PROP_TITLE);
	}
	
	/**
	 * Method getBkmsDisplay.
	 * @return String
	 * @see gov.pnnl.cat.alerting.alerts.RepositoryAlert#getBkmsDisplay()
	 */
	public String getBkmsDisplay() {
		ContentReader reader = contentService.getReader(nodeRef, AlertingConstants.PROP_ALERT_BKMS_DISPLAY);
		String formattedAlert = reader.getContentString();
	    
		return formattedAlert;
	}

}
