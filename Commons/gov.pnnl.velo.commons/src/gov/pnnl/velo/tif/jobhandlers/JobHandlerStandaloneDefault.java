package gov.pnnl.velo.tif.jobhandlers;

import java.io.File;
import java.util.List;

import org.kepler.job.JobException;
import org.kepler.job.JobStatusInfo;
import org.kepler.job.JobSupport;
import org.kepler.ssh.ExecException;
import org.kepler.ssh.ExecInterface;

import gov.pnnl.velo.exception.JobConfigNotSetException;
import gov.pnnl.velo.tif.model.Fileset;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.JobHandler;

public class JobHandlerStandaloneDefault implements JobHandler{

  
	/* (non-Javadoc)
   * @see gov.pnnl.velo.tif.model.JobHandler#init()
   */
  @Override
  public void init() {
    // TODO Auto-generated method stub
    
  }

  @Override
	public ExecInterface getExecObject() throws JobConfigNotSetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJobConfig(JobConfig jobConfig) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JobConfig getJobConfig() {
		// TODO Auto-generated method stub
		return null;
	}
	
	 @Override
	  public String getRemoteJobDirName(JobConfig jobConfig) {

	    // TODO Auto-generated method stub
	    return null;
	  }

	@Override
	public File prepareLocalWorkingDirectory() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stageLocalInputs(List<Fileset> localInputs)
			throws ExecException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stageRemoteInputs(List<Fileset> remoteInputs)
			throws JobException, ExecException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void jobBeforeSubmit() throws JobException, ExecException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void jobSubmitted(JobStatusInfo status) throws JobException,
			ExecException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSelfPolling() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JobStatusInfo queryJobStatus(String jobID, JobSupport jobSupport) throws JobException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void jobStarted(JobStatusInfo status) throws JobException,
			ExecException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void jobRunning(JobStatusInfo status) throws JobException,
			ExecException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void jobComplete(JobStatusInfo status) throws JobException,
			ExecException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void jobTerminated(JobStatusInfo status) throws JobException,
			ExecException {
		// TODO Auto-generated method stub
		
	}

  @Override
  public void logMessage(String message) {
    // TODO Auto-generated method stub
    
  }

  @Override
	public void recordJobStatus(String status) {
		// TODO Auto-generated method stub
		
	}

  @Override
  public void stageVeloServerInputs(List<Fileset> veloServerInputs,
      List<Fileset> localInputs) throws JobException {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public String getSubmitCommandOptions(JobConfig launchConfig){
	  return "";
  }

 

}
