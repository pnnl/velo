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
package gov.pnnl.cat.alerting.delivery;

import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;

import org.alfresco.service.namespace.QName;

/**
 * Delivery channels are responsible for transporting an alert to a particular
 * destination.  Alerts can be sent over muliple delivery channels.
 * @version $Revision: 1.0 $
 */
public interface DeliveryChannel {

  /**
   * Send the alert to all recipients defined by the
   * Alert object.
   * @param alert
  
   * @throws DeliveryException */
  public void send(Alert alert) throws DeliveryException;
  
  /**
   * The unique identifier representing this channel.
  
   * @return QName
   */
  public QName getName();
   
  /**
   * The user-friendly title of this channel
   * @return String
   */
  public String getTitle();
}
