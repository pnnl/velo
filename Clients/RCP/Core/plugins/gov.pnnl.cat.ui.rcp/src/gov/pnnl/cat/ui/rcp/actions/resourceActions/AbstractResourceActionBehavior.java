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
package gov.pnnl.cat.ui.rcp.actions.resourceActions;

import gov.pnnl.cat.ui.rcp.SystemManager;

import org.eclipse.core.runtime.jobs.Job;

/**
 */
public abstract class AbstractResourceActionBehavior implements IResourceActionBehavior {

  /**
   * Method scheduleJob.
   * @param job Job
   * @param delay long
   */
  public static void scheduleJob(Job job, long delay) {
    job.addJobChangeListener(SystemManager.getInstance());

//  IProgressConstants.PROGRESS_VIEW_ID = CatViewIDs.PROGRESS_MONITOR_VIEW;
//  transferJob.setProperty(IProgressConstants.PROGRESS_VIEW_ID, CatViewIDs.PROGRESS_MONITOR_VIEW);
    job.setUser(true);
    job.setPriority(Job.LONG);
    job.schedule(delay);
  }

}
