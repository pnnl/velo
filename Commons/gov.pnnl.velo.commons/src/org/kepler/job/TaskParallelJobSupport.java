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
package org.kepler.job;

/*
 * Author: Jared Chase
 */

/**
 * Abstract interface for jobmanager support classes Class Job uses the methods
 * of a supporter class to submit jobs and check task status
 */
interface TaskParallelJobSupport extends JobSupport {
  
    /*
     * Parse output of task status check command and get status info
     * @return: a JobStatusInfo object, or throws an JobException with the error output
     */
    /**
     * Method parseTaskStatusOutput.
     * @param jobID String
     * @param numTasks int
     * @param exitCode int
     * @param output String
     * @param error String
     * @return TaskParallelJobStatusInfo
     * @throws JobException
     * @see org.kepler.job.JobSupport#parseTaskStatusOutput(String, int, int, String, String)
     */
    public TaskParallelJobStatusInfo parseTaskStatusOutput (
        String jobID,
        int numTasks,
        int exitCode,
        String output,
        String error )  throws JobException;


    /** Get the command to ask the status of each task
     *   return: the String of command
     * @param jobID String
     * @return String
     * @see org.kepler.job.JobSupport#getTaskStatusCmd(String)
     */
    public String getTaskStatusCmd (String jobID);

}
