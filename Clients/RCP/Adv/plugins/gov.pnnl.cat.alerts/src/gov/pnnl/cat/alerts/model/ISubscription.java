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
import gov.pnnl.velo.model.CmsPath;

import java.util.Calendar;

/**
 */
public interface ISubscription {
  public final static CmsPath PATH_SUB_ROOT = new CmsPath("/app:company_home/sub:Subscriptions");

  /**
   */
  public enum Frequency {
    DAILY,
    WEEKLY,
    HOURLY
  }

  /**
   */
  public enum Channel {
    REPOSITORY,
    EMAIL
  }

  /**
   */
  public enum Type {
    REPOSITORY,
    SEARCH
  }

  /**
   */
  public enum ChangeType {
    NEW,
    MODIFIED,
    DELETED,
    EXPIRED,
    EXPIRING
  }

  /**
  
   * @return the type of subscription */
  public Type getType();

  /**
  
   * @return the name */
  public String getName();

  /**
  
   * @return the title */
  public String getTitle();

  /**
  
   * @return the frequency */
  public Frequency getFrequency();

  /**
  
   * @return the channel */
  public Channel[] getChannels();

  /**
  
   * @return the user */
  public IUser getUser();

  /**
  
   * @return the uuid */
  public String getId();

  /**
  
   * @return the created */
  public Calendar getCreated();
}
