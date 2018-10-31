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
/*
 * Copyright (c) 2004-2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author: chandrika $'
 * '$Date: 2011-01-03 18:39:23 -0800 (Mon, 03 Jan 2011) $' 
 * '$Revision: 26610 $'
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */

package org.kepler.job;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Support class for Hadoop Spark job manager support Class Job uses the
 * methods of a supporter class to submit jobs and check status
 */
public class JobSupportSpark implements JobSupport {

	private static final Log log = LogFactory.getLog(JobSupportSpark.class
			.getName());
	private static final boolean isDebugging = log.isDebugEnabled();

	public JobSupportSpark() {
	}

	/**
	 * Method init.
	 * @param sparkBinPath String
	 * @see org.kepler.job.JobSupport#init(String)
	 */
	public void init(String sparkBinPath) {
		if (sparkBinPath != null && !sparkBinPath.trim().equals("")) {
			String binPath = new String(sparkBinPath);
			if (!sparkBinPath.endsWith("/"))
				binPath += "/";
			_sparkSubmitCmd = binPath + _sparkSubmitCmd;
			_sparkStatusCmd = binPath + _sparkStatusCmd;
			_sparkDeleteCmd = binPath + _sparkDeleteCmd;
		}
	}

	/**
	 * Create a submission file for the specific job manager, based on the
	 * information available in Job: - executable name - input files - output
	 * files - arguments for the job
	 * @param filename String
	 * @param job Job
	 * @return boolean
	 * @see org.kepler.job.JobSupport#createSubmitFile(String, Job)
	 */
	public boolean createSubmitFile(String filename, Job job) {

		return false;
	}

	/**
	 * Submit command for Spark return: the command for submission
	 * @param submitFile String
	 * @param options String
	 * @return String
	 * @see org.kepler.job.JobSupport#getSubmitCmd(String, String)
	 */
	public String getSubmitCmd(String submitFile, String options) {

		String _commandStr;
		if (options != null)
			_commandStr = _sparkSubmitCmd + " " + options + " " + submitFile;
		else
                        // TODO: no submitFile for first cut at Spark job submission
			// _commandStr = _sparkSubmitCmd + " " + submitFile;
			_commandStr = _sparkSubmitCmd;

		return _commandStr;
	}

	/**
	 * Parse output of submission and get information: jobID return String jobID
	 * on success throws JobException at failure (will contain the error stream
	 * or output stream)
	 * @param output String
	 * @param error String
	 * @return String
	 * @throws JobException
	 * @see org.kepler.job.JobSupport#parseSubmitOutput(String, String)
	 */
	public String parseSubmitOutput(String output, String error)
			throws JobException {

		// System.out.println("====Spark parse: picking the jobid from output...");
		/*
		 * Spark submit output: on success, it is: job submitted with id: <jobid>
		 *                      on error, it is: job failed to submit!
		 */
		String jobID = null;
		int idx = output.indexOf("\n");

		if (idx > -1) {
			String firstrow = output.substring(0, idx);
			if (firstrow.startsWith("job submitted with id: "))
				jobID = firstrow.substring(23);
			else
				throw new JobException("Error at submission of Spark job: "
						+ firstrow);
			if (isDebugging)
				log.debug("Spark parse: jobID = " + jobID + " firstrow = "
						+ firstrow);
		}
		return jobID;
	} // end-of-submit

	/**
	 * Get the command to ask the status of the job return: the String of
	 * command
	 * @param jobID String
	 * @return String
	 * @see org.kepler.job.JobSupport#getStatusCmd(String)
	 */
	public String getStatusCmd(String jobID) {
		String _commandStr = _sparkStatusCmd + jobID;
		return _commandStr;
	}

	/**
	 * Parse output of status check command and get status info return: a
	 * JobStatusInfo object, or throws an JobException with the error output
	 * @param jobID String
	 * @param exitCode int
	 * @param output String
	 * @param error String
	 * @return JobStatusInfo
	 * @throws JobException
	 * @see org.kepler.job.JobSupport#parseStatusOutput(String, int, String, String)
	 */
	public JobStatusInfo parseStatusOutput(String jobID, int exitCode,
			String output, String error) throws JobException {

		// Spark vspark.sh wrapper gives back multiple lines of output
		// with each line being a different job attribute
		// only the status attribute is given if the job isn't recognized

		// System.out.println("+++++ status: picking the status from output" );
		JobStatusInfo stat = new JobStatusInfo();
		stat.statusCode = JobStatusCode.NotInQueue;
		stat.jobID = jobID;

		boolean foundStatus = false;
        String sa[] = output.split("\n");
		for (int i = 0; i < sa.length; i++) {
			// System.out.println("Spark status string " + i + " = " + sa[i]);
			if (sa[i].startsWith("user: "))
				stat.owner = sa[i].substring(6);

			else if (sa[i].startsWith("name: ")) {
				// String jobName = sa[i].substring(6);
			}
			else if (sa[i].startsWith("status: ")) {
				foundStatus = true;
				String state = sa[i].substring(8);
				if (state.equals("UNKNOWN"))
					stat.statusCode = JobStatusCode.Wait;
				else if (state.equals("PENDING"))
					stat.statusCode = JobStatusCode.Wait;
				else if (state.equals("RUNNING"))
					stat.statusCode = JobStatusCode.Running;
				else if (state.equals("SUCCEEDED"))
					stat.statusCode = JobStatusCode.Success;
				else if (state.equals("FAILED"))
					stat.statusCode = JobStatusCode.Failed;
			}
			else if (sa[i].startsWith("started: "))
				stat.submissionTime = sa[i].substring(9);

			else if (sa[i].startsWith("elapsed: "))
				stat.runTime = sa[i].substring(9);
			else if (sa[i].startsWith("output: ")) {
				StringBuffer sb = new StringBuffer();
				// skip over 8 lines because they aren't useful output
				for (int it = i+8; it < sa.length; it++) {
					sb.append(sa[it]);
					sb.append("\n");
				}
				stat.consoleOutput = sb.toString();
				// the output is always the last part of the status so
				// don't try to process anything else
				break;
			}
			

			if (isDebugging) {
				log
						.debug("Spark status Values: jobid=" + stat.jobID
								+ " owner=" + stat.owner
								+ " submit/startTime="
								+ stat.submissionTime + " status=["
								+ stat + "]");
			}
		}
		// System.out.println("Spark status = " + stat.statusCode);

		if (!foundStatus)
			stat.statusCode = JobStatusCode.Error;

		stat.statusRecordTime = new Date();
		return stat;
	}

	/**
	 * Get the command to remove a job from queue (either running or waiting
	 * jobs). return: the String of command
	 * @param jobID String
	 * @return String
	 * @see org.kepler.job.JobSupport#getDeleteCmd(String)
	 */
	public String getDeleteCmd(String jobID) {
		String _commandStr = _sparkDeleteCmd + jobID;
		return _commandStr;
	}

	/**
	 * Parse output of delete command. return: true if stdout contains the expected output
	 * and exitCode is 0, otherwise return false
	 * @param jobID String
	 * @param exitCode int
	 * @param output String
	 * @param error String
	 * @return boolean
	 * @throws JobException
	 * @see org.kepler.job.JobSupport#parseDeleteOutput(String, int, String, String)
	 */
	public boolean parseDeleteOutput(String jobID, int exitCode, String output,
			String error) throws JobException {
		if (exitCode == 0 && output.contains("job killed"))
			return true;
		else
			return false;
	}

	// ////////////////////////////////////////////////////////////////////
	// // private variables ////

	// The combined command to execute.
	private String _sparkSubmitCmd = "vspark.sh --submit --near";
	private String _sparkStatusCmd = "vspark.sh --status ";
	private String _sparkDeleteCmd = "vspark.sh --kill ";

	/**
	 * Method getTaskStatusCmd.
	 * @param jobID String
	 * @return String
	 * @throws NotSupportedException
	 * @see org.kepler.job.JobSupport#getTaskStatusCmd(String)
	 */
	public String getTaskStatusCmd(String jobID) throws NotSupportedException {
		throw new NotSupportedException("Task parallel jobs are not supported");
	}

	/**
	 * Method parseTaskStatusOutput.
	 * @param jobID String
	 * @param numTasks int
	 * @param exitCode int
	 * @param output String
	 * @param error String
	 * @return TaskParallelJobStatusInfo
	 * @throws JobException
	 * @throws NotSupportedException
	 * @see org.kepler.job.JobSupport#parseTaskStatusOutput(String, int, int, String, String)
	 */
	public TaskParallelJobStatusInfo parseTaskStatusOutput(String jobID,
			int numTasks, int exitCode, String output, String error)
			throws JobException, NotSupportedException {
		throw new NotSupportedException("Task parallel jobs are not supported");
	}

} // end-of-class-JobSupportSpark
