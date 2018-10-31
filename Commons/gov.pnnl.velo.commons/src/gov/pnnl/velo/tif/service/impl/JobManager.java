package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.exception.JobConfigNotSetException;
import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.model.Fileset;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.JobHandler;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.service.CmsService;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.util.JobUtils;
import gov.pnnl.velo.util.VeloTifConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kepler.job.JobException;
import org.kepler.job.JobStatusCode;
import org.kepler.job.JobStatusInfo;
import org.kepler.job.JobSupport;
import org.kepler.ssh.AuthCancelException;
import org.kepler.ssh.AuthFailedException;
import org.kepler.ssh.ExecException;
import org.kepler.ssh.ExecInterface;
import org.kepler.ssh.LocalExec;

/**
 * A JobManager class submits and monitors a single Job. TODO: merge in changes from akuna TODO: should we implement the JobManager interface so different versions can be contributed via bean container?
 * 
 * @version $Revision: 1.0 $
 */

// TODO: COmpare with JobManager in Akuna client. Mainly for error handling
public class JobManager implements Runnable {

  private static final String FORK_SCRIPT = "jmgr-fork.sh";
  private JobConfig jobConfig;

  public JobConfig getJobConfig() {

    return jobConfig;
  }

  public void setJobConfig(JobConfig jobConfig) {

    this.jobConfig = jobConfig;
  }

  public JobHandler getJobHandler() {

    return jobHandler;
  }

  public void setJobHandler(JobHandler jobHandler) {

    this.jobHandler = jobHandler;
  }

  private JobHandler jobHandler;
  private JobStatusInfo jobStatus = null;
  private JobSupport jobSupport = null; // job support class
  private ExecInterface execObject = null; // class for remote/local
  private CmsService cmsService = TifServiceLocator.getCmsService();

  // execution

  private String jobscriptname = null;
  protected Log logger = LogFactory.getLog(JobManager.class);
  private Machine machine;
  private Code code;
  private HashMap<String, String> jobsMonitored;
  private String filesToFormat = "";

  // private String host; // remote host of jobmanager

  /**
   * @param jobConfig
   * @param jobHandler
   * 
   * 
   * @param exec
   *          ExecInterface
   * @param fileStagingHandler
   *          FileStagingHandler
   * @throws JobException
   *           * @throws ExecException
   */
  public JobManager(JobConfig jobConfig, JobHandler handler) throws JobException, ExecException {
    this.jobConfig = jobConfig;

    if (handler == null) {
      throw new RuntimeException("Job handler cannot be null!");
    }
    this.jobHandler = handler;

    machine = jobConfig.getMachine();
    code = jobConfig.getCode();
    if (code == null) {
      jobscriptname = "submit_job";
    }
    // only set the job config if it's not already set - no need to reinitialize twice
    if (jobHandler.getJobConfig() == null) {
      jobHandler.setJobConfig(jobConfig);
    }

    // make sure the job handler is initialized
    jobHandler.init();

    try {
      this.execObject = jobHandler.getExecObject();
    } catch (JobConfigNotSetException e) {
      // ignore as we have set JobConfig
    }
    // ServiceLocator.getScriptRegistry();
    jobscriptname = "submit_" + jobConfig.getCode().getIdAndVersion();
    
    logger.debug("Initializing JobManager");
    logger.debug("machine: " + jobConfig.getMachineId());
    logger.debug("code id: " + jobConfig.getCode().getIdAndVersion());
    logger.debug("remote job dir: " + jobConfig.getRemoteDir());
    initJobSupport();
  }

  /**
   * Method getExecObject.
   * 
   * @return ExecInterface
   */
  public ExecInterface getExecObject() {
    return execObject;
  }

  /**
   * Choose a JobSupport for execution <i>qsys</i> can be which is supported at that time: Condor <i>target</i> is either "localhost" or "user@host" is the machine where the jobmanager is running <binPath> is the full path to the jobmanager commands on that machine, or "" or null if they are in the default path if "'jobmanager'Support" class cannot be instantiated, a JobException is thrown
   * 
   * @throws JobException
   */
  protected void initJobSupport() throws JobException {

    String qsys = null;
    String binPath = "";

    if (machine.getScheduler() != null) {
      qsys = machine.getScheduler().getName();
      binPath = machine.getScheduler().getPath();
    }

    // if machine has no scheduler or
    // if the user doesn't want to
    // submit to queue (even if the remote machine might have one)
    // set scheduler to fork
    if (qsys == null || qsys.equalsIgnoreCase("Fork") || jobConfig.isDoNotQueue()) {
      qsys = "Fork";
      binPath = jobConfig.getRemoteDir();
    }

    // instantiate the supporter class
    String classname = "org.kepler.job.JobSupport" + qsys;
    try {
      jobSupport = (JobSupport) Class.forName(classname).newInstance();

    } catch (ClassNotFoundException cnf) {
      throw new JobException("Couldn't find class " + classname, cnf);

    } catch (InstantiationException ie) {
      throw new JobException("Couldn't instantiate an object of type " + classname, ie);

    } catch (IllegalAccessException ia) {
      throw new JobException("Couldn't access class " + classname, ia);
    }

    // initialize the supporter class
    if (qsys.equals("Fork")) {
      // Override in this case - we put it into the run dir
      jobSupport.init(jobConfig.getRemoteDir());
    } else {
      jobSupport.init(binPath);
    }

    // process the target
    if (execObject == null) {
      execObject = JobUtils.getDefaultExecObject(jobConfig);
    }
  }

  /**
   * Method submit job to compute node and monitor runs from the current local JVM
   * 
   * @param overwrite
   *          - set to true if the remote staging folder should be overwritten
   * @param options
   * @return JobId
   * @throws JobException
   */
  public String submit(boolean overwrite, String options) throws JobException {
    return submit(overwrite, options, true);
  }

  /**
   * Submit the job
   * 
   * @param overwrite
   *          - set to true if the remote staging folder should be overwritten
   * @param options
   * @param monitor
   *          boolean - set to true to start monitoring the job after submission. false otherwise
   * 
   * @return JobId or null if unsuccessful
   * @throws JobException
   */
  public String submit(boolean overwrite, String options, boolean monitor) throws JobException {

    int exitCode = 0;
    String commandStr;
    String jobID = null;
    logger.debug("Staging file before job launch..");

    // Job job = null;
    // stage the files before submission
    try {

      // ==== 1. authenticate to remote host ====
      // (this won't do anything if the connection is already open)
      execObject.openConnection();

      // ==== 1. Update status ====//
      jobHandler.jobBeforeSubmit();

      // ==== 2. Get job submission command based on the scheduler
      // used ====//
      commandStr = jobSupport.getSubmitCmd(jobscriptname, options);
      if (commandStr == null || commandStr.trim().equals("")) {
        throw new JobException("Supporter class could not give back meaningful command to submit your job");
      }
      logger.debug("submit Commandstr is " + commandStr);

      logger.debug("Staging file before job launch..");

      // ==== 3. prepare remote working directory, generate jobscript,
      // and stage files ===//
      prepareJobFiles(overwrite);
      stageFiles(overwrite);
      logger.debug("Finished staging files...");

      // ==== 3. Submit job from the remote working directory ====//
      commandStr = new String("cd " + jobConfig.getRemoteDir() + "; " + commandStr);
      logger.debug("Trying to launch job using remote command: " + commandStr);
      ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
      ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
      // finally!
      exitCode = _exec(commandStr, commandStdout, commandStderr);
      logger.debug("Ran commandstr exitcode=" + exitCode);
      if (exitCode != 0) {
        throw new JobException("Error at job submission. Stdout:\n" + commandStdout + "\nStderr:\n" + commandStderr);
      }

      // ==== 4. Parse output for real jobID ====//
      // IFF user has not specified a specific JobID via the
      // LaunchConfiguration
      // This method can throw JobException as well!
      jobID = jobConfig.getJobId();
      if (jobID == null || jobID.isEmpty()) {
        jobID = jobSupport.parseSubmitOutput(commandStdout.toString(), commandStderr.toString());
      }
      logger.debug("Job launched.  JobID=" + jobID);

      jobStatus = new JobStatusInfo();
      jobStatus.jobID = jobID;
      jobStatus.owner = jobConfig.getUserName();
      jobStatus.submissionTime = new Date().toString();
      jobStatus.statusCode = JobStatusCode.Wait;
      jobStatus.statusRecordTime = new Date();

      try {
        jobHandler.jobSubmitted(jobStatus);
      } catch (Exception ex) {
        ex.printStackTrace();
        throw new JobException(" Job submitted with job id: " + jobID + " but unable to record metadata " + ex.getMessage(), ex);
      }

      Thread monitorqueue = null;
      if (monitor) {
        monitorqueue = new Thread(this);
        monitorqueue.start(); // the monitoring
        logger.debug("Started thread");
        // Now sit and wait for job monitor thread to finish
        try {
          monitorqueue.join();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    } catch (AuthFailedException e) {
      // if the authentication failed, throw it back up
      throw e;

    } catch (AuthCancelException e) {
      // If user cancelled the authentication, don't log this as an error
      logger.debug("User auth cancel");
      // jobHandler.recordJobStatus(VeloTifConstants.STATUS_CANCELLED);

    } catch (Exception e) {
      String errMsg = "Job submission failed : " + e.getMessage();
      errMsg = errMsg.replaceAll("[\n\r]", " ");
      logger.error(errMsg, e);
      jobHandler.logMessage(errMsg);
      jobHandler.recordJobStatus(VeloTifConstants.STATUS_ERROR);
      throw new RuntimeException(e);
    }

    logger.debug("Returning jobID " + jobID);
    return jobID;
  } // end-of-submit

  /**
   * Create run script and store in localServerWorkingDirectory
   * 
   * @throws Exception
   */
  private void prepareJobFiles(boolean overwrite) throws Exception {

    if (JobUtils.isLocalhost(machine) && overwrite) {
      // if localhost then overwrite remoteDir as prepareLocalWorkingDir
      // could be making localWorkingDir same as remoteDir and copy files in it
      execObject.deleteFile(jobConfig.getRemoteDir(), true, false);
    }
    logger.debug("preparing localWorkingDir");

    File localWorkingDir = jobHandler.prepareLocalWorkingDirectory();
    jobConfig.setLocalWorkingDir(localWorkingDir);

    // Add the mandatory files
    List<Fileset> localInputs = code.getJobLaunching().getLocalInputs();
    if (execObject instanceof LocalExec || machine.getScheduler() == null || machine.getScheduler().getName().equals("Fork") || jobConfig.isDoNotQueue()) {

      logger.debug("Writing fork script");
      File forkscriptFile = new File(localWorkingDir, FORK_SCRIPT);
      JobUtils.writeForkMgr(forkscriptFile.getAbsolutePath());
      filesToFormat = filesToFormat + " " + FORK_SCRIPT;
      if (!localWorkingDir.getAbsolutePath().equals(jobConfig.getRemoteDir())) {
        Fileset f = new Fileset();
        f.setDir(localWorkingDir.getAbsolutePath());
        f.setIncludes(FORK_SCRIPT);
        localInputs.add(f);
        logger.debug("adding fileset fork to localInputs list" + f);
      }

    }
    File jobscriptFile = new File(localWorkingDir, jobscriptname);
    JobUtils.writeJobScript(jobConfig, jobscriptFile.getAbsolutePath());
    filesToFormat = filesToFormat + " " + jobscriptname;
    if (!localWorkingDir.getAbsolutePath().equals(jobConfig.getRemoteDir())) {
      Fileset f = new Fileset();
      f.setDir(localWorkingDir.getAbsolutePath());
      f.setIncludes(jobscriptname);
      localInputs.add(f);
      logger.debug("adding fileset submit to localInputs list" + f);
    }
  }

  /**
   * Write the files to the remote server
   * 
   * @param overwrite
   * 
   * @throws Exception
   */
  private void stageFiles(boolean overwrite) throws Exception {

    // ==== 1. Create remote directory ====//
    if (!JobUtils.isLocalhost(machine) && overwrite) {
      // if localhost then overwrite would have happened earlier in prepareFiles
      // delete the remote working directory if asked by the submitter
      execObject.deleteFile(jobConfig.getRemoteDir(), true, false);
    }
    // This line wasn't creating the correct folder when getRemoteDir returned a foldername that had spaces in it.
    // I tried wrapping in quotes "\""+jobConfig.getRemoteDir()+"\"" but it still didn't work.
    execObject.createDir(jobConfig.getRemoteDir(), true);
    logger.debug("Created job working dir");

    // stage file in Velo server to compute server
    // call this before stageLocalInputs as staging veloserver inputs
    // could happen indirectly through stageLocalInputs
    // if job server is not directly accessible from VeloServer
    // we can bring file from velo server to local and then ship files
    // from local to remote job server
    jobHandler.stageVeloServerInputs(code.getJobLaunching().getVeloServerInputs(), code.getJobLaunching().getLocalInputs());

    // stage files in local filesystem to compute server
    jobHandler.stageLocalInputs(code.getJobLaunching().getLocalInputs());
    
    execObject.copyTo(jobConfig.getDynamicLocalInputFiles(), jobConfig.getRemoteDir(), true);
    
    logger.debug("Staged local files");

    // finally run dos2unix on all local files, constant files and registry
    // files
    // remote files would anyway be in the same format
    // dos2unix(launchConfig.getJobDirectory());

    // TODO: make filesToFormat variable accessible to other classes so that they can
    // add to the list of files that need to be formatted. We could add it as jobconfig parmeter
    // For now only
    // files generated by job manager locally - fork script, submit script - are added
    formatFiles(jobConfig.getRemoteDir());

    // make job script and scheduler(in case of Fork) executable
    makeExecutable(jobConfig.getRemoteDir() + "/" + jobscriptname);
    if (execObject instanceof LocalExec || machine.getScheduler() == null || machine.getScheduler().getName().equals("Fork")) {
      makeExecutable(jobConfig.getRemoteDir() + "/" + FORK_SCRIPT);
    }

    // ==== 3. Copy any remote files to job's working dir ====//
    // Use case: executables/scripts that should be in the same directory
    // as the input files.
    logger.debug("Staging remote inputs.");
    jobHandler.stageRemoteInputs(code.getJobLaunching().getRemoteInputs());

  }

  /**
   * delete a job from queue
   * 
   * 
   * @param jobID
   *          String
   * @return boolean
   * @throws JobException
   */
  public boolean delete(String jobID) throws JobException {
    boolean stat = false;
    try {
      if (jobID == null) {
        throw new JobException("JobManager.status() called with null argument");
      }

      String commandStr = jobSupport.getDeleteCmd(jobID);

      int exitCode = 0;

      if (commandStr == null || commandStr.trim().equals("")) {
        throw new JobException("Supporter class could not give back meaningful" + "command to remove your job");
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
      stat = jobSupport.parseDeleteOutput(jobID, exitCode, commandStdout.toString(), commandStderr.toString());

      if (stat) {
        jobHandler.jobTerminated(new JobStatusInfo());

      }

    } catch (AuthFailedException e) {
      // if the authentication failed, throw it back up
      throw e;

    } catch (Throwable e) {
      // Don't fail on a terminated job. Update just the error message
      String msg = "Warning: Failed to run post processing on terminated job.";
      jobHandler.logMessage(msg);
    }
    return stat;

  }

  public boolean reconnect(String jobid, boolean monitor) {
    try {
      // authenticate first
      execObject.openConnection();

      // ==== 1. Update status ====//
      jobHandler.recordJobStatus(VeloTifConstants.STATUS_RECONNECT);
      jobStatus = new JobStatusInfo();
      jobStatus.jobID = jobid;
      jobStatus.owner = jobConfig.getUserName();
      jobStatus.statusCode = JobStatusCode.Wait;

      // make sure we have a local working dir to stage to
      File localWorkingDir = jobHandler.prepareLocalWorkingDirectory();
      jobConfig.setLocalWorkingDir(localWorkingDir);

      // make sure we set the status property to the correct status
      JobStatusInfo currentstat = jobHandler.queryJobStatus(jobid, jobSupport);
      String status = VeloTifConstants.STATUS_WAIT;
      if (currentstat.statusCode.equals(JobStatusCode.Wait)) {
        status = VeloTifConstants.STATUS_WAIT;
      } else if (currentstat.statusCode.equals(JobStatusCode.NotInQueue)) {
        status = VeloTifConstants.STATUS_POSTPROCESS;
      } else if (currentstat.statusCode.equals(JobStatusCode.Running)) {
        status = VeloTifConstants.STATUS_START;
      } else if (currentstat.statusCode.equals(JobStatusCode.Error)) {
        status = VeloTifConstants.STATUS_POSTPROCESS;
      }else if (currentstat.statusCode.equals(JobStatusCode.Success)) {
          status = VeloTifConstants.STATUS_POSTPROCESS;
      }else if (currentstat.statusCode.equals(JobStatusCode.Failed)) {
          status = VeloTifConstants.STATUS_POSTPROCESS;
      }
      jobHandler.recordJobStatus(status);

      Thread monitorqueue = null;
      if (monitor) {
        monitorqueue = new Thread(this);
        monitorqueue.start(); // the monitoring
        logger.debug("Started thread");
        // Now sit and wait for job monitor thread to finish
        try {
          monitorqueue.join();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    } catch (AuthFailedException e) {
      // if the authentication failed, throw it back up
      throw e;

    } catch (AuthCancelException e) {
      // if authentication is cancelled terminate and don't do anything
      return false;

    } catch (Throwable e) {
      String errMsg = "Job reconnect failed" + e.getMessage();
      errMsg = errMsg.replaceAll("[\n\r]", " ");
      logger.error(errMsg, e);
      jobHandler.logMessage(errMsg);
      jobHandler.recordJobStatus(VeloTifConstants.STATUS_ERROR);
      throw new RuntimeException(e);
    }
    return true;
  }

  /**
   * Method makeExecutable.
   * 
   * @param path
   *          String
   * @throws JobException
   */
  private void makeExecutable(String path) throws JobException {
    ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
    ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
    String cmd = "chmod a+x " + path;
    int exitCode = _exec(cmd, commandStdout, commandStderr);
    logger.debug("******Made " + path + " executable. Command used - " + cmd);
    if (exitCode != 0) {
      throw new JobException("Error at job submission. Stdout:\n" + commandStdout + "\nStderr:\n" + commandStderr);
    }
  }

  /**
   * Method run.
   * 
   * @see java.lang.Runnable#run()
   */
  public void run() {

    // File sfile = new File(launchConfig.getRemoteStagingAreaPath() +
    // File.separator + ".status");
    logger.debug("In job monitoring thread. Current status -" + jobStatus.statusCode);
    while (jobStatus.statusCode != JobStatusCode.NotInQueue 
    		&& jobStatus.statusCode != JobStatusCode.Error
    		&& jobStatus.statusCode != JobStatusCode.Success
    		&& jobStatus.statusCode != JobStatusCode.Failed) {
      try {

        logger.debug("In job monitoring thread. Checking status -" + jobStatus.statusCode);
        // check for processess
        JobStatusInfo stat = jobHandler.queryJobStatus(jobStatus.jobID, jobSupport);
        logger.debug("In job monitoring thread. Current status -" + stat.statusCode);
        if (stat.statusCode == JobStatusCode.NotInQueue) {
          // nothing to monitor anymore. break
          break;
        }

        // check if status has changed from last observed status
        if (jobStatus.statusCode != stat.statusCode) {
          jobStatus.statusCode = stat.statusCode;
          // logger.debug("Status" + stat.statusCode);
          if (stat.statusCode == JobStatusCode.Running) {
            if (jobHandler != null) {
              try {
                System.out.println(" thread calling handler's jobStarted method");
                jobHandler.jobStarted(stat);
                System.out.println(" thread completed handler's jobStarted method");
              } catch (Exception ex) {
                String errmsg = "Error in job handler(" + jobHandler.getClass() + ") jobStarted method " + ex.getMessage();
                jobHandler.logMessage(errmsg);
              }
            }
          }
        } else if (stat.statusCode == JobStatusCode.Running) {
          // else if the status hasn't changed and it is "Running"
          // call the handler's running
          // method - this will be called many times while the job is
          // running and is where
          // handlers will check for new files to download, parse,
          // etc.
          if (jobHandler != null) {
            try {
              System.out.println(" thread calling handler's jobRunning method");
              jobHandler.jobRunning(stat);
              System.out.println(" thread completed handler's jobRunning method");
            } catch (Exception ex) {
              String errmsg = "Error in job handler(" + jobHandler.getClass() + ") jobRunning method " + ex.getMessage();
              jobHandler.logMessage(errmsg);
            }
          }
        }

        try {
          Thread.sleep(jobConfig.getPollingInterval() * 1000);
        } catch (InterruptedException ex) {
          // do nothing
        }

      } catch (Exception jex) {
        String errmsg = "Exception Monitoring Job status" + jex.getMessage();
        jobHandler.logMessage(errmsg);
      }

    }

    // Custom handling of job completion
    if (jobHandler != null) {
      try {
        logger.debug(" thread calling handler's jobComplete method");

        jobHandler.jobComplete(jobStatus);
        logger.debug(" thread completed handler's jobComplete method");

      } catch (Exception ex) {

        logger.debug("Job Completed but post processing failed : " + ex.getMessage());
        String errmsg = "Job Completed but post processing failed : " + ex.getMessage();
        jobHandler.logMessage(errmsg);
      }
    } // end job complete post process
  }

  /**
   * Method formatFiles. Format the list of files specified by filesToFormat variable. 
   * Eariler this was not formating if job is local but now that JobManager could be running
   * on the Velo server, this always formats the files in filesToFormat variable. 
   * First tries a dos2unix and if it fails tries a sed command. 
   * 
   * Note - Mac formats are ignored as all recent mac operating systems ( any OS X) uses linux 
   * format. So no explicit mac2unix is done if dos2unix command fails. dos2unix can convert
   * both dos style  end of line (\r\n) and older mac style end of line (\r). 
   * sed command only removes any existing \r and doesn't try to replace it with \n
   * @param dir
   *          String
   * @param filesToConvert
   * @param files
   *          Vector<File>
   * @throws JobException
   */
  private void formatFiles(String dir) throws JobException {

    int exitCode;
    String cdCmd = "cd " + dir + "; ";
    logger.debug("In format files. cdCmd " + cdCmd);

    StringBuffer cmd = new StringBuffer(cdCmd);
    ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
    ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();

    cmd.append("dos2unix ");
    cmd.append(filesToFormat);
    System.out.println("In format files. try 1st format change command " + cmd);
    exitCode = _exec(new String(cmd), commandStdout, commandStderr);

    if (exitCode != 0) { // use sed -i if dos2unix is not available

      cmd = new StringBuffer(cdCmd);
      //only handles dos format(\r\n) and removes \r
      //does not handle age old mac format end of line (\r)
      cmd.append("sed -i \"\" 's/\\x0D//g' ");
      cmd.append(filesToFormat);

      System.out.println("In format files. try 2st format change command " + cmd);
      exitCode = _exec(new String(cmd), commandStdout, commandStderr);

    }

  }

  /**
   * Execute a command either locally (Java Runtime) or remotely (SSH).
   * 
   * 
   * @param commandStr
   *          String
   * @param commandStdout
   *          ByteArrayOutputStream
   * @param commandStderr
   *          ByteArrayOutputStream
   * @return exitCode of the command. * @throws JobException
   */
  private int _exec(String commandStr, ByteArrayOutputStream commandStdout, ByteArrayOutputStream commandStderr) throws JobException {

    int exitCode = 0;
    try {

      // if (isDebugging) log.debug("Execute on " + user + "@" + host +
      // ": " + commandStr);
      exitCode = execObject.executeCmd(commandStr, commandStdout, commandStderr);

    } catch (ExecException e) {
      String host = jobConfig.getMachineId();
      String user = jobConfig.getUserName();
      throw new JobException("Jobmanager._exec: Error at execution on Machine id:" + host + " as user:" + user + "  command: " + commandStr + "\n" + e, e);
    }

    return exitCode;
  }

  public void setMonitor(HashMap<String, String> jobsMonitored) {

    this.jobsMonitored = jobsMonitored;

  }

  public String getJobId() {
    if (jobStatus == null)
      return null;
    else
      return jobStatus.jobID;
  }

  public void setJobStatus(JobStatusInfo jobStatus) {

    this.jobStatus = jobStatus;

  }

  public JobStatusInfo getJobStatus() {

    return jobStatus;
  }

  public JobSupport getJobSupport() {
    return jobSupport;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JobManager)) {
      return false;
    }

    // TODO: for now we assume only one job can run per context path
    JobManager jm = (JobManager) obj;
    String myContext = jobConfig.getContextPath();
    String jmContext = jm.jobConfig.getContextPath();

    return myContext.equals(jmContext);
  }

}
