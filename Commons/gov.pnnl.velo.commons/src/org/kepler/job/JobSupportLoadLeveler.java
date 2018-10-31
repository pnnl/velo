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
 * Support class for IBM's LoadLeveler job manager support Class JobManager uses
 * the methods of a supporter class to submit jobs and check status
 */
public class JobSupportLoadLeveler implements JobSupport {

	private static final Log log = LogFactory
			.getLog(JobSupportLoadLeveler.class.getName());
	private static final boolean isDebugging = log.isDebugEnabled();

	public JobSupportLoadLeveler() {
	}

	/**
	 * Method init.
	 * @param llBinPath String
	 * @see org.kepler.job.JobSupport#init(String)
	 */
	public void init(String llBinPath) {
		if (llBinPath != null && !llBinPath.trim().equals("")) {
			String binPath = new String(llBinPath);
			if (!llBinPath.endsWith("/"))
				binPath += "/";
			_llSubmitCmd = binPath + _llSubmitCmd;
			_llStatusCmd = binPath + _llStatusCmd;
			_llDeleteCmd = binPath + _llDeleteCmd;
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
	 * Submit command for LoadLeveler return: the command for submission
	 * @param submitFile String
	 * @param options String
	 * @return String
	 * @see org.kepler.job.JobSupport#getSubmitCmd(String, String)
	 */
	public String getSubmitCmd(String submitFile, String options) {

		String _commandStr;
		if (options != null)
			_commandStr = _llSubmitCmd + " " + options + " " + submitFile;
		else
			_commandStr = _llSubmitCmd + " " + submitFile;

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

		// System.out.println("====LoadLeveler parse: picking the jobid from output...");
		/*
		 * LoadLeveler llsubmit output is several lines: on success, there is a
		 * line: "llsubmit: The job "jobID" has been submitted." if submitfile
		 * does not exists or other error:??
		 */
		String jobID = null;

		String sa[] = output.split("\n"); // cut up lines
		int idx;
		for (int i = 0; i < sa.length; i++) {
			// if (isDebugging) log.debug("LoadLeveler status string " + i +
			// " = "+ sa[i]);
			idx = sa[i].indexOf(" has been submitted");
			if (idx > -1) {
				// Successful job submission, jobID is in this line.
				// Cut to the second quote, excluding the quote.
				String temp = output.substring(0, idx - 1);
				// start of jobid string after the first quote
				int qidx = output.indexOf("\"");
				if (qidx > -1) {
					// cut from the first quote, excluding the qoute
					jobID = temp.substring(qidx + 1);
					if (isDebugging)
						log.debug("LoadLeveler parse: jobID = " + jobID
								+ " temp = " + temp);
				}
			}
		}

		if (jobID == null) {
			if (error != null && error.length() > 0)
				throw new JobException(
						"Error at submission of LoadLeveler job: " + error);
			else
				throw new JobException(
						"Error at submission of LoadLeveler job: " + output);
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
		String _commandStr = _llStatusCmd + jobID;
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

		// LoadLeveler status prints to stdout always, and never to stderror.
		// exitCode != 0 is error, but exitCode==0 still may mean that job is
		// not in queue.
		// If job is in queue, the formatted report looks like:
		// Step Id Owner Queue Date ST
		// ------------------------ ----------- ----------- --
		// s00601.287247.0 jxhan 07/12 09:10 NQ
		//
		// 1 job step(s) in query, 0 waiting, 0 pending, 0 running, 1 held, 0
		// preempted
		//
		// If job is not in the queue anymore, the message is
		// ""llq: There is currently no job status to report."

		// System.out.println("+++++ status: picking the status from output" );
		JobStatusInfo stat = new JobStatusInfo();
		stat.statusCode = JobStatusCode.NotInQueue;

		if (exitCode != 0) {
			// error case, error text in output
			throw new JobException("LoadLeveler status query error:\n" + output);
		}

		// now we have 0 exitCode, so either get status info, or no job message

		boolean foundStatus = false;
		String localJobID = createLocalJobID(jobID); // a trick for LoadLeveler

		String sa[] = output.split("\n");
		for (int i = 0; i < sa.length; i++) {
			// if (isDebugging) log.debug("LoadLeveler status string " + i +
			// " = "+ sa[i]);
			if (sa[i].trim().startsWith(localJobID)) {
				String vals[] = sa[i].trim().split("( )+", 9);
				if (vals.length >= 5) {
					String reportedJobID = vals[0].trim();
					stat.owner = vals[1].trim();
					stat.submissionTime = vals[2].trim() + " " + vals[3].trim();
					stat.runTime = new String("N/A");
					String sts = vals[4].trim();

					if (sts.equals("R") || // running
							sts.equals("ST") || // starting
							sts.equals("P") || // pending
							sts.equals("CK") || // checkpointing
							sts.equals("CP") || // prepare to complete
							sts.equals("C") || // completed
							sts.equals("E") || // preempted
							sts.equals("EP") || // preempt pending
							sts.equals("MP") // resume pending
					) {

						stat.statusCode = JobStatusCode.Running;

					} else if (sts.equals("I") || // idle
							sts.equals("NQ") || // not queued (for running)
							sts.equals("HU") || // user hold
							sts.equals("H") || // user hold
							sts.equals("HS") || // system hold
							sts.equals("S") || // system hold
							sts.equals("D") || // deferred
							sts.equals("V") || // vacated
							sts.equals("VP") || // vacated pending
							sts.equals("RP") // remove pending
					) {

						stat.statusCode = JobStatusCode.Wait;

					} else if (sts.equals("CA") || // cancelled
							sts.equals("TX") || // terminated
							sts.equals("RM") // removed
					) {

						stat.statusCode = JobStatusCode.NotInQueue;

					} else {
						/*
						 * possible states: NR never run X rejected XP reject
						 * pending
						 */
						stat.statusCode = JobStatusCode.Error;
					}
					foundStatus = true;
					if (isDebugging)
						log.debug("LoadLeveler status Values: jobid="
								+ stat.jobID + " owner=" + stat.owner
								+ " submissionTime=" + stat.submissionTime
								+ " status=[" + sts + "]");
				}
			}
		}
		// System.out.println("LoadLeveler status = " + stat.statusCode);

		if (!foundStatus) {
			if (output != null && output.length() > 0) {
				// it can be the message: llq: There is currently no job status
				// to report.
				if (output
						.startsWith("llq: There is currently no job status to report.")) {
					stat.statusCode = JobStatusCode.NotInQueue;
				} else {
					log.warn("Output string = [" + output + "] len="
							+ output.length());
					stat.statusCode = JobStatusCode.Error;
				}
			} else { // unknown thing happened, output is null
				throw new JobException(
						"LoadLeveler status produced an unknown situation for job "
								+ jobID);
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
		String _commandStr = _llDeleteCmd + jobID;
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

	/**
	 * Create the usable jobID "host.job.step" from the "fullhostname.job.step".
	 * Submission reports jobID with the full hostname, e.g.
	 * s00509.nersc.gov.410337.0 Status query / delete works for such ID,
	 * however, they report the id with short hostname, e.g. s00509.410337.0 so
	 * we need that short id to get the status.
	 * @param fullJobID String
	 * @return String
	 */
	private String createLocalJobID(String fullJobID) {
		String vals[] = fullJobID.trim().split("\\.");
		if (vals.length <= 3) {
			// our theory does not fit reality. Not NERSC? Just return as it is.
			return fullJobID;
		}

		String id = new String ();
		if (vals.length > 4)	// for format s00509.nersc.gov.410337.0
			id = vals[0] + "." + vals[vals.length - 2] + "." + vals[vals.length - 1];
		else					// for format s00509.nersc.gov.410337
			id = vals[0] + "." + vals[vals.length - 1];
		
		//System.out.println("full id = " + fullJobID + "   job id = " + id);
		return id;
	}

	// ////////////////////////////////////////////////////////////////////
	// // private variables ////

	// The combined command to execute.
	private String _llSubmitCmd = "llsubmit ";
  //some machines may NOT support -j option
  //private String _llStatusCmd = "llq -f %id %o %dq %st -j ";
  //-j looks like an optional option even on machines that support it   
	private String _llStatusCmd = "llq -f %id %o %dq %st ";  
	private String _llDeleteCmd = "llcancel ";

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

} // end-of-class-JobSupportLoadLeveler
