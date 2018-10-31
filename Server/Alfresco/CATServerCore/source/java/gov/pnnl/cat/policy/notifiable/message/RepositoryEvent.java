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
package gov.pnnl.cat.policy.notifiable.message;

import java.io.Serializable;

/**
 * Base class for any repository events.  Determines what node was being affected
 * nodePath, nodeId, eventPerpetrator, and eventTimestamp are all required!
 * See comments below for how to format each of these fields
 * @author D3G574
 *
 * @version $Revision: 1.0 $
 */
public class RepositoryEvent implements Serializable {
  
  public static final String TYPE_NODE_ADDED = "nodeAdded";
  public static final String TYPE_NODE_REMOVED = "nodeRemoved";
  public static final String TYPE_PROPERTY_ADDED = "propertyAdded";
  public static final String TYPE_PROPERTY_REMOVED = "propertyRemoved";
  public static final String TYPE_PROPERTY_CHANGED = "propertyChanged";
  public static final String TYPE_TARGET_NODE_MOVED = "targetNodeMoved";
  
  public static final String PROPERTY_NEW_TARGET_LOCATION = "newLocation";
  
  private static final long serialVersionUID = 7942198684895463724L;
  
  private String eventType;

  /**
   * nodePath should be constructed via:
   * nodeService.getPath(nodeRef).toString();
   */
  private String nodePath;

  /**
   * nodeId is the uuid of the node:
   * nodeRef.getId()
   */
  private String nodeId;

  /**
   * eventPerpetrator is the username that caused the change that created this event
   */
  private String eventPerpetrator;

  /**
   * System.currentTimeMillis() equivalent of when this event occurred
   */
  private long eventTimestamp;
  
  /**
   * propertyName should be in the form of QName.toString()
   */
  private String propertyName;

  /**
   * propertyValue should be Object.toString(), where Object is the actual property value
   */
  private String propertyValue;

  /**
   * Constructor for RepositoryEvent.
   * @param eventType String
   */
  public RepositoryEvent(String eventType) {
    this.eventType = eventType;
  }
  
  public RepositoryEvent() {
    
  }
  
  /**
   * Method getNodeId.
   * @return String
   */
  public String getNodeId() {
    return nodeId;
  }

  /**
   * Method setNodeId.
   * @param nodeId String
   */
  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  /**
   * Method getNodePath.
   * @return String
   */
  public String getNodePath() {
    return nodePath;
  }

  /**
   * Method setNodePath.
   * @param nodePath String
   */
  public void setNodePath(String nodePath) {
    this.nodePath = nodePath;
  }

  /**
   * Method getEventPerpetrator.
   * @return String
   */
  public String getEventPerpetrator() {
    return eventPerpetrator;
  }

  /**
   * Method setEventPerpetrator.
   * @param eventPerpetrator String
   */
  public void setEventPerpetrator(String eventPerpetrator) {
    this.eventPerpetrator = eventPerpetrator;
  }

  /**
   * Method getEventTimestamp.
   * @return long
   */
  public long getEventTimestamp() {
    return eventTimestamp;
  }

  /**
   * Method setEventTimestamp.
   * @param eventTimestamp long
   */
  public void setEventTimestamp(long eventTimestamp) {
    this.eventTimestamp = eventTimestamp;
  }

  /**
   * Method getEventType.
   * @return String
   */
  public String getEventType() {
    return eventType;
  }

  /**
   * Method setEventType.
   * @param eventType String
   */
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
  

  /**
   * Method getPropertyName.
   * @return String
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Method setPropertyName.
   * @param propertyName String
   */
  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  /**
   * Method getPropertyValue.
   * @return String
   */
  public String getPropertyValue() {
    return propertyValue;
  }

  /**
   * Method setPropertyValue.
   * @param propertyValue String
   */
  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }
  
}
