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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 */
public interface Subscription {
  
  /**
   * Get the Alfresco NodeRef associated with this subscription object.
   * This also serves as a unique identifier.
  
   * @return NodeRef
   */
  public NodeRef getNodeRef();
  /**
   * Method setNodeRef.
   * @param nodeRef NodeRef
   */
  public void setNodeRef(NodeRef nodeRef);
  
  /**
   * Get the owner of the subscription
   * TODO: add support for group subscriptions
  
   * @return SubscriptionOwner
   */
  public SubscriptionOwner getOwner();
  /**
   * Method setSubscriptionOwner.
   * @param owner SubscriptionOwner
   */
  public void setSubscriptionOwner(SubscriptionOwner owner);
  
  /**
   * The user-specified title of this subscription.
  
   * @return String
   */
  public String getTitle();
  /**
   * Method setTitle.
   * @param title String
   */
  public void setTitle(String title);
  
  /**
   * The name of the subscription object.
  
   * @return String
   */
  public String getName();
  /**
   * Method setName.
   * @param name String
   */
  public void setName(String name);
  
  /**
   * The type of this subscription.
  
   * @return QName
   */
  public QName getType();
 // public void setType(QName type);
  
  /**
   * The parameters for this subscription.  Used for 
   * event detection.
  
   * @return Map<QName,Serializable>
   */
  public Map<QName, Serializable> getParameters();
  /**
   * Method setParameters.
   * @param parameters Map<QName,Serializable>
   */
  public void setParameters(Map<QName, Serializable> parameters);
  
  /**
   * The delivery channels for this subscription.
  
   * @return List<QName>
   */
  public List<QName> getDeliveryChannels();
  /**
   * Method setDeliveryChannels.
   * @param deliveryChannels List<QName>
   */
  public void setDeliveryChannels(List<QName> deliveryChannels);
  
  /**
   * The frequency with which alerts are sent.
  
  
   * @return Frequency
   * @see Frequency */
  public Frequency getFrequency();
  /**
   * Method setFrequency.
   * @param frequency Frequency
   */
  public void setFrequency(Frequency frequency);

  
  /**
   * The last time an alert was sent from this subscription.
   * May need this for search-based subscriptions.
  
   * @return Date
   */
  public Date getLastAlertSent();
  
  /**
   * The date which the subscription was created.
  
   * @return Date
   */
  public Date getCreated();
}
