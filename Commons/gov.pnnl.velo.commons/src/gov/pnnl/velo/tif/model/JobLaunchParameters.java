package gov.pnnl.velo.tif.model;


/**
 * Used to encapsulate all job launch service parameters for remote
 * execution
 *
 */
public class JobLaunchParameters {
  
  private Credentials credentials;
  private JobConfig jobConfig;
  
  /**
   * @param credentials
   * @param jobConfig
   */
  public JobLaunchParameters(Credentials credentials, JobConfig jobConfig) {
    super();
    this.credentials = credentials;
    this.jobConfig = jobConfig;
  }

  /**
   * @return the credentials
   */
  public Credentials getCredentials() {
    return credentials;
  }
  
  /**
   * @param credentials the credentials to set
   */
  public void setCredentials(Credentials credentials) {
    this.credentials = credentials;
  }
  
  /**
   * @return the jobConfig
   */
  public JobConfig getJobConfig() {
    return jobConfig;
  }
  
  /**
   * @param jobConfig the jobConfig to set
   */
  public void setJobConfig(JobConfig jobConfig) {
    this.jobConfig = jobConfig;
  }

}

