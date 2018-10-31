/**
 * 
 */
package gov.pnnl.velo.tif.jobhandlers;

import gov.pnnl.velo.exception.JobConfigNotSetException;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.service.TifServiceLocator;

import java.io.File;
import java.util.Date;

/**
 * TODO: copy impl from one of Chandrika's classes
 * 
 */
public class JobHandlerVeloClientDefault extends JobHandlerVeloDefault {
	
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.model.JobHandler#init()
   */
  @Override
  public void init() {
    // TODO Auto-generated method stub
    
  }

  /*
	 * (non-Javadoc)
	 * Prepare the local working /staging directory and set it in jobConfig
	 * @see gov.pnnl.velo.tif.model.JobHandler#prepareLocalStagingDirectory()
	 */
	@Override
	public File prepareLocalWorkingDirectory() throws Exception {
	  if(jobConfig==null){
	    throw new JobConfigNotSetException("Call setJobConfig before calling any other method in jobhandler");
	  }
	  File localWorkingDir = getLocalWorkingDir();
		if (localWorkingDir == null) {
			File parent = TifServiceLocator.getVeloWorkspace().getVeloFolder();
			File jobsLocalFolder = new File(parent, "jobs_local_stage_area");
			if (!jobsLocalFolder.exists()) {
				jobsLocalFolder.mkdir();
			}
			localWorkingDir = new File(jobsLocalFolder, jobConfig.getUserName() + "_" + new Date().getTime());
			if(!localWorkingDir.exists()) {
			  localWorkingDir.mkdir();
			}
			setlocalWorkingDir(localWorkingDir);
		
		} else {
		  localWorkingDir.mkdirs(); // make sure this directory exists
		}
		//Default handler doesn't do anything else. 
		//Overriding classes can use this space to do any local preprocessing
		//or add any constant files etc. 
		jobConfig.setLocalWorkingDir(localWorkingDir);
		return localWorkingDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.pnnl.velo.tif.model.JobHandler#isSelfPolling()
	 */
	@Override
	public boolean isSelfPolling() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public String getSubmitCommandOptions(JobConfig launchConfig){
	  return "";
	}

}
