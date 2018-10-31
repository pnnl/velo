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

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Alerts that are persisted to the repository.  Event list
 * needs to be persisted to XML using XStream.
 * @version $Revision: 1.0 $
 */
public interface RepositoryAlert extends Alert {
  
  /**
   * Get the Alfresco NodeRef associated with this alert object.
  
   * @return NodeRef
   */
  public NodeRef getNodeRef();
  
  /**
   * The name of the persisted alert object.
  
   * @return String
   */
  public String getName();  
  
  
  /**
   * Returns true if the alert has been read by the user
  
   * @return boolean
   */
  public boolean isAlertRead();
  
  /**
   * Returns the subscription object that created this alert.
  
   * @return null if the subscription has been deleted. */
  public NodeRef getSubscription();
  
  /**
   * When this alert was created (i.e., sent).
  
   * @return Date
   */
  public Date getCreated();
  
  /**
   * Returns a version of this repository alert formatted for display in BKMS
  
   * @return String
   */
  public String getBkmsDisplay();
}
