package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.exception.ExceptionUtils;
import gov.pnnl.velo.tif.service.CmsService;
import gov.pnnl.velo.util.PrioritizedThreadFactory;
import gov.pnnl.velo.util.VeloTifConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kepler.job.JobStatusCode;
import org.kepler.job.JobStatusInfo;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class JobMonitoringThread extends Thread {
  
  private static Logger logger = Logger.getLogger(JobMonitoringThread.class);

  // thread pools - using thread pools is more efficient than creating a bunch
  // of threads
  protected ThreadPoolTaskScheduler completedJobThreadPool;
  protected ThreadPoolTaskScheduler runningJobThreadPool;

  // maps so we know all the currently running jobs
  protected Map<String, JobManager> jobIdToJobManager = Collections.synchronizedMap(new HashMap<String, JobManager>());
  protected List<JobManager> jobsMonitored = Collections.synchronizedList(new ArrayList<JobManager>());

  // map so we know if there is a currently running thread for this job id - so we don't waste cycles
  // putting a bunch of running threads on the pool if a thread is currently being executed
  protected Map<String, Boolean>  jobIdToThreadExecuting = Collections.synchronizedMap(new HashMap<String, Boolean>());
  
  private CmsService cmsService;
  
  public JobMonitoringThread() {

    super();
    setName("Velo Server Job Monitoring Thread");
    setDaemon(true); // we must not hang up JVM termination
  }
  
  /**
   * @return the jobsMonitored
   */
  public List<JobManager> getJobsMonitored() {
    return jobsMonitored;
  }

  /**
   * Method doRuntimeProcessing.
   * 
   * @param job
   *          Job
   * @param stat
   *          JobStatusInfo
   */
  private boolean doRuntimeProcessing(final JobManager jobManager, final JobStatusInfo stat) {
    boolean threadScheduled = false;
    
    synchronized(jobIdToThreadExecuting) {
      Boolean isThreadRunning = jobIdToThreadExecuting.get(jobManager.getJobId());
      if(isThreadRunning == null || isThreadRunning == false) {
        jobIdToThreadExecuting.put(jobManager.getJobId(), true);
        threadScheduled = true;

        runningJobThreadPool.schedule(new Runnable() {
          
          @Override
          public void run() {
            
            try {
              logger.debug("job RUNNING for job " + jobManager.getJobId() + " and user " + jobManager.getJobConfig().getCmsUser());
              cmsService.setRunAsUser(jobManager.getJobConfig().getCmsUser());
              jobManager.getJobHandler().jobRunning(stat);
              
            } catch (Exception e) {
              logger.error(e);
              logErrorInJobHandling("running", jobManager, e, VeloTifConstants.STATUS_ERROR);
              
            } finally {
              synchronized(jobIdToThreadExecuting) {
                jobIdToThreadExecuting.remove(jobManager.getJobId());
              }
            }
          }
        }, new Date());
      
      } 
    }
    return threadScheduled;
    
  }
  
  private boolean doStartedProcessing(final JobManager job, final JobStatusInfo currentstat) {
    boolean threadScheduled = false;
    
    synchronized(jobIdToThreadExecuting) {
      Boolean isThreadRunning = jobIdToThreadExecuting.get(job.getJobId());
      if(isThreadRunning == null || isThreadRunning == false) {
        jobIdToThreadExecuting.put(job.getJobId(), true);
        threadScheduled = true;
        runningJobThreadPool.schedule(new Runnable() {

          @Override
          public void run() {

            try {
              logger.debug("job STARTED for job " + job.getJobId() + " and user " + job.getJobConfig().getCmsUser());
              cmsService.setRunAsUser(job.getJobConfig().getCmsUser());
              job.getJobHandler().jobStarted(currentstat);

            } catch (Exception e) {
              logger.error(e);
              logErrorInJobHandling("started", job, e, VeloTifConstants.STATUS_ERROR);

            } finally {
              synchronized(jobIdToThreadExecuting) {
                jobIdToThreadExecuting.put(job.getJobId(), false);
              }
            }
          }

        }, new Date());
      
      } 
    }
    return threadScheduled;
  }
  
  private boolean doCompletedProcessing(final JobManager job, final JobStatusInfo currentstat) {
    boolean threadScheduled = false;
    
    synchronized(jobIdToThreadExecuting) {
      Boolean isThreadRunning = jobIdToThreadExecuting.get(job.getJobId());
      if(isThreadRunning == null || isThreadRunning == false) {
        jobIdToThreadExecuting.put(job.getJobId(), true);
        threadScheduled = true;

        completedJobThreadPool.schedule(new Runnable() {
          
          @Override
          public void run() {
            
            try {
              logger.debug("job COMPLETE for job " + job.getJobId() + " and user " + job.getJobConfig().getCmsUser());
              
              cmsService.setRunAsUser(job.getJobConfig().getCmsUser());
              job.getJobHandler().jobComplete(currentstat);
              
            } catch (Exception e) {
              // TODO - should catch fine grained exception - to know
              // if status has already been updated or not by handler.
              // if status is set to job status and if only post
              // processing failed
              // we can send status here as null so that we don't
              // overwrite actual
              // job status
              logger.error(e);
              logErrorInJobHandling("post-processing", job, e, null);
              
            } finally {
              removeJobFromQueue(job.getJobId(), null);          
            }
          }
          
        }, new Date());
      
      } 
    }
    return threadScheduled;
    
    
    
  }

  /**
   * @param phase
   * @param job
   * @param e
   * @param jobStatus
   */
  protected void logErrorInJobHandling(String phase, JobManager jobManager, Throwable e, String jobStatus) {

    String msg = "Exception thrown monitoring job " + jobManager.getJobId() + " during " + phase + " phase";
    logger.error(msg, e);
    jobManager.getJobHandler().logMessage(msg + "\n" + ExceptionUtils.getFullStackTrace(e));
    if (jobStatus != null) {
      jobManager.getJobHandler().recordJobStatus(jobStatus);
    }
  }

  /**
   * Method removeJob.
   * 
   * @param jobId
   *          String
   */
  public void removeJobFromQueue(String jobId, Throwable e) {
    logger.debug("removing job from queue: " + jobId);
    JobManager jobManager = jobIdToJobManager.remove(jobId);
    jobIdToThreadExecuting.remove(jobId);
    removeJobFromQueue(jobManager, e);
  }
  
  public void removeJobFromQueue(JobManager jobManager, Throwable e) {
    if(jobManager != null) {
      jobsMonitored.remove(jobManager);
      
      // make sure the status of this job is marked as error 
      if(e != null) {
        jobManager.getJobHandler().recordJobStatus(VeloTifConstants.STATUS_ERROR);
      }
    }
  }
  
  /**
   * @param cmsService the cmsService to set
   */
  public void setCmsService(CmsService cmsService) {
    this.cmsService = cmsService;
  }

  /**
   * Method setCompletedJobThreadPool.
   * 
   * @param completedJobThreadPool
   *          ThreadPoolTaskScheduler
   */
  public void setCompletedJobThreadPool(ThreadPoolTaskScheduler completedJobThreadPool) {
    this.completedJobThreadPool = completedJobThreadPool;
    this.completedJobThreadPool.setThreadFactory(new PrioritizedThreadFactory("Completed Jobs Thread Pool", "medium"));
  }

  /**
   * Method setRunningJobThreadPool.
   * 
   * @param runningJobThreadPool
   *          ThreadPoolTaskScheduler
   */
  public void setRunningJobThreadPool(ThreadPoolTaskScheduler runningJobThreadPool) {
    this.runningJobThreadPool = runningJobThreadPool;
    this.runningJobThreadPool.setThreadFactory(new PrioritizedThreadFactory("Running Jobs Thread Pool", "medium"));
  }
  
  
  /**
   * Method run.
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    try {
      runInternal();
    } catch (Throwable e) {
      logger.error("Job Monitor Thread failed", e);
    }
  }

  private void runInternal() {
    logger.debug("Started monitoring of all jobs in JobMonitoringThread");

    // loop through all the jobs in jobIdToJob map
    // check status of job and then call appropriate job handler
    // methods - jobStarted() or jobCompleted() using different
    // thread pool for each
    // jobBeforeSubmitted() and jobSubmitted() methods
    // would have got called from JobManager.java
    while (true) {
      
      Collection<String> jobIds = new ArrayList<String>();
      synchronized(jobIdToJobManager) {
        for(String jobId : jobIdToJobManager.keySet()) {
          jobIds.add(jobId);
        }
      }
      
      logger.debug("Current number of jobs to monitor " + jobIds.size());

      for (final String jobId : jobIds) {
        JobManager job = jobIdToJobManager.get(jobId);
        if(job == null) {
          continue;
        }
        logger.debug("Current jobID: " + job.getJobId());

        // First check if we should be monitoring this job or if the job is
        // self-polling. (An example of self-polling job would be if job isn't
        // running via a scheduler and instead the workflow engine sends status
        // messages over active mq. In this case, the job handler would do its
        // own
        // monitoring.)
        // If the job is self-polling, then just skip since we aren't monitoring
        // it.
        if (job.getJobHandler().isSelfPolling()) {
          continue;
        }
        
        try {
          final JobStatusInfo laststat = job.getJobStatus();
          int pollingInterval = job.getJobConfig().getPollingInterval() * 1000;

          // 1. check if polling interval has elapsed from last poll
          // time or if job is running. We will have to call
          // handler.jobRunning() even if polling time has not elapsed
          // This gives a chance to the handler to do/continue runtime
          // processing
          long timediff = (new Date()).getTime() - laststat.statusRecordTime.getTime();

          // 2. if it is time to poll, query job status
          final JobStatusInfo currentstat = job.getJobHandler().queryJobStatus(laststat.jobID, job.getJobSupport());
          boolean threadScheduled = false;

          // 3. if status has not changed don't do anything except for
          // jobRunning case
          if (laststat.statusCode != currentstat.statusCode) {
            
            if (currentstat.statusCode == JobStatusCode.Running) {
              threadScheduled = doStartedProcessing(job, currentstat);
                  
            } else if (currentstat.statusCode == JobStatusCode.NotInQueue
                || currentstat.statusCode == JobStatusCode.Error
                || currentstat.statusCode == JobStatusCode.Success
                || currentstat.statusCode == JobStatusCode.Failed) {

              threadScheduled = doCompletedProcessing(job, currentstat);
            }
            
            
          } else if (currentstat.statusCode == JobStatusCode.Running && (timediff >- pollingInterval) ) {
            // if job is running we will have to call
            // handler.jobRunning() to give a chance to
            // the handler to do/continue runtime processing
            threadScheduled = doRuntimeProcessing(job, currentstat);
            
          } 
          if(threadScheduled) {
            // update job status in job obj
            job.setJobStatus(currentstat);
            
            laststat.statusCode = currentstat.statusCode;
          }

        } catch (Throwable e) {
          // Set status as "unable to determine job status"
          // on job node
          e.printStackTrace();
          job.getJobHandler().logMessage("Unable to determine job status: \n" + ExceptionUtils.getFullStackTrace(e));
          removeJobFromQueue(job.getJobId(), e); // if we get an error, remove the job from the queue
        }
        

      }
      // looped through jobs
      // TODO: May be sleep time should be determined by number of jobs in queue
      // if there are just one or two may be we can sleep longer
      // if there are multiple may be we don't need to sleep at all
      int sleeptime = 2000;
      if (jobIdToJobManager.size() == 0) {
        // sleep longer
        sleeptime = 60000;
      }
      try {
        Thread.sleep(sleeptime);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      // and start the loop again
    } // end of while true loop
  } // end of run method

}