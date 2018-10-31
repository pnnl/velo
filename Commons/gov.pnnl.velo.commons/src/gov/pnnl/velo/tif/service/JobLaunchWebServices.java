package gov.pnnl.velo.tif.service;

import gov.pnnl.velo.tif.model.Credentials;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.JobLaunchParameters;

import org.kepler.ssh.AuthFailedException;

public interface JobLaunchWebServices {

  public Credentials getCredentialsToComputeServer(JobConfig config, AuthFailedException e);

  public String executeLaunchJobWebScript(JobLaunchParameters jobParameters) throws AuthFailedException;
  
  public void executeReconnectJobWebScript(JobLaunchParameters jobParameters) throws AuthFailedException;
  
  public String executeKillJobWebScript(JobLaunchParameters jobParameters) throws AuthFailedException;
  
  public String executeJobStatusWebScript(JobLaunchParameters jobParameters) throws AuthFailedException;

}