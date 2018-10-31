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
 */

package org.kepler.job;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Support class for Slurm job manager support used on Olympus.
 *  Class Job uses the methods of a supporter class to
 *  submit jobs and check status
 *
 * History: Copied from JobSubmitMoab and modified.
 * @version $Revision: 1.0 $
 */
public class JobSupportSlurm implements JobSupport
{

    private static final Log log = LogFactory.getLog( JobSupportSlurm.class.getName() );
    private static final boolean isDebugging = true;//log.isDebugEnabled();
    private String _slurmSubmitCmd="sbatch ";
    private String _slurmStatusCmd="squeue -h -o %T -j "; // to be followed by jobid
    private String _slurmDeleteCmd="scancel "; // to be followed by jobid
    private String _slurmTaskStatusCmd="squeue -h -as -o %i | grep ";
    public JobSupportSlurm()
    {
    }


    /**
     * Method init.
     * @param slurmBinPath String
     * @see org.kepler.job.JobSupport#init(String)
     */
    public void init( String slurmBinPath )
    {
       if ( slurmBinPath != null && !slurmBinPath.trim().equals("") )  {
          String binPath = new String(slurmBinPath);
          if ( ! slurmBinPath.endsWith("/") )
             binPath += "/";
          _slurmSubmitCmd = binPath + _slurmSubmitCmd;
          _slurmStatusCmd = binPath + _slurmStatusCmd;
          _slurmDeleteCmd = binPath + _slurmDeleteCmd;
       }
    }

    /** Create a submission file for the specific job manager,
     *  based on the information available in Job:
     *   - executable name
     *   - input files
     *   - output files
     *   - arguments for the job
     * @param filename String
     * @param job Job
     * @return boolean
     * @see org.kepler.job.JobSupport#createSubmitFile(String, Job)
     */
    public boolean createSubmitFile ( String filename, Job job )
    {
       return false;
    }



    /** Submit command for Moab
     *   return: the command for submission
     * @param submitFile String
     * @param options String
     * @return String
     * @see org.kepler.job.JobSupport#getSubmitCmd(String, String)
     */
    public String getSubmitCmd ( String submitFile, String options )
    {
       String _commandStr;
       if (options != null)
          _commandStr = _slurmSubmitCmd + " " + options + " " + submitFile;
       else
          _commandStr = _slurmSubmitCmd + " " + submitFile;

       return _commandStr;
    }


    /** Parse output of submission and get information: jobID
     *  return String jobID on success
     *  throws JobException at failure (will contain the error stream or output stream)
     * @param output String
     * @param error String
     * @return String
     * @throws JobException
     * @see org.kepler.job.JobSupport#parseSubmitOutput(String, String)
     */
    public String parseSubmitOutput (
          String output,
          String error ) throws JobException
    {

       // For successful submissions, output:
       //Submitted batch job 334340
       String jobID = null;
       Pattern pattern = Pattern.compile("Submitted batch job ([0-9]+).*");

       String lines[] = output.split("\n");
       for (int idx=0; idx<lines.length; idx++) {
    	   Matcher matcher = pattern.matcher(lines[idx]);
          if (matcher.matches()) {
             jobID = matcher.group(1);
             break;
          }
       }

       if (isDebugging) {
          log.debug("Slurm submit output: "+output);
          log.debug("Slurm jobID = " + jobID);
       }

       if (jobID == null) {
          if (error != null && error.length() > 0)
             throw new JobException("Error submitting Moab job: " + error);
          else
             throw new JobException("Error submitting Moab job: " + output);
       }
       return jobID;
    }


    /** Get the command to ask the status of the job
     *   return: the String of command
     * @param jobID String
     * @return String
     * @see org.kepler.job.JobSupport#getStatusCmd(String)
     */
    public String getStatusCmd (String jobID)
    {
       return _slurmStatusCmd + jobID;
    }

    /** Get the command to ask the status of each task
     *   return: the String of command
     * @param jobID String
     * @return String
     * @see org.kepler.job.JobSupport#getTaskStatusCmd(String)
     */
    public String getTaskStatusCmd (String jobID)
    {
    	return getStatusCmd(jobID) + ";" + _slurmTaskStatusCmd + jobID;
    }

    /**
     * Parse output of status check command and get status info
    
     * @param jobID String
     * @param exitCode int
     * @param output String
     * @param error String
     * @return TaskParallelJobStatusInfo
     * @throws JobException
     * @see org.kepler.job.JobSupport#parseStatusOutput(String, int, String, String)
     */
    public TaskParallelJobStatusInfo parseStatusOutput (
        String jobID,
        int exitCode,
        String output,
        String error )  throws JobException
    {
       // Output should be a single word indicating the status.
       // If the job doesn't exist, the output will be empty
       // The known values include:
       //
       // wait statuses
       // PENDING
       // SUSPENDED
       // CONFIGURING
       //
       // running statuses
       // RUNNING
       // COMPLETING
       //
       // not in queue
       // CANCELLED
       // COMPLETED
       // FAILED
       // TIMEOUT
       // NODE_FAIL

       //System.out.println("output: " + output);
    	
       TaskParallelJobStatusInfo stat = new TaskParallelJobStatusInfo();
       stat.statusCode = JobStatusCode.NotInQueue;
       stat.jobID = jobID;

       boolean foundStatus = false;
       if (output.length() > 0) {
          output = output.split("\n")[0];
          if (output.equals("PENDING") ||
              output.equals("SUSPENDED") ||
              output.equals("CONFIGURING")) {
             foundStatus = true;
             stat.statusCode = JobStatusCode.Wait;
          } else if (output.equals("RUNNING") ||
        		     output.equals("COMPLETING")) {
             foundStatus = true;
             stat.statusCode = JobStatusCode.Running;
          } else if (output.equals("CANCELLED") ||
  		  	 	     output.equals("COMPLETED") ||
 		  	 	     output.equals("FAILED") ||
     		  	 	 output.equals("TIMEOUT") ||
     		  	 	 output.equals("NODE_FAIL") ) {
             // Note sure - leave it at not in queue?
             foundStatus = true;
             stat.statusCode = JobStatusCode.NotInQueue;
          } else {
             foundStatus = true;
             stat.statusCode = JobStatusCode.Wait;
          }
       } else {
          stat.statusCode = JobStatusCode.NotInQueue;
       }
       
//       System.out.println("JobStatusCode: " + stat.statusCode);

       if (!foundStatus) {
          // May want to look at err string or something here
       }

       return stat;
    }



    /**
     * Parse output of task status check command and get status info
    
     * @param jobID String
     * @param numTasks int
     * @param exitCode int
     * @param output String
     * @param error String
     * @return TaskParallelJobStatusInfo
     * @throws JobException
     * @see org.kepler.job.JobSupport#parseTaskStatusOutput(String, int, int, String, String)
     */
    public TaskParallelJobStatusInfo parseTaskStatusOutput (
        String jobID,
        int numTasks,
        int exitCode,
        String output,
        String error )  throws JobException
    {
    	String[] lines = output.split("\n");

        TaskParallelJobStatusInfo jobStatus = 
     	   (TaskParallelJobStatusInfo)parseStatusOutput (jobID, exitCode, lines[0], error);

        jobStatus.taskStatusCodes = new HashMap<String,JobStatusCode>(numTasks);

        /*
         * if(code == JobStatusCode.Running){
 					// this is the only unambiguous state so record it
 					
 				} else if(oldCode == null) {
 					result.put(taskId,JobStatusCode.Wait);
 				} else if(oldCode == JobStatusCode.Running && 
 					code == JobStatusCode.NotInQueue) {
 					result.put(taskId,JobStatusCode.NotInQueue);
 				} else if(code == JobStatusCode.Running) {
 					result.put(taskId,JobStatusCode.Running);
 				}
         */
        if( jobStatus.statusCode == JobStatusCode.Running ) {
          for (int idx=1; idx<lines.length; idx++) {

             Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)");
             Matcher matcher = pattern.matcher(lines[idx]);
             if (matcher.matches()) {
                String jobid = matcher.group(1);
                if( jobid.equals(jobID) ) {
                   String taskId = matcher.group(2);
                   jobStatus.taskStatusCodes.put(taskId,JobStatusCode.Running);
                }
             }
          }
          for( int idx = 0; idx < numTasks; idx++ ) {
             if(! jobStatus.taskStatusCodes.containsKey(Integer.toString(idx))) {
             	jobStatus.taskStatusCodes.put(Integer.toString(idx),JobStatusCode.NotInQueue);
             }
          }
        } else {
          for( int idx = 0; idx < numTasks; idx++ ) {
         	 jobStatus.taskStatusCodes.put(Integer.toString(idx),jobStatus.statusCode) ;
          }
        }
        
        return jobStatus;
    }


    /**
    
     * @param jobID String
     * @return String
     * @see org.kepler.job.JobSupport#getDeleteCmd(String)
     */
    public String getDeleteCmd (String jobID)
    {
       return  _slurmDeleteCmd + jobID;
    }


    /**
     * Parse output of delete command.
    
     * @param jobID String
     * @param exitCode int
     * @param output String
     * @param error String
     * @return boolean
     * @throws JobException
     * @see org.kepler.job.JobSupport#parseDeleteOutput(String, int, String, String)
     */
    public boolean parseDeleteOutput( String jobID,
          int exitCode,
          String output,
          String error ) throws JobException
    {
       if (exitCode == 0)
          return true;
       else
          return false;
    }

}
