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

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Temporary alerts waiting to be digested.  Only need
 * to set the text.
 * @version $Revision: 1.0 $
 */
public interface TemporaryAlert {
  
  /**
   * The Event that is associated with this alert.  Will only
   * be a single event.
  
   * @return Event
   */
  public Event getEvent();
  /**
   * Method setEvent.
   * @param event Event
   */
  public void setEvent(Event event);
  
  /**
   * Get the Alfresco NodeRef associated with this object.
  
   * @return NodeRef
   */
  public NodeRef getNodeRef();
  /**
   * Method setNodeRef.
   * @param nodeRef NodeRef
   */
  public void setNodeRef(NodeRef nodeRef);
  
}
