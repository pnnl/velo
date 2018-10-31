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
 * Support class for NCCS job manager support Class Job uses the methods of a
 * supporter class to submit jobs and check status
 */
public class JobSupportNCCS implements JobSupport {

	private static final Log log = LogFactory.getLog(JobSupportNCCS.class
			.getName());
	private static final boolean isDebugging = log.isDebugEnabled();

	public JobSupportNCCS() {
	}

	/**
	 * Method init.
	 * @param nccsBinPath String
	 * @see org.kepler.job.JobSupport#init(String)
	 */
	public void init(String nccsBinPath) {
		if (nccsBinPath != null && !nccsBinPath.trim().equals("")) {
			String binPath = new String(nccsBinPath);
			if (!nccsBinPath.endsWith("/"))
				binPath += "/";
			_nccsSubmitCmd = binPath + _nccsSubmitCmd;
			_nccsStatusCmd = binPath + _nccsStatusCmd;
			_nccsDeleteCmd = binPath + _nccsDeleteCmd;
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
	 * Submit command for NCCS return: the command for submission
	 * @param submitFile String
	 * @param options String
	 * @return String
	 * @see org.kepler.job.JobSupport#getSubmitCmd(String, String)
	 */
	public String getSubmitCmd(String submitFile, String options) {

		String _commandStr;
		if (options != null)
			_commandStr = _nccsSubmitCmd + " " + options + " " + submitFile;
		else
			_commandStr = _nccsSubmitCmd + " " + submitFile;

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

		// System.out.println("====NCCS parse: picking the jobid from output...");
		/*
		 * NCCS qsub output is simple: on success, it is the jobID in one single
		 * line. if submitfile does not exists or other error, messages are
		 * printed on stdout stderr is empty
		 */
		String jobID = null;
		int idx = output.indexOf("\n");

		if (idx > -1) {
			String firstrow = output.substring(0, idx);
			if (firstrow.matches("[0-9]*.*")) {
				jobID = firstrow;
			}
			if (isDebugging)
				log.debug("NCCS parse: jobID = " + jobID + " firstrow = "
						+ firstrow);
		}

		if (jobID == null) {
			if (error != null && error.length() > 0)
				throw new JobException("Error at submission of NCCS job: "
						+ error);
			else
				throw new JobException("Error at submission of NCCS job: "
						+ output);
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
		String _commandStr = _nccsStatusCmd + jobID;
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

		// NCCS status does not use exitCode. It can show error, but in real it
		// can mean only that
		// job is not in the queue anymore, which is good...

		// System.out.println("+++++ status: picking the status from output" );
		JobStatusInfo stat = new JobStatusInfo();
		stat.statusCode = JobStatusCode.NotInQueue;

		boolean foundStatus = false;

		String sa[] = output.split("\n");
		int idx;
		for (int i = 0; i < sa.length; i++) {
			// System.out.println("NCCS status string " + i + " = "+ sa[i]);
			String vals[] = sa[i].trim().split("( )+", 9);
			if (jobID.startsWith(vals[0].trim())) { // jobID may be longer than
													// the first field which is
													// limited in length
				if (vals.length >= 5) {
					stat.jobID = jobID;
					String jobName = vals[1].trim();
					stat.owner = vals[2].trim();
					stat.runTime = vals[3].trim();
					String sts = vals[4].trim();
					switch (sts.charAt(0)) {
					case 'R':
					case 'E':
						stat.statusCode = JobStatusCode.Running;
						break;
					case 'Q':
					case 'H':
					case 'T':
					case 'W':
					case 'S':
						stat.statusCode = JobStatusCode.Wait;
						break;
					default:
						stat.statusCode = JobStatusCode.Wait;
					}
					foundStatus = true;
					if (isDebugging)
						log.debug("NCCS status Values: jobid=" + stat.jobID
								+ " owner=" + stat.owner + " runTime="
								+ stat.runTime + " status=[" + sts + "]");
				}
			}
		}
		// System.out.println("NCCS status = " + stat.statusCode);

		if (!foundStatus) {
			if (error != null && error.length() > 0) {
				// it can be the message: qstat: Unknown Job Id ...
				if (error.startsWith("qstat: Unknown Job Id")) {
					stat.jobID = jobID;
					stat.statusCode = JobStatusCode.NotInQueue;
				} else {
					log.warn("Error string = [" + error + "] len="
							+ error.length());
					stat.jobID = jobID;
					stat.statusCode = JobStatusCode.Error;
				}
			} else { // not an error, just job is not in the job queue now
				stat.jobID = jobID;
				stat.statusCode = JobStatusCode.NotInQueue;
			}
		}
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
		String _commandStr = _nccsDeleteCmd + jobID;
		return _commandStr;
	}

	/**
	 * Parse output of delete command. return: true or false indicating that the
	 * command was successful or not
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
		if (exitCode == 0)
			return true;
		else
			return false;
	}

	// ////////////////////////////////////////////////////////////////////
	// // private variables ////

	// The combined command to execute.
	private String _nccsSubmitCmd = "qsub ";
	private String _nccsStatusCmd = "qstat ";
	private String _nccsDeleteCmd = "qdel ";

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

} // end-of-class-JobSupportNCCS
