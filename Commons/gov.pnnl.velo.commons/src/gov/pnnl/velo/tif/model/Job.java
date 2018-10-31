package gov.pnnl.velo.tif.model;

import org.kepler.job.JobStatusInfo;
import org.kepler.job.JobSupport;
import org.kepler.ssh.ExecInterface;
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



/**
 * Represents a Job currently being managed by JobManager
 */
public class Job {
  
  private JobConfig jobConfig;
  private String jobId;
  private JobHandler jobHandler; //not in config
  private String cmsUser; // will be null if this is a client-side monitor
  private JobStatusInfo jobStatus; // current job status
  
  public Job(String jobId, JobConfig jobConfig, JobHandler jobHandler, String cmsUser) {
    this.jobConfig = jobConfig;
    this.jobId = jobId;
    this.jobHandler = jobHandler;
    this.cmsUser = cmsUser;
  }

  public JobConfig getJobConfig() {
    return jobConfig;
  }

  public void setJobConfig(JobConfig jobConfig) {
    this.jobConfig = jobConfig;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public JobHandler getJobHandler() {
    return jobHandler;
  }

  public void setJobHandler(JobHandler jobHandler) {
    this.jobHandler = jobHandler;
  }

  public String getCmsUser() {
    return cmsUser;
  }

  public void setCmsUser(String cmsUser) {
    this.cmsUser = cmsUser;
  }

  public JobStatusInfo getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(JobStatusInfo jobStatus) {
    this.jobStatus = jobStatus;
  }
  
}
