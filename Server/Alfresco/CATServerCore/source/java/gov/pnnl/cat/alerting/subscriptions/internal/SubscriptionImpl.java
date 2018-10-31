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
package gov.pnnl.cat.alerting.subscriptions.internal;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.subscriptions.Frequency;
import gov.pnnl.cat.alerting.subscriptions.Subscription;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionOwner;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 */
public class SubscriptionImpl implements Subscription {

	private NodeRef nodeRef;
	private Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	private QName type;
	private SubscriptionOwner owner;
	
	/**
	 * Constructor for SubscriptionImpl.
	 * @param type QName
	 */
	protected SubscriptionImpl(QName type) {
		this.type = type;
		// default the last alert sent date to the current time so we are capturing all events from this point forward
		properties.put(AlertingConstants.PROP_SUB_LAST_ALERT_SENT, new Date(System.currentTimeMillis()));
	}
	
	/**
	 * Method getDeliveryChannels.
	 * @return List<QName>
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#getDeliveryChannels()
	 */
	public List<QName> getDeliveryChannels() {
		return (List<QName>)properties.get(AlertingConstants.PROP_SUB_DELIVERY_CHANNELS);
	}

	/**
	 * Method getFrequency.
	 * @return Frequency
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#getFrequency()
	 */
	public Frequency getFrequency() {
		String frequencyString = (String)properties.get(QName.createQName(type.getNamespaceURI(), AlertingConstants.PROP_SUB_FREQUENCY_LOCALNAME));
		return new Frequency(frequencyString);
	}
	
	/**
	 * Method getLastAlertSent.
	 * @return Date
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#getLastAlertSent()
	 */
	public Date getLastAlertSent() {
		return (Date)properties.get(AlertingConstants.PROP_SUB_LAST_ALERT_SENT);
	}
	
	/**
	 * Method setLastAlertSent.
	 * @param date Date
	 */
	public void setLastAlertSent(Date date) {
		properties.put(AlertingConstants.PROP_SUB_LAST_ALERT_SENT, date);
	}

	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#getName()
	 */
	public String getName() {
		return (String)properties.get(ContentModel.PROP_NAME);
	}

	/**
	 * Method getNodeRef.
	 * @return NodeRef
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#getNodeRef()
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	/**
	 * Method getOwner.
	 * @return SubscriptionOwner
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#getOwner()
	 */
	public SubscriptionOwner getOwner() {
		return owner;
	}

	/**
	 * Method getParameters.
	 * @return Map<QName,Serializable>
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#getParameters()
	 */
	public Map<QName, Serializable> getParameters() {
		return properties;
	}

	/**
	 * Method getTitle.
	 * @return String
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#getTitle()
	 */
	public String getTitle() {
		return (String)properties.get(ContentModel.PROP_TITLE);
	}

	/**
	 * Method getType.
	 * @return QName
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#getType()
	 */
	public QName getType() {
		return type;
	}

	/**
	 * Method setDeliveryChannels.
	 * @param deliveryChannels List<QName>
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#setDeliveryChannels(List<QName>)
	 */
	public void setDeliveryChannels(List<QName> deliveryChannels) {
		properties.put(AlertingConstants.PROP_SUB_DELIVERY_CHANNELS, (Serializable)deliveryChannels);
	}

	/**
	 * Method setFrequency.
	 * @param frequency Frequency
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#setFrequency(Frequency)
	 */
	public void setFrequency(Frequency frequency) {
		properties.put(QName.createQName(type.getNamespaceURI(), AlertingConstants.PROP_SUB_FREQUENCY_LOCALNAME), 
				frequency.toString());
	}

	/**
	 * Method setName.
	 * @param name String
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#setName(String)
	 */
	public void setName(String name) {
		properties.put(ContentModel.PROP_NAME, name);
	}

	/**
	 * Method setNodeRef.
	 * @param nodeRef NodeRef
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#setNodeRef(NodeRef)
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	/**
	 * Method setParameters.
	 * @param parameters Map<QName,Serializable>
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#setParameters(Map<QName,Serializable>)
	 */
	public void setParameters(Map<QName, Serializable> parameters) {
		this.properties.putAll(parameters);
	}

	/**
	 * Method setSubscriptionOwner.
	 * @param owner SubscriptionOwner
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#setSubscriptionOwner(SubscriptionOwner)
	 */
	public void setSubscriptionOwner(SubscriptionOwner owner) {
		this.owner = owner;
	}

	/**
	 * Method setTitle.
	 * @param title String
	 * @see gov.pnnl.cat.alerting.subscriptions.Subscription#setTitle(String)
	 */
	public void setTitle(String title) {
		properties.put(ContentModel.PROP_TITLE, title);
	}

	/**
	 * Method setType.
	 * @param type QName
	 */
	public void setType(QName type) {
		this.type = type;
	}

  /**
   * Method getCreated.
   * @return Date
   * @see gov.pnnl.cat.alerting.subscriptions.Subscription#getCreated()
   */
  public Date getCreated() {
    return (Date)properties.get(ContentModel.PROP_CREATED);
  }
  
  /**
   * Method equals.
   * @param o Object
   * @return boolean
   */
  public boolean equals(Object o) {
  	if ((o instanceof Subscription) == false) {
  		return false;
  	}
  	Subscription s = (Subscription)o;
  	return s.getNodeRef().equals(nodeRef) && s.getType().equals(type);
  }
  
  /**
   * Method hashCode.
   * @return int
   */
  public int hashCode() {
  	return nodeRef.hashCode();
  }
}
