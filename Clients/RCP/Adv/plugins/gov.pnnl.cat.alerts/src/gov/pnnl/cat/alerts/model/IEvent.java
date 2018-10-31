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

import org.eclipse.core.runtime.IAdaptable;

/**
 */
public interface IEvent extends IAdaptable {

  /**
  
   * @return the changeType */
  public ChangeType getChangeType();

  /**
  
   * @return the time */
  public Calendar getTime();

  /**
  
   * @return the perpetrator */
  public IUser getPerpetrator();

  /**
  
   * @return the resourceName */
  public String getResourceName();
  
  /**
  
   * @return the resourcePath */
  public CmsPath getResourcePath();
  
  /**
  
   * @return the url */
  public String getUrl();

  /**
  
   * @return the uuid */
  public String getId();

  /**
  
   * @return - true if this event represents a valid document; false if the document is
   * no longer accessible. */
  public boolean isValid();
}
