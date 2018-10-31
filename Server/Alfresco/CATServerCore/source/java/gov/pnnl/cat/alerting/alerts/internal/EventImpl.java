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
/**
 * 
 */
package gov.pnnl.cat.alerting.alerts.internal;

import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.Event;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Event object should be serializable to XML via XStream.
 *
 * @version $Revision: 1.0 $
 */
public class EventImpl implements Event {

  private String resourceName;
  private String uuid;
  private String changeType;
  private Actor eventPerpetrator;
  private Date eventTime;
  private String urlString;


  /* (non-Javadoc)
   * @see gov.pnl.dmi.alerting.alerts.Event#getChangeType()
   */
  public String getChangeType() {
    return changeType;
  }

  /* (non-Javadoc)
   * @see gov.pnl.dmi.alerting.alerts.Event#getEventPerpetrator()
   */
  public Actor getEventPerpetrator() {
    return eventPerpetrator;
  }

  /* (non-Javadoc)
   * @see gov.pnl.dmi.alerting.alerts.Event#getEventTime()
   */
  public Date getEventTime() {
    return eventTime;
  }

  /* (non-Javadoc)
   * @see gov.pnl.dmi.alerting.alerts.Event#getResourceName()
   */
  public String getResourceName() {
    return resourceName;
  }

  /* (non-Javadoc)
   * @see gov.pnl.dmi.alerting.alerts.Event#getResourceURL()
   */
  public URL getResourceURL() {
	  if (urlString == null) {
		  return null;
	  }
	  try {
		  return new URL(urlString);
	  } catch (MalformedURLException e) {
		  throw new RuntimeException(e);
	  }
  }

  /* (non-Javadoc)
   * @see gov.pnl.dmi.alerting.alerts.Event#getUUID()
   */
  public String getUUID() {
    return uuid;
  }

/**
 * Method setChangeType.
 * @param changeType String
 * @see gov.pnnl.cat.alerting.alerts.Event#setChangeType(String)
 */
public void setChangeType(String changeType) {
	this.changeType = changeType;
	
}

/**
 * Method setEventPerpetrator.
 * @param actor Actor
 * @see gov.pnnl.cat.alerting.alerts.Event#setEventPerpetrator(Actor)
 */
public void setEventPerpetrator(Actor actor) {
	this.eventPerpetrator = actor;
	
}

/**
 * Method setEventTime.
 * @param eventTime Date
 * @see gov.pnnl.cat.alerting.alerts.Event#setEventTime(Date)
 */
public void setEventTime(Date eventTime) {
	this.eventTime = eventTime;
	
}

/**
 * Method setResourceName.
 * @param resourceName String
 * @see gov.pnnl.cat.alerting.alerts.Event#setResourceName(String)
 */
public void setResourceName(String resourceName) {
	this.resourceName = resourceName;
	
}

/**
 * Method setResourceURL.
 * @param resourceURL URL
 * @see gov.pnnl.cat.alerting.alerts.Event#setResourceURL(URL)
 */
public void setResourceURL(URL resourceURL) {
	if (resourceURL != null) {
		this.urlString = resourceURL.toExternalForm();
	} else {
		this.urlString = null;
	}
	
}

/**
 * Method setUUID.
 * @param uuid String
 * @see gov.pnnl.cat.alerting.alerts.Event#setUUID(String)
 */
public void setUUID(String uuid) {
	this.uuid = uuid;
	
}

  
}
