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

import java.net.URL;
import java.util.Date;

/**
 * Keeps track of an event that happened, that we are monitoring
 * for a subscription.  Needs to be generic enough that it can
 * work for any type of event.
 *
 * @version $Revision: 1.0 $
 */
public interface Event {

  /**
   * The name of the resource that was changed.  Could be
   * file1.txt, "Pathogen Fact Sheet", or "Carina Lansing" - 
   * whatever you are monitoring.
  
   * @return String
   */
  public String getResourceName();
  /**
   * Method setResourceName.
   * @param resourceName String
   */
  public void setResourceName(String resourceName);
  
  /**
   * Get a URL to the resource that can be accessed via the web
   * and referenced in an alert message.  Need this in case the
   * resource gets deleted, and we can no longer look up information
   * on it.
  
   * @return URL
   */
  public URL getResourceURL();
  /**
   * Method setResourceURL.
   * @param resourceURL URL
   */
  public void setResourceURL(URL resourceURL);
  
  /**
   * Get the unique identifier for the resource that was changed.
  
   * @return String
   */
  public String getUUID();
  /**
   * Method setUUID.
   * @param uuid String
   */
  public void setUUID(String uuid);
  
  /**
   * The type of change that occurred for this resource.  For example,
   * could be "New" or "Deleted", if this event has to do with a 
   * file/folder.  Could be something else, depending on the 
   * subscription type.
  
   * @return String
   */
  public String getChangeType();
  /**
   * Method setChangeType.
   * @param changeType String
   */
  public void setChangeType(String changeType);
  
  /**
   * Who caused the event.
  
   * @return Actor
   */
  public Actor getEventPerpetrator();
  /**
   * Method setEventPerpetrator.
   * @param actor Actor
   */
  public void setEventPerpetrator(Actor actor);
  
  /**
   * The time the event occurred.
  
   * @return Date
   */
  public Date getEventTime();
  /**
   * Method setEventTime.
   * @param eventTime Date
   */
  public void setEventTime(Date eventTime);

}
