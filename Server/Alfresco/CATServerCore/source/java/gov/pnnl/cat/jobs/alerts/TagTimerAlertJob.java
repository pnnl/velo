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
package gov.pnnl.cat.jobs.alerts;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 */
public class TagTimerAlertJob implements Job {

  /**
   * Method execute.
   * @param executionContext JobExecutionContext
   * @throws JobExecutionException
   * @see org.quartz.Job#execute(JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext executionContext) throws JobExecutionException {
    TagTimerAlertWork work = (TagTimerAlertWork)executionContext.getJobDetail().getJobDataMap().get("bean");
    if(work != null) {
      // Change the current thread's name so we know what is running
      String oldName = Thread.currentThread().getName();
      Thread.currentThread().setName("Tag&Timer Thread");
      try {
        work.run();
      } finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

}
