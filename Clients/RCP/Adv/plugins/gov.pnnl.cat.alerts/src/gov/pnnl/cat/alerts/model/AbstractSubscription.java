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

import gov.pnnl.cat.core.resources.security.IUser;

import java.util.Calendar;

import org.eclipse.core.runtime.PlatformObject;

/**
 */
public abstract class AbstractSubscription extends PlatformObject implements ISubscription {
  private String name;
  private String title;
  private Frequency frequency;
  private Channel[] channels;
  private IUser user;
  private String id;
  private Calendar created;

  /**
   * Method getName.
   * @return String
   * @see gov.pnnl.cat.alerts.model.ISubscription#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Method getTitle.
   * @return String
   * @see gov.pnnl.cat.alerts.model.ISubscription#getTitle()
   */
  @Override
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Method getFrequency.
   * @return Frequency
   * @see gov.pnnl.cat.alerts.model.ISubscription#getFrequency()
   */
  @Override
  public Frequency getFrequency() {
    return frequency;
  }

  /**
   * @param frequency the frequency to set
   */
  public void setFrequency(Frequency frequency) {
    this.frequency = frequency;
  }

  /**
   * Method getChannels.
   * @return Channel[]
   * @see gov.pnnl.cat.alerts.model.ISubscription#getChannels()
   */
  @Override
  public Channel[] getChannels() {
    return channels;
  }

  /**
  
   * @param channels Channel[]
   */
  public void setChannels(Channel... channels) {
    this.channels = channels;
  }

  /**
   * Method getUser.
   * @return IUser
   * @see gov.pnnl.cat.alerts.model.ISubscription#getUser()
   */
  @Override
  public IUser getUser() {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(IUser user) {
    this.user = user;
  }

  /**
   * Method getId.
   * @return String
   * @see gov.pnnl.cat.alerts.model.ISubscription#getId()
   */
  @Override
  public String getId() {
    return id;
  }

  /**
  
   * @param uuid String
   */
  public void setId(String uuid) {
    this.id = uuid;
  }

  /**
   * Method getCreated.
   * @return Calendar
   * @see gov.pnnl.cat.alerts.model.ISubscription#getCreated()
   */
  @Override
  public Calendar getCreated() {
    return created;
  }

  /**
   * @param created the created to set
   */
  public void setCreated(Calendar created) {
    this.created = created;
  }

}
