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
 * '$Date: 2011-01-03 18:31:43 -0800 (Mon, 03 Jan 2011) $' 
 * '$Revision: 26609 $'
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

import java.io.File;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class Job to describe a standalone job. It is assumed that a job will be
 * submitted only once and then forgotten. Do not resubmit the same Job object
 * but create another one.
 */

public class Job {

	public JobStatusInfo status;

	private String myID; // the key of this object in the hash table
	// becomes set in setJobID() called from jobFactory;
	private String workdirPath; // path of (remote) working directory for
	// job submission and files

	private File localWorkdir; // local working directory for the job (output)
	private String localWorkdirPath; // path of local working directory for
	// output files

	// private String submitFile; // the submitFile of the job;
	// It is to be created or given by the user.
	// private boolean submitFileIsLocal; // is the submitFile created here, or
	// is it on the remote site? default=true (i.e.local);

	private JobManager jmgr; // the jobmanager which handles this job

	private String arguments; // argument string for the job
	
	private int numTasks = 0; //number of tasks

	private static final Log log = LogFactory.getLog(Job.class.getName());
	private static final boolean isDebugging = log.isDebugEnabled();

	public class JobFile {
		String filename;
		boolean isLocal;

		/**
		 * Constructor for JobFile.
		 * @param filename String
		 * @param isLocal boolean
		 */
		JobFile(String filename, boolean isLocal) {
			this.filename = filename;
			this.isLocal = isLocal;
		}
	};

	public class JobFiles {
		JobFile executable; // the executable file, local or remote
		JobFile submitfile; // the submitfile, local or remote, given by user or
							// created by supporter class
		Vector inputfiles; // of type JobFile, input files to be declared in
							// submit file
		Vector otherinputfiles; // of type String, other files to be staged
								// before submission
		JobFile binfile; // scheduler script to be staged to binpath
						// if bin path is not given stage to workingdir
		Vector outputfiles; // of type String

	};

	public JobFiles jobfiles;

	/**
	 * Constructor is called from JobFactory. jobID is a unique id for the jobs.
	 * Currently only for the current applications. Later it will be an uuid.
	 * @param jobID String
	 */
	protected Job(String jobID) {
		status = new JobStatusInfo();
		// submitFileIsLocal = true;
		jobfiles = new JobFiles();
		jobfiles.inputfiles = new Vector();
		jobfiles.otherinputfiles = new Vector();
		jobfiles.outputfiles = new Vector();
		myID = jobID;
		status.statusCode = JobStatusCode.NotInQueue;
	}

	/**
	 * Get the key of the Job object in the hash table. Used by JobManager to
	 * create unique files based on Job's key Do not mix it with status.jobID,
	 * which is the real job id in the remote queue.
	 * @return String
	 */
	public String getJobID() {
		return myID;
	}

	/**
	 * Method setNumTasks.
	 * @param nt int
	 */
	public void setNumTasks(int nt) {
		numTasks = nt;
	}

	/**
	 * Method getNumTasks.
	 * @return int
	 */
	public int getNumTasks() {
		return numTasks;
	}


	/**
	 * Set the executable for the job. <i>executablePath</i> is the path to the
	 * executable <i>isLocal</i> true means a local file, false means the
	 * executable is on the remote site <i>arguments</i> are the arguments to
	 * be passed to the job. This will work only if the submission file is
	 * created by the JobManager and not provided as is in the setSubmitFile()
	 * method.
	 * 
	
	 * @param executablePath String
	 * @param isLocal boolean
	 * @param arguments String
	 * @return true if succeeds. Throws JobException if isLocal is true but
	 *         executable is not found locally. * @throws JobException
	 */
	public boolean setExecutable(String executablePath, boolean isLocal,
			String arguments) throws JobException {

		if (isLocal) {
			File f = new File(executablePath);
			if (!f.isFile()) {
				throw new JobException("Local executable file "
						+ executablePath + " is actually not a file."
						+ " Did you want to declare it remote file?");
			}
		}

		jobfiles.executable = new JobFile(executablePath, isLocal);
		this.arguments = arguments;
		return true;
	}

	/**
	 * Set an input file for job. It can be either locally present or remotely.
	 * 
	
	 * @param path String
	 * @param isLocal boolean
	 * @return true at success, but throws JobException if file is assumed to be
	 *         a local file and it does not exist. * @throws JobException
	 */
	public boolean setInputFile(String path, boolean isLocal)
			throws JobException {

		if (isLocal) {
			File f = new File(path);
			if (!f.isFile()) {
				throw new JobException("Local input file " + path
						+ " is actually not a file."
						+ " Did you want to declare it remote file?");
			}
		}
		if (path == null || path.trim().length() == 0)
			throw new JobException(
					"Your parameter as input file string is empty");

		jobfiles.inputfiles.add(new JobFile(path, isLocal));
		return true;
	}

	/**
	 * Set file to be staged to bin path. It can be either locally present or
	 * remotely. It is currently used for staging default fork script
	 * jmgr-fork.sh from job/resources
	 * 
	 * @return true at success, but throws JobException if file is assumed to be
	 *         a local file and it does not exist.
	 */
	public void setBinFile(String binFile, boolean isLocal) throws JobException {

		if (isLocal) {
			File f = new File(binFile);
			if (!f.isFile()) {
				throw new JobException("Local file " + binFile
						+ " is actually not a file."
						+ " Did you want to declare it remote file?");
			}
		}
		jobfiles.binfile = new JobFile(binFile, isLocal);
	}

	/**
	 * Set an "other" input file for job. It will be staged to the remote site
	 * into the remote working dir but it will not be used in creating the
	 * submission file.
	 * 
	
	 * @param path String
	 * @return true at success, but throws JobException if file is assumed to be
	 *         a local file and it does not exist. * @throws JobException
	 */
	public boolean setOtherInputFile(String path) throws JobException {

		File f = new File(path);
		if (!f.isFile()) {
			throw new JobException("'Other' input file " + path
					+ " is actually not a file.");
		}
		if (path == null || path.trim().length() == 0)
			throw new JobException(
					"Your parameter as input file string is empty");

		jobfiles.otherinputfiles.add(path);
		return true;
	}

	/**
	 * Set an output file for job. It should be the name of output file which
	 * will be produced by the job.
	 * @param path String
	 * @throws JobException
	 */
	public void setOutputFile(String path) throws JobException {

		if (path == null || path.trim().length() == 0)
			throw new JobException(
					"Your parameter as output file string is empty");

		jobfiles.outputfiles.add(path);
	}

	/**
	 * Set and create the local working directory. For local execution, it will
	 * be the same as the working directory, so it is not necessary to set. For
	 * remote execution, the result of the remote job will be brought to this
	 * directory (not implemented!).
	 * 
	
	 * @param path String
	 * @return true if creation succeeded, throws JobException otherwise * @throws JobException
	 */
	public boolean setLocalWorkdir(String path) throws JobException {

		File dir = new File(path);
		if (!dir.exists()) {
			if (!dir.mkdirs())
				throw new JobException(
						"Path "
								+ path
								+ " cannot be created to be the local working directory for the job");
		} else if (!dir.isDirectory()) {
			throw new JobException("Path " + path
					+ " exists but is not a directory");
		}

		localWorkdirPath = path;
		localWorkdir = dir;
		return true;
	}

	/**
	 * Set the (remote) working directory. The directory given as path must
	 * exist already. The actual working directory of a job will be this path
	 * appended with the job's myID. That directory will be created
	 * automatically and the job submission will be issued from this directory.
	 * I.e. relative input/output filenames in the job submission refer files
	 * under this directory. To get back the actual working directory name, use
	 * getWorkdirPath().
	 * 
	
	 * @param path String
	 * @return if path==null, it returns false, otherwise true */
	public boolean setWorkdir(String path) {
		return setWorkdir(path, true);
	}

	/**
	 * Set the (remote) working directory. If "createUniqueSubdir" is set to
	 * false, then the given dir is used as working dir. If "createUniqueSubdir"
	 * is set to true, the directory given as path must exist already and tThe
	 * actual working directory will be this path appended with the job's myID.
	 * That directory will be created automatically and the job submission will
	 * be issued from this directory. I.e. relative input/output filenames in
	 * the job submission refer files under this directory. To get back the
	 * actual working directory name, use getWorkdirPath().
	 * 
	
	 * @param path String
	 * @param createUniqueSubdir boolean
	 * @return if path==null, it returns false, otherwise true */
	public boolean setWorkdir(String path, boolean createUniqueSubdir) {
		String sep;
		if (path == null)
			return false;
		if (createUniqueSubdir) {
			if (path.endsWith("/"))
				sep = "";
			else
				sep = "/";
			workdirPath = path + sep + myID;
		} else {
			workdirPath = path;
		}
		return true;
	}

	/**
	 * Method getLocalWorkdirPath.
	 * @return String
	 */
	public String getLocalWorkdirPath() {
		return localWorkdirPath;
	}

	/**
	 * Method getWorkdirPath.
	 * @return String
	 */
	public String getWorkdirPath() {
		return workdirPath;
	}

	/**
	 * Give a predefined submit file for the job. This is the most general
	 * possibility for advanced users, to submit jobs to a specific job manager.
	 * 
	 * For basic users, do not use this method. Just define the executable,
	 * input and output files for the Job and the JobManager will create an
	 * appropriate submit file for the selected job manager. !!!That is not
	 * implemented in the support classes, so you must use this method always!!!
	 * 
	
	
	 * @param submitFile String
	 * @param isItLocal boolean
	 * @throws JobException
	 */
	public void setSubmitFile(String submitFile, boolean isItLocal)
			throws JobException {

		if (isItLocal) {
			File f = new File(submitFile);
			if (!f.isFile()) {
				throw new JobException("Local file " + submitFile
						+ " is actually not a file."
						+ " Did you want to declare it remote file?");
			}
		}
		jobfiles.submitfile = new JobFile(submitFile, isItLocal);
	}

	/**
	 * Submit a job, called from Job.submit(); boolean <i>overwrite</i>
	 * indicates whether old files that exist on the same directory should be
	 * removed before staging new files. As long jobIDs are not really unique,
	 * this is worth to be true. <i>options</i> can be a special options string
	 * for the actual jobmanager.
	 * 
	 * @return: jobID as String if submission is successful (it is submitted and
	 *          real jobID of the submitted job can be retrieved) Real jobID can
	 *          also be found in job.status.jobID, but you probably do not need
	 *          it except for logging. on error throws JobException
	 */
	public String submit(JobManager jobmanager, boolean overwrite,
			String options) throws JobException {

		if (jobmanager == null) {
			throw new JobException(
					"Valid Jobmanager is needed at job submission");
		}
		this.jmgr = jobmanager;

		// set defaults if things are unset or throw exception
		checkDefaults();

		status.jobID = jmgr.submit(this, overwrite, options);

		// The job has been successfully submitted (or an exception has been
		// thrown)
		status.statusCode = JobStatusCode.Wait;

		return status.jobID;
	}

	/**
	 * Reconnect to a executing job. As long jobIDs are not really unique, this
	 * is worth to be true. <i>options</i> can be a special options string for
	 * the actual jobmanager.
	 * 
	 * @return: jobID as String if submission is successful (it is submitted and
	 *          real jobID of the submitted job can be retrieved) Real jobID can
	 *          also be found in job.status.jobID, but you probably do not need
	 *          it except for logging. on error throws JobException
	 */
	public boolean reconnect(JobManager jobmanager) throws JobException {

		if (jobmanager == null) {
			throw new JobException(
					"Valid Jobmanager is needed at job reconnect");
		}
		this.jmgr = jobmanager;

		// set defaults if things are unset or throw exception
		checkDefaults();
		status.statusCode = JobStatusCode.Wait;
		status(); // successful query if not throws exception
		return true;
	}

	/**
	 * If something important is not set, set default here or throw an exception
	 * @throws JobException
	 */
	private void checkDefaults() throws JobException {

		if (localWorkdirPath == null) {
			String home = System.getProperty("user.home");
			if ( System.getProperty("os.name").toLowerCase().indexOf("win") >= 0 ) {
				home = System.getenv().get("HOMEPATH");
			}
			setLocalWorkdir(new String(
					home + File.separator + ".hpcc" + File.separator + myID));
		}
		if (workdirPath == null)
			setWorkdir(new String(".hpcc"));

		// if ( jobfiles.executable == null )
		// throw new JobException("Job has not executable file specified.");

	}

	/**
	 * Check the status of the job
	 * 
	 * @return: true if succeeded The JobStatusInfo data struct of Job is
	 *          altered: (job.status) throws JobException on error, or you call
	 *          for a non-submitted job
	 */
	public boolean status() throws JobException {

		if (jmgr == null) {
			throw new JobException(
					"Valid Jobmanager is needed before checking job status");
		}

		if (status.jobID == null) {
			throw new JobException("The job is not submitted. "
					+ "Or at least, it has no real jobID, so we lost it.");
		}

		if (status.statusCode == JobStatusCode.Error) // no need to check it
			// again
			return true;
		
		JobStatusInfo stat = jmgr.status(status.jobID,numTasks);
		status.statusCode = stat.statusCode;
		if(numTasks > 0) {
			((TaskParallelJobStatusInfo)status).taskStatusCodes = 
				((TaskParallelJobStatusInfo)stat).taskStatusCodes;
		}

		return true;
	}

	/**
	 * Remove job from the queue (either running or waiting)
	 * 
	 * @return: true if succeeded, false is not throws JobException on error, or
	 *          you call for a non-submitted job
	 */
	public boolean deleteFromQueue() throws JobException {

		if (jmgr == null) {
			throw new JobException(
					"Valid Jobmanager is needed before removing the job");
		}

		if (status.jobID == null) {
			throw new JobException("The job is not submitted. "
					+ "Or at least, it has no real jobID, so we lost it.");
		}

		boolean stat = jmgr.delete(status.jobID);
		return stat;
	}

	/*
	 * ---------------- Protected methods for JobManager and JobSupport classes
	 * ------------
	 */
	/**
	 * Method getSubmitFile.
	 * @return String
	 */
	public String getSubmitFile() {
		if (jobfiles.submitfile != null)
			return jobfiles.submitfile.filename;
		return null;
	}

	/**
	 * Method isSubmitFileLocal.
	 * @return boolean
	 */
	protected boolean isSubmitFileLocal() {
		if (jobfiles.submitfile != null)
			return jobfiles.submitfile.isLocal;
		return false;
	}

	/**
	 * Method getBinFile.
	 * @return String
	 */
	protected String getBinFile() {
		if (jobfiles.binfile != null)
			return jobfiles.binfile.filename;
		return null;
	}

	/**
	 * Method isBinFileLocal.
	 * @return boolean
	 */
	protected boolean isBinFileLocal() {
		if (jobfiles.binfile != null)
			return jobfiles.binfile.isLocal;
		return false;
	}

	/**
	 * Return vector of local files that should be staged to the remote site for
	 * a job. Vector elements are type of File!
	 * 
	 * @return: Vector of File objects. On error throws JobException
	 */
	protected Vector getLocalFiles() {

		Vector files = new Vector();

		if (jobfiles.executable != null && jobfiles.executable.isLocal)
			files.add(new File(jobfiles.executable.filename));

		int i;
		int inputs = jobfiles.inputfiles.size();
		for (i = 0; i < inputs; i++) {
			Job.JobFile f = (Job.JobFile) (jobfiles.inputfiles.get(i));
			if (f.isLocal)
				files.add(new File(f.filename));
		}

		int others = jobfiles.otherinputfiles.size();
		for (i = 0; i < others; i++) {
			files.add(new File((String) jobfiles.otherinputfiles.get(i)));
		}

		if (jobfiles.submitfile != null && jobfiles.submitfile.isLocal)
			files.add(new File(jobfiles.submitfile.filename));

		return files;
	}

	/**
	 * Return vector of remote files that should be copied into to remote job
	 * directory before submission. Vector elements are type of Strings!
	 * 
	 * @return: Vector of File objects. On error throws JobException
	 */
	protected Vector getRemoteFiles() {

		Vector files = new Vector();

		if (jobfiles.executable != null && !jobfiles.executable.isLocal)
			files.add(jobfiles.executable.filename);

		int i;
		int inputs = jobfiles.inputfiles.size();
		for (i = 0; i < inputs; i++) {
			Job.JobFile f = (Job.JobFile) (jobfiles.inputfiles.get(i));
			if (!f.isLocal)
				files.add(f.filename);
		}

		if (jobfiles.submitfile != null && !jobfiles.submitfile.isLocal)
			files.add(jobfiles.submitfile.filename);

		return files;
	}

} // end-of-class-Job

