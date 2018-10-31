package gov.pnnl.velo.tif.service;

import gov.pnnl.velo.tif.model.Credentials;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.service.impl.JobManager;

import java.util.List;

public interface JobLaunchService {
  
  /**
   * Method to initialize the registry by loading entries from appropriate source.
   */
  public void init();
    
  /**
   * Return after Job is submitted to the JobMonitoringQueue.  Users may 
   * call queryJobStatus() to determine if the job is complete or not.
   * Credentials can be null. If null, it's up to launch method to prompt for them if needed.
   * The job will run asynchronously in the background.
   * 
   * @param config
   * @param credentials
   * @return
   */
  public String launchJob(JobConfig config, Credentials credentials) throws Exception;
  
  /**
   * Terminate the given job. When the job is actively being monitored by Velo Server (i.e. job was 
   * started with Velo and job is still in queue(waiting/running) then input JobConfig object
   * could just contain the jobId and the server would look up rest but if you are using 
   * local job monitoring or if the job is already complete Velo needs most of job config info
   * to look up the job. For example, it needs the code id, machine id, remote path etc
   * @param jobconfig
   * @param credentials - credentials to compute server
   */
  public boolean kill(JobConfig config, Credentials credentials) throws Exception;
  
  /**
   * Reconnect to a disconnected job
   * @param jobConfig
   * @param jobId
   */
  public void reconnect(JobConfig jobConfig, Credentials credentials) throws Exception;
  
  /**
   * Get the jobs that are currently being monitored locally
   * @return
   */
  public List<JobManager> getLocallyMonitoredJobs();
  
    
  /**
   * Queries job status on compute node.
   * When the job is actively being monitored by Velo Server (i.e. job was 
   * started with Velo and job is still in queue(waiting/running) then input JobConfig object
   * could just contain the jobId and the server would look up rest but if you are using 
   * local job monitoring or if the job is already complete Velo needs most of job config info
   * to look up the job. For example, it needs the code id, machine id, remote path etc
   * @param config
   * @param credentials -  credentials to compute server
   * @return status of job as a string - Wait, Running, Error, NotInQueue, Success or Failed
   * @throws Exception
   */
  public String getStatus(JobConfig config, Credentials credentials) throws Exception;

}