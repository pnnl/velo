package gov.pnnl.velo.tif.model;

import gov.pnnl.velo.exception.JobConfigNotSetException;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.kepler.job.JobException;
import org.kepler.job.JobStatusInfo;
import org.kepler.job.JobSupport;
import org.kepler.ssh.ExecException;
import org.kepler.ssh.ExecInterface;


/** 
 * Abstract interface for controlling the lifecycle of a velo job.
 * 
 * TODO: add new methods to give JobHandler full control
 * TODO: get rid of FileStagingHandler on the server, and put into this class
 * TODO: change LaunchConfiguration to JobConfig
 */
public interface JobHandler
{
  static HashMap<String,String> jobsMonitored = new HashMap<String,String>();
  
  /**
   * Init any variables necessary.
   */
  public void init();
  
  /**
   * Method setExecObject.
   * @param exec specific ExecInterface impl
   */
 // public void setExecObject(ExecInterface exec);

  /**
   * Method getExecObject.
   * @return instance of ExecInterface
 * @throws JobConfigNotSetException if setJobConfig was not called before getExecObject()
   */
  public ExecInterface getExecObject() throws JobConfigNotSetException;

  /**
   * Method setJobConfig.
   * @param jobConfig jobConfig
   */
  public void setJobConfig(JobConfig jobConfig);

  /**
   * Method getJobConfig
   * @return JobConfig
   */
  public JobConfig getJobConfig();

  /**
   * Used to create a unique job run directory so that
   * user need not create it. User can run with say a single 
   * parent remote directory (set in jobConfig.setRemoteDir()) 
   * If unique job directories need not be created, this method
   * can return null. In that jobs will be run in jobConfig.getRemoteDir()
   * If this method returns a String then jobConfig.remoteDir will be set to
   * jobConfig.getRemoteDir() + "/" + the return value of this method
   * 
   * @return
   */
  public String getRemoteJobDirName(JobConfig jobConfig);
  
  /**
   * Take whatever files you need from alfresco and put them in a local
   * folder so they will be accessible via the transfer method
   */
  public File prepareLocalWorkingDirectory() throws Exception;
  
  /**
   * Transfer the files from in Velo server to the remote server where job is running
 * @throws ExecException 
   */
  public void stageVeloServerInputs(List<Fileset> veloServerInputs, List<Fileset> localInputs) throws JobException;


  /**
   * Transfer the files from the local staging directory to the remote server
 * @throws ExecException 
   */
  public void stageLocalInputs(List<Fileset> localInputs) throws ExecException;
  
  /** 
   * Copy/link/move files already on the compute server to the appropriate job
   * run directory
   * @param remoteInputs
   * @throws JobException
 * @throws ExecException 
   */
  public void stageRemoteInputs(List<Fileset> remoteInputs) throws JobException, ExecException;

  /**
   * Job Handlers can use this method to change any file in the local staging
   * directory to do special things to input files before they are transferred
   * to the compute server.
   * @throws JobException
   * @throws ExecException
   */
  public void jobBeforeSubmit() throws JobException,ExecException;
  
  /**
   * Called when the job has been submitted to the queue on the
   * compute server.
   * @param status
   * @throws JobException * @throws ExecException */
  public void jobSubmitted(JobStatusInfo status) 
      throws JobException,ExecException;

  /**
   * If the job handler is self polling, then it will NOT
   * be placed in the monitoring queue, so it will be up to
   * itself to generate polling events (e.g., via an
   * async AMQP message). Should be checked by JobLaunchService class
   * @return
   */
  public boolean isSelfPolling();

  /**
   * During the job monitor queue's polling, use this method to determine the job's
   * status. This way Handler can determine job status any way it wants. Paas job id 
   * and default JobSupport class loaded based on queue system
   * @return
   */
  public JobStatusInfo queryJobStatus(String jobID, JobSupport jobSupport) throws JobException;

  /**
   * Called when the job has started to run.
   *  
   * @param status
   * @throws JobException
   * @throws ExecException */
  public void jobStarted(JobStatusInfo status) 
      throws JobException,ExecException;

  /**
   * A hook for actions associated with running job. This is called periodically
   * by the job monitoring queue when the job is running.
   * Useful for processing output files such as monitoring
   * individual runs of UQ, PE etc.
   * @param status
   * @throws JobException * @throws ExecException */
  public void jobRunning(JobStatusInfo status) 
      throws JobException,ExecException;

  /**
   * Called when the job is completed.
   * Information about the job that is useful to any handler is provided.
   * @param jobConfig
   * @param status
   * @throws JobException * @throws ExecException */
  public void jobComplete(JobStatusInfo status) 
      throws JobException, ExecException;

  /**
   * A hook to associate when job get explicitly killed/terminated by user
   * either before it started running or after it started running and before it completed
   * @param exec
   * @param request
   * @param status
   */
  public void jobTerminated(JobStatusInfo status)
      throws JobException, ExecException;

  /**
   * In the case of an error, we may want to log this in a persistent
   * way (i.e, log file, CMS property, etc.).  We may want to log several
   * messages, even though we don't necessarily want to change the job
   * status.  This lets us do so.
   * @param exception
   * @param message
   */
  public void logMessage(String message);

  /**
   * Method for persisting job status information.  See VeloTifConstants.STATUS_*
   * for a list of all possible statuses
   * @param status
   */
  public void recordJobStatus(String status);

  /**
   * Generate any command line args to be sent to the submit command. 
   * @param launchConfig
   * @return String to be appended to the submit command
   */
  public String getSubmitCommandOptions(JobConfig launchConfig);


}
