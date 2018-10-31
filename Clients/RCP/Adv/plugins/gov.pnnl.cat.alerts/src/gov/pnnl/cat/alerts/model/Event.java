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
package gov.pnnl.cat.alerts.model;

import gov.pnnl.cat.alerts.model.ISubscription.ChangeType;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.velo.model.CmsPath;

import java.util.Calendar;

import org.eclipse.core.runtime.PlatformObject;

/**
 */
public class Event extends PlatformObject implements IEvent {
  private ChangeType changeType;
  private Calendar time;
  private IUser perpetrator;
  private String resourceName;
  private String url;
  private String id;
  private boolean valid;
  private CmsPath path;

  /**
  
   * @return the changeType * @see gov.pnnl.cat.alerts.model.IEvent#getChangeType()
   */
  public ChangeType getChangeType() {
    return changeType;
  }
  /**
   * @param changeType the changeType to set
   */
  public void setChangeType(ChangeType changeType) {
    this.changeType = changeType;
  }
  /**
  
   * @return the time * @see gov.pnnl.cat.alerts.model.IEvent#getTime()
   */
  public Calendar getTime() {
    return time;
  }
  /**
   * @param time the time to set
   */
  public void setTime(Calendar time) {
    this.time = time;
  }
  /**
  
   * @return the perpetrator * @see gov.pnnl.cat.alerts.model.IEvent#getPerpetrator()
   */
  public IUser getPerpetrator() {
    return perpetrator;
  }
  /**
   * @param perpetrator the perpetrator to set
   */
  public void setPerpetrator(IUser perpetrator) {
    this.perpetrator = perpetrator;
  }
  /**
  
   * @return the resourceName * @see gov.pnnl.cat.alerts.model.IEvent#getResourceName()
   */
  public String getResourceName() {
    return resourceName;
  }
  /**
   * @param resourceName the resourceName to set
   */
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }
  /**
  
   * @return the url * @see gov.pnnl.cat.alerts.model.IEvent#getUrl()
   */
  public String getUrl() {
    return url;
  }
  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }
  /**
  
   * @return the uuid * @see gov.pnnl.cat.alerts.model.IEvent#getId()
   */
  public String getId() {
    return id;
  }
  /**
   * @param uuid the uuid to set
   */
  public void setId(String uuid) {
    this.id = uuid;
  }
  
  /**
   * @param path the path to set
   */
  public void setResourcePath(CmsPath path) {
    this.path = path;
  }
  
  /**
   * Method getResourcePath.
   * @return CmsPath
   * @see gov.pnnl.cat.alerts.model.IEvent#getResourcePath()
   */
  @Override
  public CmsPath getResourcePath() {
    return this.path;
  }
  
  /**
  
   * @return the valid * @see gov.pnnl.cat.alerts.model.IEvent#isValid()
   */
  public boolean isValid() {
    return valid;
  }
  /**
   * @param valid the valid to set
   */
  public void setValid(boolean valid) {
    this.valid = valid;
  }
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return path.toDisplayString();
  }

}
