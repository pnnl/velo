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
package gov.pnnl.cat.ui.rcp.exceptionhandling;

import gov.pnnl.velo.model.CmsPath;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;

/**
 */
public class ErrorEvent implements Comparable<ErrorEvent>, IAdaptable {
  private CmsPath path;
  private IStatus status;
  private Date date;

  /**
   * Constructor for ErrorEvent.
   * @param path CmsPath
   * @param status IStatus
   */
  public ErrorEvent(CmsPath path, IStatus status) {
    this.path = path;
    this.status = status;
    this.date = new Date();
  }

  /**
   * Method getErrorStatus.
   * @return IStatus
   */
  public IStatus getErrorStatus() {
    return this.status;
  }

  /**
   * Method getPath.
   * @return CmsPath
   */
  public CmsPath getPath() {
    return this.path;
  }

  /**
   * Method getDate.
   * @return Date
   */
  public Date getDate() {
    return this.date;
  }

  /**
   * Method compareTo.
   * @param event ErrorEvent
   * @return int
   */
  public int compareTo(ErrorEvent event) {
    return event.getDate().compareTo(date);
  }

  /**
   * Method getAdapter.
   * @param adapter Class
   * @return Object
   */
  public Object getAdapter(Class adapter) {
    return null;
  }
}
