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
package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.model.Credentials;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.JobHandler;
import gov.pnnl.velo.tif.model.JobLaunchParameters;
import gov.pnnl.velo.tif.service.JobLaunchService;
import gov.pnnl.velo.tif.service.JobLaunchWebServices;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.util.VeloTifConstants;

import java.util.List;

import org.apache.log4j.Logger;
import org.kepler.job.JobStatusInfo;
import org.kepler.ssh.AuthCancelException;
import org.kepler.ssh.AuthFailedException;
import org.kepler.ssh.ExecInterface;
import org.kepler.ssh.ReAuthNotAllowedException;
import org.kepler.ssh.SshExec;
import org.kepler.ssh.VeloUserInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Server-side job launching and monitoring.
 */
public class JobLaunchServiceVelo implements JobLaunchService, ApplicationContextAware {
  
  
  // so we can look up job handlers from the bean container
  protected ApplicationContext applicationContext; 
  private static Logger logger = Logger.getLogger(JobLaunchServiceVelo.class);
  
  private JobMonitoringThread jobMonitorThread; // for local job monitoring
  private JobLaunchWebServices webServiceAPI; // for velo server-side job monitoring
  
  public JobLaunchServiceVelo() { 
  }
  
  /**
   * @param webServiceAPI the webServiceAPI to set
   */
  public void setWebServiceAPI(JobLaunchWebServices webServiceAPI) {
    this.webServiceAPI = webServiceAPI;
  }

  /**
   * Method setApplicationContext.
   * @param applicationContext ApplicationContext
   * @see org.springframework.context.ApplicationContextAware#setApplicationContext(ApplicationContext)
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
  
  /** 
   * Make sure you set this as the init method from your spring bean configuration
   * @see gov.pnnl.velo.tif.service.JobLaunchService#init()
   */
  @Override
  public void init() {
    jobMonitorThread.start();    
  }
  
  /**
   * Should be injected by spring
   * @param jobMonitorThread
   */
  public void setJobMonitorThread(JobMonitoringThread jobMonitorThread) {
    this.jobMonitorThread = jobMonitorThread;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.JobLaunchService#kill(java.lang.String)
   */
  @Override
  public boolean kill(JobConfig config, Credentials credentials) throws Exception {
    logger.debug("Killing job: " + config.getJobId());
    
    // Need to decide if we will send kill command the server or to the client
    if(isLocalMonitoring(config)) {
      return killJobLocalMonitoring(config, credentials);

    } else {
      return killJobVeloServerMonitoring(config);
    }
  }
  
  private boolean killJobLocalMonitoring(JobConfig config, Credentials credentials) throws Exception {
    
    String jobId = config.getJobId();
    boolean stat = false;
    try {

      // Look up the job from the cache
      JobManager jobManager = jobMonitorThread.jobIdToJobManager.get(jobId);
      if(jobManager == null) {
    	logger.debug("Job Manager was null. Creating new Job Manager for job");
        // we have to make a new job manager so we can kill the job
        JobHandler jobHandler = getJobHandler(config);
        setCredentials(jobHandler.getExecObject(), credentials);

        // Use the run dir from saved properties, not the JobConfig!
        String remoteDir = TifServiceLocator.getCmsService().getProperty(config.getContextPath(), VeloTifConstants.JOB_RUNDIR);
        config.setRemoteDir(remoteDir);

        // submit job
        jobManager = new JobManager(config, jobHandler);

      }
      // Kill the job
      stat = jobManager.delete(jobId);
      jobMonitorThread.removeJobFromQueue(jobId, null);

    } catch (AuthFailedException e) {
      jobMonitorThread.removeJobFromQueue(jobId, null); // don't log exception
      throw e; // now throw it back so callers get it

    } catch (Exception e) {
      jobMonitorThread.removeJobFromQueue(jobId, e);
      throw e;
    } 
    return stat;


  }

  private boolean killJobVeloServerMonitoring(JobConfig config) {    
    Credentials credentials = null;
    boolean complete = false;
    AuthFailedException authFailed = null;
    boolean stat = false;
    while(!complete) {
      try {
        credentials = webServiceAPI.getCredentialsToComputeServer(config, authFailed);
      } catch (AuthCancelException e) {
        break;
      }
      JobLaunchParameters jobLaunchParams = new JobLaunchParameters(credentials, config);

      // call the job launch web script
      try {
        String stat_str = webServiceAPI.executeKillJobWebScript(jobLaunchParams);
        if (stat_str.equalsIgnoreCase("true")){
        	stat = true;
        	jobMonitorThread.removeJobFromQueue(config.getJobId(), null);
        }
        complete = true;

      } catch (AuthFailedException e) {
        authFailed = e;
      }
    }
    return stat;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.JobLaunchService#kill(java.lang.String)
   */
  @Override
  public String getStatus(JobConfig config, Credentials credentials) throws Exception {
    logger.debug("Find status for job: " + config.getJobId());
    
    // Need to decide if we will send kill command the server or to the client
    if(isLocalMonitoring(config)) {
      return statusJobLocalMonitoring(config, credentials);

    } else {
      return statusJobVeloServerMonitoring(config);
    }
  }
  
  private String statusJobLocalMonitoring(JobConfig config, Credentials credentials) throws Exception {
    
    String jobId = config.getJobId();
    try {

      // Look up the job from the cache
      JobManager jobManager = jobMonitorThread.jobIdToJobManager.get(jobId);
      
      if(jobManager == null) {
    	//reconnect to the job
    	logger.debug("Job Manager was null. Creating new Job Manager for job");
        // we have to make a new job manager so we can kill the job
        JobHandler jobHandler = getJobHandler(config);
        setCredentials(jobHandler.getExecObject(), credentials);

        // Use the run dir from saved properties, not the JobConfig!
        String remoteDir = TifServiceLocator.getCmsService().getProperty(config.getContextPath(), VeloTifConstants.JOB_RUNDIR);
        config.setRemoteDir(remoteDir);

        jobManager = new JobManager(config, jobHandler);

      }
      // query job status
      JobStatusInfo stat = jobManager.getJobHandler().queryJobStatus(jobId, jobManager.getJobSupport());
      
      return stat.toString();

    } catch (AuthFailedException e) {
      jobMonitorThread.removeJobFromQueue(jobId, null); // don't log exception
      throw e; // now throw it back so callers get it

    } catch (Exception e) {
      jobMonitorThread.removeJobFromQueue(jobId, e);
      throw e;
    } 


  }

  private String statusJobVeloServerMonitoring(JobConfig config) {    
    Credentials credentials = null;
    boolean complete = false;
    AuthFailedException authFailed = null;
    String jobID = null;

    while(!complete) {
      try {
        credentials = webServiceAPI.getCredentialsToComputeServer(config, authFailed);
      } catch (AuthCancelException e) {
        break;
      }
      JobLaunchParameters jobLaunchParams = new JobLaunchParameters(credentials, config);

      // call the status webscript
      try {
         return webServiceAPI.executeJobStatusWebScript(jobLaunchParams);

      } catch (AuthFailedException e) {
        authFailed = e;
      }
    }
    return "Error finding job Status";
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.JobLaunchService#reconnect(gov.pnnl.velo.tif.model.JobConfig, java.lang.String)
   */
  @Override
  public void reconnect(JobConfig config, Credentials credentials) throws Exception {
    logger.debug("Reconnecting job: " + config.getJobId());
    
    // Need to decide if we will reconnect from server or client
    if(isLocalMonitoring(config)) {
      reconnectJobLocalMonitoring(config, credentials);

    } else {
      reconnectJobVeloServerMonitoring(config);
    }    
  }
  
  private void setCredentials(ExecInterface exec, Credentials credentials) throws Exception {
    
    // Set the custom userinfo object to session
    // credentials will always be null when launching from client-side, so this will
    // only get called from the velo server
    if(credentials != null) {
      NoPromptUserInfo userinfo = new NoPromptUserInfo();
      userinfo.setCredentials(credentials);
      if (exec instanceof SshExec ) {
        ((SshExec) exec).setUserInfo(userinfo);
      }
      if (credentials.getIdentityFile() != null) {
        exec.addIdentity(credentials.getIdentityFile().getAbsolutePath());
      }
    }
    
  }
  
  private void reconnectJobLocalMonitoring(JobConfig config, Credentials credentials) throws Exception {
        
    String jobId = config.getJobId();

    try {
      // Create JobHandler
      JobHandler jobHandler = getJobHandler(config);
      setCredentials(jobHandler.getExecObject(), credentials);
      
      // Use the run dir from saved properties, not the JobConfig!
      String remoteDir = TifServiceLocator.getCmsService().getProperty(config.getContextPath(), VeloTifConstants.JOB_RUNDIR);
      config.setRemoteDir(remoteDir);

      // submit job
      JobManager jobManager = new JobManager(config, jobHandler);
      jobMonitorThread.jobsMonitored.add(jobManager);    
    
      // reconnect the job
      jobManager.reconnect(jobId, false);
      jobMonitorThread.jobIdToJobManager.put(jobId, jobManager);

    } catch (AuthFailedException e) {
      jobMonitorThread.removeJobFromQueue(jobId, null); // don't log exception
      throw e; // now throw it back so callers get it

    } catch (Exception e) {
      jobMonitorThread.removeJobFromQueue(jobId, e);
      throw e;
    } 
    
  }
  
  private void reconnectJobVeloServerMonitoring(JobConfig config) {
    
    Credentials credentials = null;
    boolean complete = false;
    AuthFailedException authFailed = null;
    String jobID = null;
    
    while(!complete) {
      try {
        credentials = webServiceAPI.getCredentialsToComputeServer(config, authFailed);
      } catch (AuthCancelException e) {
        break;
      }
      JobLaunchParameters jobLaunchParams = new JobLaunchParameters(credentials, config);

      // call the job launch web script
      try {
        webServiceAPI.executeReconnectJobWebScript(jobLaunchParams);
        complete = true;
        
      } catch (AuthFailedException e) {
        authFailed = e;
      }
    }

  }
  
  private void initializeRemoteRunDir(JobConfig jobConfig, JobHandler jobHandler) {  
    String customName = jobHandler.getRemoteJobDirName(jobConfig);
    if(customName != null) {
      jobConfig.setRemoteDir(jobConfig.getRemoteDir() + "/"+ jobHandler.getRemoteJobDirName(jobConfig));
    }
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.JobLaunchService#getLocallyMonitoredJobs()
   */
  @Override
  public List<JobManager> getLocallyMonitoredJobs() {
    return jobMonitorThread.getJobsMonitored();
  }

  private JobHandler getJobHandler(JobConfig jobConfig) throws Exception {
    Code code = jobConfig.getCode();
    logger.debug("In getJobHandler: code is" + code);
    String jobHandlerId = code.getJobLaunching().getJobHandlerId();
    logger.debug("job handler id :" + jobHandlerId);
    if (jobHandlerId == null || jobHandlerId.trim().isEmpty()) {
      // set defaultHandler
      jobHandlerId = "defaultCmsJobHandler";
    }

    JobHandler jobHandler = null;
    
    // get the handler from the spring container
    jobHandler = (JobHandler) applicationContext.getBean(jobHandlerId);

    jobHandler.setJobConfig(jobConfig);

    return jobHandler;
  }
  
  /**
   * TODO: pass in a generic Monitor class
   * @see gov.pnnl.velo.tif.service.JobLaunchService#launchJob(gov.pnnl.velo.tif.model.JobConfig, gov.pnnl.velo.tif.model.Credentials)
   */
  @Override
  public String launchJob(final JobConfig config, Credentials credentials) throws Exception {

    // Need to decide if we will launch to the server or to the client
    if(isLocalMonitoring(config)) {
      return launchJobLocalMonitoring(config, credentials);

    } else {
      return launchJobVeloServerMonitoring(config);
    }
  }
  
  /**
   * If we are launching to localhost, or the machine is behind a firewall
   * such that it is not accessible by the velo server, then we need to do 
   * a client-side launch and monitoring.
   * @param config
   * @return
   */
  private boolean isLocalMonitoring(JobConfig config) {
    return (webServiceAPI == null) || config.isLocalMonitoring();
  }

  private String launchJobVeloServerMonitoring(JobConfig config) throws Exception {
    Credentials credentials = null;
    boolean complete = false;
    AuthFailedException authFailed = null;
    String jobID = null;
    
    while(!complete) {
      try {
        credentials = webServiceAPI.getCredentialsToComputeServer(config, authFailed);
      } catch (AuthCancelException e) {
        break;
      }
      JobLaunchParameters jobLaunchParams = new JobLaunchParameters(credentials, config);
      
      // call the job launch web script
      try {
        jobID = webServiceAPI.executeLaunchJobWebScript(jobLaunchParams);
        complete = true;
        
      } catch (AuthFailedException e) {
        authFailed = e;
      }
    }
    return jobID;
  }

  private String launchJobLocalMonitoring(JobConfig launchConfig, Credentials credentials) throws Exception {
    
    // make sure all the launch config's variables are set correctly
    // TODO: do we need  this?
    //launchConfig.validate();

    // Create JobHandler
    JobHandler jobHandler = getJobHandler(launchConfig);
    setCredentials(jobHandler.getExecObject(), credentials);
    
    // Create a special subdir with date/time stamp for the remote run dir
    // ONLY do this on launch, not on reconnect
    initializeRemoteRunDir(launchConfig, jobHandler);

    // submit job
    JobManager jobManager = new JobManager(launchConfig, jobHandler);
    jobMonitorThread.jobsMonitored.add(jobManager);    
    String jobId = null;
    
    try {
      // Get the overwrite flag (i.e., should remote dir be wiped before running) from the JobConfig
      boolean overwrite = true;
      String overwriteStr = launchConfig.getJobHandlerParameters().get(VeloTifConstants.JOB_HANDLER_PROP_OVERWRITE_REMOTE_DIR);
      if(overwriteStr != null && !overwriteStr.isEmpty()) {
        overwrite = Boolean.valueOf(overwriteStr);
      }
      String options = jobHandler.getSubmitCommandOptions(launchConfig);
      jobId = jobManager.submit(overwrite, options, false);  //need to set the overwrite flag to true for cssef job, in the 'submit_CESMWorkflow' script is the path to the input which changes for each run
      if(jobId != null) { // jobId will be null if an error occurred or the user cancelled
        jobMonitorThread.jobIdToJobManager.put(jobId, jobManager);
      }
      
    } catch (AuthFailedException e) {
      jobMonitorThread.removeJobFromQueue(jobManager, null); // don't log exception
      throw e; // now throw it back so callers get it
      
    } catch (Exception e) { // in case we get some uncaught exception, quit monitoring
      jobMonitorThread.removeJobFromQueue(jobManager, e);
      throw e; // now throw it back so callers get it
    }

    return jobId;
  }


  /**
   * Class that controls how authentication information is passed to JSCH.
   * We don't need to prompt for authentication, since it was sent via the
   * Credential Object to the job launch Service.
   *
   */
  protected class NoPromptUserInfo extends VeloUserInfo {

    private Credentials credentials;
    private int authenticationAttemptCount = 0;

    /**
     * Method getCredentials.
     * @return Credentials
     */
    public Credentials getCredentials() {
      return credentials;
    }

    /**
     * Method setCredentials.
     * @param credentials Credentials
     */
    public void setCredentials(Credentials credentials) {
      this.credentials = credentials;
      logger.debug("Set credentials in userinfo:" + credentials.getUserName());
      
      // make sure passwords are initialized
      if(credentials != null) {
        this.passphrase = credentials.getCredential();
        this.passwd = credentials.getCredential();
        this.passpki = credentials.getCredential();
      }
    }    

    /* (non-Javadoc)
     * @see org.kepler.ssh.VeloUserInfo#promptPassphrase(java.lang.String)
     */
    @Override
    public boolean promptPassphrase(String message) {
      authenticationAttemptCount++;
      if(authenticationAttemptCount > 1){
        String title = VeloUserInfo.TITLE;
        String errMessage = "Authentication failed.  Invalid username or password.  Please try again.";
        throw new AuthFailedException("Authentication Failed.", title, message, errMessage, new String[]{message});
      }
      return true;
    }

    /**
     * Method promptPassword.
     * @param message String
     * @return boolean
     * @see com.jcraft.jsch.UserInfo#promptPassword(String)
     */
    public boolean promptPassword(String message) {
      authenticationAttemptCount++;
      if(authenticationAttemptCount > 1){
        String title = VeloUserInfo.TITLE;
        String errMessage = "Authentication failed.  Invalid username or password.  Please try again.";
        throw new AuthFailedException("Authentication Failed.", title, message, errMessage, new String[]{message});
      }
      return true;
    }

    /**
     * Method promptKeyboardInteractive.
     * @param destination String
     * @param name String
     * @param instruction String
     * @param prompt String[]
     * @param echo boolean[]
     * @return String[]
     * @throws Exception
     * @see com.jcraft.jsch.UIKeyboardInteractive#promptKeyboardInteractive(String, String, String, String[], boolean[])
     */
    @Override
    public String[] promptKeyboardInteractive(String destination,
        String name, String instruction, String[] prompt, boolean[] echo) {

      authenticationAttemptCount++;
      if(authenticationAttemptCount > 1){

        if(prompt[prompt.length-1].toLowerCase().contains("passcode")){
          throw new ReAuthNotAllowedException("\nAuthentication failed for one of two reasons: \n1. Passcode expired before authentication attempted - retry authentication with next passcode.\n2. Server is expecting a second passcode to be entered - user should autheticate to server externally (for example, using putty) and then retry launching job.\n\n");
        }else{
          // they typed the wrong password
          String title = VeloUserInfo.TITLE;
          String message = "Authenticating to: " + destination;
          String errMessage = "Authentication failed.  Invalid username or password.  Please try again.";
          throw new AuthFailedException("Authentication Failed.", title, message, errMessage, prompt);
         
        }
      }
      logger.debug("PromptKeyboardInteractive");
      // System.out.println("SSH: promptKI called\n"+
      // "\tDestination: " + destination +
      // "\n\tName: " + name +
      // "\n\tinstruction: " + instruction +
      // "\n\tpromptlen: " + prompt.length);

      if (credentials.getCredential() != null) {
        passpki = credentials.getCredential();
        String[] response = new String[1];
        response[0] = passpki;
        logger.debug("PromptKeyboardInteractive. returning response");
        return response;
      } else {
        return null;
      }
    }

  }

}
