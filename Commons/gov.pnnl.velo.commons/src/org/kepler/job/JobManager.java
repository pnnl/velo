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
 * '$Date: 2011-01-03 18:45:51 -0800 (Mon, 03 Jan 2011) $' 
 * '$Revision: 26611 $'
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kepler.ssh.ExecException;
import org.kepler.ssh.ExecFactory;
import org.kepler.ssh.ExecInterface;
import org.kepler.ssh.LocalExec;

public class JobManager {

	private JobSupport jobSupport = null; // job support class

	private ExecInterface execObject; // class for remote/local execution

	private String host; // remote host of jobmanager
	private String user; // user at remote host to log in with ssh
	
	private String jobManagerName; // the support class name (just to generate
								   // Kepler ids)
	private String managerBinPath;
	
	private static final Log log = LogFactory
			.getLog(JobManager.class.getName());
	private static final boolean isDebugging = log.isDebugEnabled();

	protected JobManager() {
		preloadSupporterClasses();
	}

	/**
	 * Method getID.
	 * @return String
	 */
	public String getID() {
		return JobManagerFactory.createKey(jobManagerName, user + "@" + host);
	}
	
	private static void preloadSupporterClasses() {
		// We need to get the supporter class names directly, otherwise
		// java will not find it at runtime ???
		// System.out.println("Condor class: " + CondorSupport.class.getName());
		String c;
		c = JobSupportFork.class.getName();
		c = JobSupportCondor.class.getName();
		c = JobSupportPBS.class.getName();
		c = JobSupportNCCS.class.getName(); // Obsolete, same as PBS
		c = JobSupportLoadLeveler.class.getName();
		c = JobSupportSGE.class.getName();
		c = JobSupportMoab.class.getName();
	}

	/**
	 * Choose a jobmanager for execution <i>jobmanager</i> can be which is
	 * supported at that time: Condor <i>target</i> is either "localhost" or
	 * "user@host" is the machine where the jobmanager is running <binPath> is
	 * the full path to the jobmanager commands on that machine, or "" or null
	 * if they are in the default path if "'jobmanager'Support" class cannot be
	 * instantiated, a JobException is thrown
	 * @param jobmanager String
	 * @param target String
	 * @param binPath String
	 * @throws JobException
	 */
	protected void selectJobManager(String jobmanager, String target,
			String binPath) throws JobException {

		// instantiate the supporter class
		String classname = "org.kepler.job.JobSupport" + jobmanager;
		try {
			// System.out.println("Condor class: " +
			// Class.forName(classname).getName());
			jobSupport = (JobSupport) Class.forName(classname).newInstance();
		} catch (ClassNotFoundException cnf) {
			throw new JobException("Couldn't find class " + classname, cnf);
		} catch (InstantiationException ie) {
			throw new JobException("Couldn't instantiate an object of type "
					+ classname, ie);
		} catch (IllegalAccessException ia) {
			throw new JobException("Couldn't access class " + classname, ia);
		}

		// initialize the supporter class
		jobSupport.init(binPath);
		
		//Store the bin path
		managerBinPath = binPath;
		
		// store supporter name for Kepler use
		jobManagerName = jobmanager;

		// process the target
		if (target == null || target.trim().equals("")
				|| target.equals("local")|| target.equals("localhost")) {
			// localhost, finished
			execObject = new LocalExec();
			user = System.getProperty("user.name");
			host = new String("local");
		} else {
			int atPos = target.indexOf('@');
			if (atPos >= 0)
				user = target.substring(0, target.indexOf('@'));
			else
				user = System.getProperty("user.name");

			host = target.substring(atPos + 1);
			try {
				execObject = ExecFactory.getExecObject(user,host);
			} catch (ExecException e) {
				throw new JobException("Error connecting to " + user +"@"+host + " : " + e.toString(), e);
			}
		}
	}

	/**
	 * Submit a job, called from Job.submit(); boolean <i>overwrite</i>
	 * indicates whether old files that exist on the same directory should be
	 * removed before staging new files. As long jobIDs are not really unique,
	 * this is worth to be true. <i>options</i> can be a special options string
	 * for the actual jobmanager.
	 * 
	 * @param job Job
	 * @param overwrite boolean
	 * @param options String
	 * @return String
	 * @return: jobID as String if submission is successful (it is submitted and
	 *          real jobID of the submitted job can be retrieved) real jobID can
	 *          be found in job.status.jobID, but you do not need actually on
	 *          error throws JobException
   * @throws JobException
	 */

	protected String submit(Job job, boolean overwrite, String options)
			throws JobException {

		// first, get the submit file
		String submitFilePath = job.getSubmitFile(); // predefined submitfile?
		if (submitFilePath == null) { // no, create it now for the specific job
			// manager
			submitFilePath = new String(job.getLocalWorkdirPath()
					+ File.separator + "submitcmd." + job.getJobID());
			jobSupport.createSubmitFile(submitFilePath, job);
			job.setSubmitFile(submitFilePath, true);
		}

		// the submitfile will be in current working dir of job, so we have to
		// get the name of the submitfile without path
		File sf = new File(submitFilePath);
		String submitFileName = sf.getName();

		// job manager specific submission command
		String commandStr = jobSupport.getSubmitCmd(submitFileName, options);

		String cdCmd = "cd " + job.getWorkdirPath() + "; ";
		int exitCode = 0;

		if (commandStr == null || commandStr.trim().equals("")) {
			throw new JobException(
					"Supporter class could not give back meaningful command to submit your job");
		}

		// stage the files before submission
    try {
			// delete the remote working directory if asked by the submitter
			if (overwrite)
				execObject.deleteFile(job.getWorkdirPath(), true, false);
			
			execObject.createDir(job.getWorkdirPath(), true);
			
			boolean cpLocalBinScript = false;
			boolean cpRemoteBinScript = false;
			boolean binPathSpecified = false;
			String binFileName = job.getBinFile();
			File binFile = null;
			//if binFile exists check where it should be staged.
			if(binFileName!=null) {
				binFile = new File(binFileName);
				if(managerBinPath!=null && !(managerBinPath.trim().equals("")))
					binPathSpecified = true;
				else if(job.isBinFileLocal()){
					cpLocalBinScript = true;
				} else {
					cpRemoteBinScript = true;
				}
			}

			//If bin path is specified stage bin file to binpath
			if( binPathSpecified ){
				if(job.isBinFileLocal()){
					execObject.copyTo(binFile,managerBinPath,false);
				}else{
					StringBuffer cmd = new StringBuffer("cp ");
					cmd.append(binFileName);
					cmd.append(" ");
					cmd.append(managerBinPath);
					ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
					ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
					exitCode = _exec(new String(cmd), commandStdout, commandStderr);
					if (exitCode != 0) {
						throw new JobException(
								"Error at copying remote bin file into specified bin path."
										+ "\nStdout:\n" + commandStdout
										+ "\nStderr:\n" + commandStderr);
					}
				}
				
				//format bin file if it was copied from mac or windows
				Vector<File> vector = new Vector<File>();
				vector.add(binFile);
				formatFiles(managerBinPath,vector);
			}
			
			// stage local files/directories to the remote working directory
			// while also stripping end of line meta-characters from each file
			// if the file comes from mac or windows
			Vector<File> files = job.getLocalFiles();
			if(cpLocalBinScript){
				//add bin file to the list of files to be copied to workingdir
				files.add(binFile); 
			}
			execObject.copyTo(files, job.getWorkdirPath(), true);
			
			//Now try to change the format of the copied files
			//if the copy was from Windows or Mac
			formatFiles(job.getWorkdirPath(), files);
			
			
			// copy already remote files/directories to the working directory
			Vector<String> rfiles = job.getRemoteFiles();
			if (rfiles.size() > 0) {
				StringBuffer cmd = new StringBuffer("cp -r ");
				Iterator<String> it = rfiles.iterator();
				while (it.hasNext()) {
					cmd = cmd.append((String) it.next());
					cmd = cmd.append(" ");
				}
				if(cpRemoteBinScript){
					//add bin file to the list of files to be copied to workingdir
					cmd = cmd.append(binFileName);
					cmd = cmd.append(" ");
				}
				cmd = cmd.append(job.getWorkdirPath());

				if (isDebugging)
					log.debug("Remote file copy command: " + cmd);

				ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
				ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
				exitCode = _exec(new String(cmd), commandStdout, commandStderr);
				if (exitCode != 0) {
					throw new JobException(
							"Error at copying remote files into the job directory."
									+ "\nStdout:\n" + commandStdout
									+ "\nStderr:\n" + commandStderr);
				}
			}

		} catch (ExecException e) {
			throw new JobException(
					"Jobmanager.submit: Error at staging files to " + user
							+ "@" + host + "\n" + e, e);
		}

		// we have to enter the workdir before submitting the job
		commandStr = new String(cdCmd + commandStr);

		// submit the job finally
		ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
		ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();

		exitCode = _exec(commandStr, commandStdout, commandStderr);

		if (exitCode != 0) {
			throw new JobException("Error at job submission." + "\nStdout:\n"
					+ commandStdout + "\nStderr:\n" + commandStderr);
		}

		// parse the output for real jobID
		// This method can throw JobException as well!
		String jobID = jobSupport.parseSubmitOutput(commandStdout.toString(),
				commandStderr.toString());

		return jobID;
	} // end-of-submit

	/**
	 * Method formatFiles.
	 * @param dir String
	 * @param files Vector<File>
	 * @throws JobException
	 */
	private void formatFiles(String dir, Vector<File> files) 
	throws JobException {
		boolean isMac = System.getProperty("os.name").toLowerCase()
		.contains("mac");
		boolean isWindows = System.getProperty("os.name").toLowerCase()
		.contains("win");
		int exitCode;
		String cdCmd = "cd " + dir + "; ";
		
		if (files.size() > 0
				&& !this.host.equals("local")
				&& (isMac || isWindows)) {
			
			StringBuffer cmd = new StringBuffer(cdCmd);
			ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
			ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
			
			String filename = "*";
			if(files.size()==1){
				//if it is just one file use name instead of *
				filename = files.get(0).getName(); 
			}
			// use dos/mac 2unix if available
			if (isMac) {
				cmd.append("mac2unix ");
				cmd.append(filename);
			}
			if (isWindows) {
				cmd.append("dos2unix ");
				cmd.append(filename);
			}
			exitCode = _exec(new String(cmd), commandStdout, commandStderr);
	
			if (exitCode != 0) { // use sed -i if dos/mac 2unix not
									// available
				cmd = new StringBuffer(cdCmd);
				if (isMac) {
					cmd.append("sed -i 's/\\r/\\n/g' ");
					cmd.append(filename);
				}
				if (isWindows) {
					cmd.append("sed -i 's/\\r//g' ");
					cmd.append(filename);
				}
				exitCode = _exec(new String(cmd), commandStdout,
						commandStderr);
				// use tr as last resort
				if (exitCode != 0) {
					cmd = new StringBuffer(cdCmd);
					Iterator<File> it = files.iterator();
					while (it.hasNext()) {
						File aFile = it.next();
						String fileName = aFile.getName();
	
						cmd.append("cp " + fileName);
						cmd.append(" tmp" + fileName);
	
						if (isMac) {
							cmd.append("; tr '\\r' '\\n' <tmp" + fileName);
						}
						if (isWindows) {
							cmd.append("; tr -d '\\r' <tmp" + fileName);
						}
	
						cmd.append(" >" + fileName);
						cmd.append("; rm tmp" + fileName + "; ");
					}
					exitCode = _exec(new String(cmd), commandStdout,
							commandStderr);
					if (exitCode != 0) {
						throw new JobException(
								"Error at copying local files into the job directory."
										+ "\nStdout:\n" + commandStdout
										+ "\nStderr:\n" + commandStderr);
					}
				}
			}
		}
	}
	
	/**
	 * Check the status of the job
	 * 
	 * @param jobID String
	 * @return: JobStatusInfo struct if succeeded throws JobException on error,
	 *          or you call for a non-submitted job
	 * @throws JobException
	 */
	protected JobStatusInfo status(String jobID) throws JobException {
		return status(jobID,0);
	}
	
	/**
	 * Check the status of the job and tasks if numTasks>0
	 * 
	 * @return: JobStatusInfo struct if succeeded throws JobException on error,
	 *          or you call for a non-submitted job
	 * @param jobID String
	 * @param numTasks int
	 * @return JobStatusInfo
	 * @throws JobException
	 */
	protected JobStatusInfo status(String jobID, int numTasks) throws JobException {
		JobStatusInfo stat;
		if (jobID == null) {
			throw new JobException(
					"JobManager.status() called with null argument");
		}
		try{
			//1. Get command to execute
			String commandStr;
			if(numTasks > 0) {
				//execute jobstatus cmd and task status command
				commandStr = jobSupport.getTaskStatusCmd(jobID);
			}else{
				//execute job status command
				commandStr = jobSupport.getStatusCmd(jobID);
			}
	
			//2. Execute command
			int exitCode = 0;
			if (commandStr == null || commandStr.trim().equals("")) {
				throw new JobException(
						"Supporter class could not give back meaningful"
								+ "command to check the status of your job");
			}
	
			ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
			ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
	
			exitCode = _exec(commandStr, commandStdout, commandStderr);
			// Do not check the exitCode, as error can mean just: job not in queue
			// (e.g. PBS)
			// if (exitCode != 0)
			// throw new JobException("Error at checking job status. Stdout:\n" +
			// commandStdout +
			// "\nStderr:\n" + commandStderr);
	
			//3. Parse the output for status info
			// This method can throw JobException as well!
			
			System.out.println("JobManager.status numTasks: " + numTasks);
			if(numTasks > 0) {
				// parse both job status and individual task status. Returns TaskParallelJobStatusInfo
	            // This method can throw JobException as well!
				stat = jobSupport.parseTaskStatusOutput(
	            			jobID, numTasks, exitCode, commandStdout.toString(), commandStderr.toString());
			}else{
				stat = jobSupport.parseStatusOutput(jobID, exitCode, commandStdout
						.toString(), commandStderr.toString());
			}
		}catch(NotSupportedException e){
			throw new JobException(e.toString(), e);
		}
		return stat;
		
	}

	/**
	 * delete a job from queue
	 * 
	 * @return: JobStatusInfo struct if succeeded throws JobException on error,
	 *          or you call for a non-submitted job
	 * @param jobID String
	 * @return boolean
	 * @throws JobException
	 */
	protected boolean delete(String jobID) throws JobException {

		if (jobID == null) {
			throw new JobException(
					"JobManager.status() called with null argument");
		}

		String commandStr = jobSupport.getDeleteCmd(jobID);

		int exitCode = 0;

		if (commandStr == null || commandStr.trim().equals("")) {
			throw new JobException(
					"Supporter class could not give back meaningful"
							+ "command to remove your job");
		}

		ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
		ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();

		exitCode = _exec(commandStr, commandStdout, commandStderr);

		// Do not check the exitCode, as error can mean just: job not in queue
		// (e.g. PBS)
		// if (exitCode != 0)
		// throw new JobException("Error at checking job removel. Stdout:\n"
		// + commandStdout + "\nStderr:\n" + commandStderr);

		// parse the output for delete info
		// This method can throw JobException as well!
		boolean stat = jobSupport.parseDeleteOutput(jobID, exitCode,
				commandStdout.toString(), commandStderr.toString());

		return stat;

	}

	/**
	 * Execute a command either locally (Java Runtime) or remotely (SSH).
	 * 
	
	 * @param commandStr String
	 * @param commandStdout ByteArrayOutputStream
	 * @param commandStderr ByteArrayOutputStream
	 * @return exitCode of the command. * @throws JobException
	 */
	private int _exec(String commandStr, ByteArrayOutputStream commandStdout,
			ByteArrayOutputStream commandStderr) throws JobException {

		int exitCode = 0;
		try {
			if (isDebugging)
				log
						.debug("Execute on " + user + "@" + host + ": "
								+ commandStr);
			exitCode = execObject.executeCmd(commandStr, commandStdout,
					commandStderr);

		} catch (ExecException e) {
			throw new JobException("Jobmanager._exec: Error at execution on "
					+ user + "@" + host + " of command: " + commandStr + "\n"
					+ e, e);
		}

		return exitCode;
	}

} // end-of-class-JobManager

