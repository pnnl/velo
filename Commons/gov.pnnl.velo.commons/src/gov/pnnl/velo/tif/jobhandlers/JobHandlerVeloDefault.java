package gov.pnnl.velo.tif.jobhandlers;

import gov.pnnl.velo.exception.JobConfigNotSetException;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.model.Fileset;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.JobHandler;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.service.CmsService;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.util.JobUtils;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloTifConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kepler.job.JobException;
import org.kepler.job.JobStatusInfo;
import org.kepler.job.JobSupport;
import org.kepler.ssh.ExecException;
import org.kepler.ssh.ExecInterface;
import org.kepler.util.FilenameFilter_RegularPattern;

public abstract class JobHandlerVeloDefault implements JobHandler {

  protected JobConfig jobConfig;
  protected ExecInterface execObject;

  protected Machine machine;
  protected Code code;
  protected CmsService cmsService;
  protected File localWorkingDir;
  //local dir where outputs are temporarily downloaded before uploading to velo
  protected File localJobOutdir;
  //dir on velo where job outputs should be saved
  protected String jobOutputdir; 
  
  protected boolean recordruns;
  protected Log logger = LogFactory.getLog(this.getClass());
  protected String jobExitState;
  protected String jobTimeZoneID = TimeZone.getDefault().getID();
  protected String job_date_format =  "EEE MMM dd HH:mm:ss yyyy z";
  SimpleDateFormat date_formatter = new SimpleDateFormat(job_date_format);
  
  @Override
  public ExecInterface getExecObject() throws JobConfigNotSetException {

    if (this.jobConfig == null)
      throw new JobConfigNotSetException("No jobConfig object set. "
          + "Call setJobConfig() before calling getExecObject()");
    if (this.execObject == null)
      this.execObject = JobUtils.getDefaultExecObject(jobConfig);
    return this.execObject;
  }

  @Override
  public void setJobConfig(JobConfig jobConfig) {

    this.jobConfig = jobConfig;
    initializeVariables();
  }

  protected void initializeVariables() {

//    machine = TifServiceLocator.getMachineRegistry().get(jobConfig.getMachineId());
//    code = TifServiceLocator.getCodeRegistry().get(jobConfig.getCodeId(), jobConfig.getCodeVersion());
//    int index = machine.getCodes().indexOf(code);
//    Code mcode = null;
//    if(index!=-1)
//     mcode = machine.getCodes().get(index);
//    if(mcode!=null){
//      code = mcode; //Use code from machine if it exists
//    }
    //Use code and machine object in jobconfig. This handler could be running on client or server
    //If initial code object was loaded using config on client, we don't want to replace that
    //based on config on server. 
    machine = jobConfig.getMachine();
    code = jobConfig.getCode();
    cmsService = TifServiceLocator.getCmsService();
    //if command to be run is not loaded into jobConfig by UI or calling code.. default it to what is code from the merged code
    if(jobConfig.getCommand()==null || jobConfig.getCommand().trim().isEmpty()){
      jobConfig.setCommand(code.getJobLaunching().getCommand());
    }
  }
  
 public void setlocalWorkingDir (File localWorkingDir) {
   this.localWorkingDir = localWorkingDir;
   
   this.localJobOutdir = new File(localWorkingDir, "Outputs");
   this.localJobOutdir.mkdir();
   // create rundir under output if we need to capture run specific outputs
   if(this.code!= null && this.code.getJobLaunching() !=null 
       && this.code.getJobLaunching().getEnsembleConfiguration() !=null
       && this.code.getJobLaunching().getEnsembleConfiguration().isRecordRuns()){
     (new File(localJobOutdir, "rundir")).mkdir();
   }
 }

 /**
  * @return the localWorkingDir
  */
 public File getLocalWorkingDir() {
   return localWorkingDir;
 }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.model.JobHandler#getRemoteJobDirName(gov.pnnl.velo.tif.model.JobConfig)
   */
  @Override
  public String getRemoteJobDirName(JobConfig jobConfig) {
   
    //create unique sub directory to run the job in
    // based on the name of the tool instance folder
    SimpleDateFormat sdf = new SimpleDateFormat("MMddyy_HHmmss");
    String launchtime = sdf.format(new Date());
    String runName = new CmsPath(jobConfig.getContextPath()).getName();
    return  runName + "_" + launchtime;
    
  }

  @Override
  public JobConfig getJobConfig() {

    return jobConfig;
  }
  
  public String getJobOutputdir() {
    return jobOutputdir;
  }

  public void setJobOutputdir(String jobOutputdir) {
    this.jobOutputdir = jobOutputdir;
  }

  @Override
  public JobStatusInfo queryJobStatus(String jobID, JobSupport jobSupport) throws JobException {

    // query job status from queue
    JobStatusInfo stat;
    if (jobID == null) {
      throw new JobException("JobManager.status() called with null argument");
    }

    String commandStr = jobSupport.getStatusCmd(jobID);
    int exitCode = 0;

    if (commandStr == null || commandStr.trim().equals("")) {
      throw new JobException("Supporter class could not give back meaningful"
          + "command to check the status of your job");
    }

    ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
    ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();

    exitCode = execObject.executeCmd(commandStr, commandStdout, commandStderr);
    stat = jobSupport.parseStatusOutput(jobID, exitCode, commandStdout.toString(), commandStderr.toString());

    return stat;
  }

  /**
   * stage inputs from velo server. Default implementation assumes client side
   * job monitoring and assume remote server not accessible from velo server and hence
   * downloads files locally and then scp-s files to remote server. 
   * TODO - check local cache before downloading file from server. Once downloaded
   * add file to cache
   */
  
  @Override
  public void stageVeloServerInputs(List<Fileset> veloServerInputs, List<Fileset> jobLocalInputs) throws JobException {

    // TODO In future may be we can use the accessibleFromVeloServer
    // param of machine config and if it is set to true try to transfer
    // files directly from velo server to remote job server

    // For now downloading files to local file system and adding
    // to localInputs list.
    // JobManager always calls stageVeloServerInputs before stageLocalInputs
    try {
      Machine machine = jobConfig.getMachine();
      boolean remoteJob = !JobUtils.isLocalhost(machine); // check if job

      File tempDownloadDir = new File(localWorkingDir.getAbsolutePath(), "downloaded_inputs");
      boolean mkdir_result = tempDownloadDir.mkdir();
      logger.debug("Create downloaded_inputs (" +  tempDownloadDir.getAbsolutePath() + ") mkdir result:"+ mkdir_result);
      List<File> filesToScp =  new ArrayList<File>();
      boolean foundMatchingFiles = false;

      // <fileset dir="." includes="*" excludes=".*.xml,.akuna_inputs"/>
      for (Fileset fs : veloServerInputs) {

        // 1. First find the directory
        String dir = fs.getDirPath(jobConfig.getContextPath());
        List<Resource> childResources = cmsService.getChildren(dir);
        for (Resource resource : childResources) {
          String childName = resource.getName();
          // 3. Check if it is a file to be excluded
          // Check exclude pattern first as that might most often
          // contains specific names,
          // or at least lesser pattern to match
          boolean include = true;
          for (int i = 0; i < fs.getExcludesList().size() && include; i++) {
            if (childName.matches(fs.getExcludesList().get(i))) {
              include = false;
            }
          }
          if (!include) {
            continue; // skip to the next child
          }
          // 4. If not check if it conforms to the include pattern
          for (String includePattern : fs.getIncludesList()) {
            if(includePattern.equals("*")) {// * is not a valid java regex
              if(getResourceToLocalDir(resource,tempDownloadDir.getAbsolutePath(),true)){
                foundMatchingFiles = true;
              }
            } else if (childName.matches(includePattern)){
              if(getResourceToLocalDir(resource,tempDownloadDir.getAbsolutePath(),true)){
                foundMatchingFiles = true;
              }
            } 
            
          }// end of loop through includes list for this fs
        }// end of looping through this filset dir's children
      }// completed processing all fileset elements
      
      if(stageDynamicInputFiles(tempDownloadDir)){
        foundMatchingFiles=true;
      }
      if(foundMatchingFiles)
        if (remoteJob) {    
          filesToScp.add(new File(tempDownloadDir,"*"));
          execObject.copyTo(filesToScp, jobConfig.getRemoteDir(), true);
        }else{
          FileUtils.copyDirectory(tempDownloadDir, new File(jobConfig.getRemoteDir()), false);
        }
      FileUtils.deleteQuietly(tempDownloadDir);
      
    } catch (Exception e) {
      e.printStackTrace();
      throw new JobException("Exception staging velo server inputs: " + e.getMessage(), e);
    }
  }
  
  /**
   * stages inputs files directly added to jobConfig object. This happens after staging files
   * configured in code registry xml. 
   * @param renameScript
   * @param filesToRemoteCopy
   * @param remoteJob true if velo server is different from compute server
   * @return true if one or more files were staged successfully. false otherwise
   * @throws Exception
   */
   private boolean stageDynamicInputFiles(File tempDownloadDir) throws Exception {
      
     List<String> jobInputFiles = jobConfig.getDynamicInputFiles();
     if(jobInputFiles == null || jobInputFiles.size()==0){
       return false;
     }
     boolean found = false;
     for(String path : jobInputFiles) {
       if(path != null && !path.trim().isEmpty()) {
         cmsService.getFile(path, tempDownloadDir.getAbsolutePath());
         found=true;
       }
     }
     return found;
   }
  
  @Override
  public void stageRemoteInputs(List<Fileset> remoteInputs) throws JobException, ExecException {

    ByteArrayOutputStream commandStdout;
    ByteArrayOutputStream commandStderr;
    List<String> rfiles = new ArrayList<String>();
    String tempDir = jobConfig.getRemoteDir() + "/" + "velo_fileset";
    boolean cleanTempDir = false;

    for (Fileset fs : remoteInputs) {
      if (fs.getExcludesList().isEmpty()) {
        // life is simple - just add the files to rfiles list
        rfiles.addAll(fs.getIncludesList());
      } else {
        // we have to copy included files in separate dir and delete
        // file in exclude list
        // and then move the remaining files in job directory
        // have to do these one fileset at a time since two file set's
        // include list might include
        // say a certain file test.xml but one of them might exclude it.
        // Inorder to grab the
        // right test.xml we should not just copy all the files in fs
        // includes and delete all
        // excludes

        // cmd to copy included files
        // typically max cmd lenght is around 262144 bytes.
        // ascii characters only take 1 byte per character
        // so assuming that the end cmd string run will be smaller than
        // 262144 bytes and hence execute in one go
        String inputDir = fs.getDirPath(jobConfig.getContextPath());
        StringBuffer cmd = new StringBuffer("mkdir -p" + tempDir + "; cd " + inputDir + "; ");
        cmd.append(fs.getIncludes().replaceAll(",", " "));
        cmd.append(tempDir);
        cmd.append(";cd ");
        cmd.append(tempDir);
        // now get the exclude list and delete them form the tempDir
        cmd.append("; rm -rf ");
        cmd.append(fs.getExcludes().replaceAll(",", " "));
        // finally move the files from tempDir to remoteDir
        cmd.append("; mv * ./..");
        logger.debug("Command to copy remote files into tempDir and remove excludeList files: " + cmd);
        cleanTempDir = true;
        commandStdout = new ByteArrayOutputStream();
        commandStderr = new ByteArrayOutputStream();
        int exitCode = execObject.executeCmd(new String(cmd), commandStdout, commandStderr);
        logger.debug("Remote copy: exitcode " + exitCode);
        if (exitCode != 0) {
          throw new JobException("Error at copying remote files into the job directory. " + "Stdout:\n" + commandStdout
              + "\nStderr:\n" + commandStderr);
        }
      }
    }

    logger.debug("Staging remote files" + rfiles.toString());
    int exitCode;
    if (rfiles.size() > 0) {
      StringBuffer cmd = new StringBuffer("cp -r ");
      for (String filename : rfiles) {
        cmd = cmd.append(filename);
        cmd = cmd.append(" ");
      } // end of for loop
      cmd = cmd.append(jobConfig.getRemoteDir());

      if (cleanTempDir) {
        cmd.append("; rmdir " + tempDir);
      }
      logger.debug("Remote file copy command: " + cmd);
      // if (isDebugging) log.debug("Remote file copy command: " + cmd);
      commandStdout = new ByteArrayOutputStream();
      commandStderr = new ByteArrayOutputStream();
      exitCode = execObject.executeCmd(new String(cmd), commandStdout, commandStderr);
      logger.debug("Remote copy: exitcode " + exitCode);
      if (exitCode != 0) {
        throw new JobException("Error at copying remote files into the job directory. " + "Stdout:\n" + commandStdout
            + "\nStderr:\n" + commandStderr);
      }
    }

  }
  
  @Override
  public void stageLocalInputs(List<Fileset> localInputs) throws ExecException {
    logger.debug("stagging local inputs");
    List<File> files =  new ArrayList<File>();
    for(Fileset fs: localInputs){
      files.addAll(fs.findMatchingFiles(jobConfig.getLocalWorkingDir()));
    }
    logger.debug("matching files - " + files);
    int count = execObject.copyTo(files, jobConfig.getRemoteDir(), true);
    logger.debug("copied "+ count + " of " + files.size()+ " to " + jobConfig.getRemoteDir());
  }

  @Override
  public void jobBeforeSubmit() throws JobException, ExecException {

    ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
    ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
    int exitCode = execObject.executeCmd("date +%z", commandStdout, commandStderr);
    
    if (exitCode == 0) {
      logger.debug("Compute server timezone:" + commandStdout);
      logger.debug("Compute server timezone:" + commandStderr);
      // output of command would be offset from GMT
      String tz = commandStdout.toString().trim();
      jobTimeZoneID = "GMT"+ tz.substring(0, 3) + ":" + tz.substring(3);
    }
   
    jobOutputdir = jobConfig.getContextPath() + "Outputs";
    cmsService.createFolder(jobConfig.getContextPath(), "Outputs");

    // initialize contextNode before calling recordJobStatus for the first
    // time
    cmsService.removeProperties(jobConfig.getContextPath(), Arrays.asList(VeloTifConstants.JOB_PROPERTIES));
    recordJobStatus(VeloTifConstants.STATUS_SUBMITTING);
  }

  
  @Override
  public void jobSubmitted(JobStatusInfo status) throws JobException, ExecException {

    setJobSubmitProperties(status);
    jobsMonitored.put(jobConfig.getContextPath(),VeloTifConstants.STATUS_WAIT);
  }
  
  @Override
  public void jobStarted(JobStatusInfo status) throws JobException, ExecException {

    setJobStartProperties(status);
    jobsMonitored.put(jobConfig.getContextPath(),VeloTifConstants.STATUS_START);
  }
  
  @Override
  public void jobRunning(JobStatusInfo status) throws JobException, ExecException {

    logger.debug("In JobHandlerVeloServerDefault  Job Running");
    logger.debug("In JobHandlerVeloServerDefault  Job Running");

  }

  @Override
  public void jobComplete(JobStatusInfo status) throws JobException, ExecException {
    cmsService.setProperty(jobConfig.getContextPath(), VeloTifConstants.JOB_STATUS, VeloTifConstants.STATUS_POSTPROCESS);
    jobsMonitored.put(jobConfig.getContextPath(),VeloTifConstants.STATUS_POSTPROCESS);
    this.jobExitState = readJobExitState();
    runPostProcess(status);
    uploadJobOutputs(jobExitState, jobConfig.getContextPath() + "/Outputs");
    setJobCompleteProperties(status);
    jobsMonitored.remove(jobConfig.getContextPath());
  }

  @Override
  public void jobTerminated(JobStatusInfo status) throws JobException, ExecException {
    // make sure the killed status is 
    recursiveSetStatusProperty(jobConfig.getContextPath(), VeloTifConstants.STATUS_KILLED);
    //Don't call jobsMonitored.remove here. This method is only called when there is an explicit job kill request from
    //user. We kill the job in queue and set the status as killed but the job monitoring thread will
    //eventually poll and find that the job is not in queue and then process outputs and then remove
    //job from the  monitored jobs list
  }
  
  //TODO: This performance is terrible on the server because cmsService is wrapped in tx interceptor,
  // and we are creating a new tx for every tiny call.  We need to add to cmsService the ability to start
  // and end a tx
  private void recursiveSetStatusProperty(String node, String status) {
    String curStatus = cmsService.getProperty(node, VeloTifConstants.JOB_STATUS);
    if(curStatus != null) {
      // if we have a status and the status is not success or error, then update status
      if(!curStatus.equals(VeloTifConstants.STATUS_SUCCESS) && !curStatus.equals(VeloTifConstants.STATUS_ERROR)) {
        cmsService.setProperty(node, VeloTifConstants.JOB_STATUS, status);
      }
    } else if(cmsService.isFolder(node)) {
      for(Resource resource : cmsService.getChildren(node)) {
        recursiveSetStatusProperty(resource.getPath(), status);
      }
    }
  }
  

  protected boolean getResourceToLocalDir(Resource resource, String localDir,boolean recursive) {
    if (resource.getType().equals(VeloConstants.TYPE_FILE)) {
      cmsService.getFile(resource.getPath(), localDir);
    }else if (resource.getType().equals(VeloConstants.TYPE_FOLDER)){
      cmsService.getFolder(resource.getPath(),localDir,recursive);
    }else{
      return false;
    }
    return true;
  }
  
  /**
   * @param workdirpath
   * @return
   */
  protected String readJobExitState() {

    logger.debug("Querying .status file");
    ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
    ByteArrayOutputStream streamErr = new ByteArrayOutputStream();
    int exitcode = -1;
    String jobstatus = VeloTifConstants.STATUS_ERROR;

    try {
      exitcode = execObject.executeCmd("cat " + jobConfig.getRemoteDir() + "/.status", streamOut, streamErr);
      if (exitcode == 0) {
        logger.debug("Got status file");
        if (Integer.parseInt(streamOut.toString().trim()) == 0) {
          jobstatus = VeloTifConstants.STATUS_SUCCESS;
        } // else job status is error.
      } else {
        // if there is no status file - job must have got terminated
        // we assume that all job script have the last line as
        // "echo $? >.status"
        // all default velo job templates do
        logger.warn("Unable to read .status file. exit code is non zero."
            + "Job is not in queue, so assume job status as terminated");
        jobstatus = VeloTifConstants.STATUS_KILLED;
      }
      logger.debug("Status is " + jobstatus);
    } catch (Exception e) {
      // Unable to read .status. So job must have got killed either by user
      // or by queue
      if(logger.isDebugEnabled()) {
        logger.error("Failed to read status file.", e);
      }
      logger.warn("Unable to read .status file. Job is not in queue, " + "so assume job status as terminated");
      jobstatus = VeloTifConstants.STATUS_KILLED;
    }
    return jobstatus;
  }



  /**
   * initializes all the job properties
   * 
   * @param status
   *          JobStatusInfo
   */
  protected void setJobSubmitProperties(JobStatusInfo status) {

    Map<String, String> properties = new HashMap<String, String>();
    properties.put(VeloTifConstants.JOB_STATUS, VeloTifConstants.STATUS_WAIT);
    properties.put(VeloTifConstants.JOB_SUBMIT_TIME, status.submissionTime);
    properties.put(VeloTifConstants.JOB_CODEID, jobConfig.getCode().getIdAndVersion());
    properties.put(VeloTifConstants.JOB_JOBID, status.jobID);
    properties.put(VeloTifConstants.JOB_USER, jobConfig.getUserName());
    if (machine.getScheduler().isAllocation()) {
      properties.put(VeloTifConstants.JOB_ACCOUNT, jobConfig.getAccount());
    }
    properties.put(VeloTifConstants.JOB_MACHINE, machine.getFullDomainName());
    properties.put(VeloTifConstants.JOB_PROC_COUNT, String.valueOf(jobConfig.getProcessors()));
    properties.put(VeloTifConstants.JOB_NODE_COUNT, String.valueOf(jobConfig.getNodes()));
    if (jobConfig.getTimeLimit() != null) {
      properties.put(VeloTifConstants.JOB_TIME_LIMIT, jobConfig.getTimeLimit());
    }
    if (jobConfig.getQueueName() != null) {
      properties.put(VeloTifConstants.JOB_QUEUE, jobConfig.getQueueName());
    }
    properties.put(VeloTifConstants.JOB_POLL_INTERVAL, String.valueOf(jobConfig.getPollingInterval()));
    properties.put(VeloTifConstants.JOB_RUNDIR, jobConfig.getRemoteDir());

    cmsService.setProperties(jobConfig.getContextPath(), properties);
  }

  /**
   * Method setJobStartProperties.
   * 
   * @param status
   *          JobStatusInfo
   */
  protected void setJobStartProperties(JobStatusInfo status) {

    Map<String, String> properties = new HashMap<String, String>();
    date_formatter.setTimeZone(TimeZone.getTimeZone(jobTimeZoneID));
    properties.put(VeloTifConstants.JOB_START_TIME,date_formatter.format(new Date()));
    properties.put(VeloTifConstants.JOB_STATUS, VeloTifConstants.STATUS_START);
    cmsService.setProperties(jobConfig.getContextPath(), properties);
  }

  protected void setJobCompleteProperties(JobStatusInfo status) {

    Map<String, String> properties = new HashMap<String, String>();
    date_formatter.setTimeZone(TimeZone.getTimeZone(jobTimeZoneID));
    properties.put(VeloTifConstants.JOB_STOP_TIME, date_formatter.format(new Date()));
    properties.put(VeloTifConstants.JOB_STATUS, readJobExitState());
    cmsService.setProperties(jobConfig.getContextPath(), properties);
  }

  @Override
  public void recordJobStatus(String jobstatus) {

    try {
      if (jobstatus != null) {
        cmsService.setProperty(jobConfig.getContextPath(), VeloTifConstants.JOB_STATUS, jobstatus);
      }
      
    } catch (Throwable e) {
      e.printStackTrace();
      // do not puke job monitoring if we can't write a property
    }
  }
  
  protected void runPostProcess(JobStatusInfo status) {
    //Default is empty. 
    //code specific handlers  can overrid this method
    //For example stomp handler calls scripts to 
    //convert plot to .tec files for VisIt 
  }
  
  protected void uploadJobOutputs(String exitState, String cmsOutputPath) {

    ArrayList<String> files = new ArrayList<String>();
    ArrayList<String> links = new ArrayList<String>();
    List<Fileset> outputs = code.getJobLaunching().getOutputs();
    if(jobConfig.getDynamicOutputFiles()!=null){
      outputs.addAll(jobConfig.getDynamicOutputFiles());
    }
    
    JobUtils.extractFilesAndLinks(outputs, files, links, exitState);
    if (!links.isEmpty()) {
      createRemoteLinks(cmsOutputPath, jobConfig.getRemoteDir(), links);
    }
    logger.debug("Done creating links. Now copy output files");
    // Now handle files that need to be copied
    if(JobUtils.isLocalhost(machine)){
      ArrayList<File> filesToUpload = new ArrayList<File>();
      extractLocalMatchingFiles(files, filesToUpload);
      //TODO: This is currently not recursive
      cmsService.bulkUpload(filesToUpload,jobConfig.getContextPath() + "/Outputs");
    }else {
      int count = execObject.copyFrom(jobConfig.getRemoteDir(), files, localJobOutdir, true);
      if (count > 0) {
        logger.debug("Copied job output files locally");
        cmsService.bulkUpload(localJobOutdir, jobConfig.getContextPath() + "/Outputs", false);
        
        logger.debug("Uploaded job out files to alfresco");
      } else {
        logger.debug("No job output files found");
      }
    }
  }

  private void extractLocalMatchingFiles(ArrayList<String> files, ArrayList<File> filesToUpload) {
    //convert list of file string to list of file objects and pass that to bulk upload
   //copying simply from one directory to another within a machine is waste of time and space
   String srcDirPath = jobConfig.getRemoteDir(); 
   if (!srcDirPath.endsWith(File.separator)){
     srcDirPath = srcDirPath +   File.separator ;
   }
   for(String namePattern:files){ 
     File file = new File(srcDirPath + namePattern); //because namePattern can contain subdir names
     String name = file.getName();
     if (name.indexOf("*") != -1 || name.indexOf("?") != -1) {
       String pattern = name.replaceAll("\\.", "\\\\.").replaceAll("\\*",
           ".*").replaceAll("\\?", ".");

       FilenameFilter_RegularPattern filter = new FilenameFilter_RegularPattern(
           pattern);
       String dirname = file.getParent();
       File dir = new File(dirname);
       File[] filesArray = dir.listFiles(filter);
       filesToUpload.addAll(Arrays.asList(filesArray));

     } else { // no wildcards
       filesToUpload.add(file);
     }
   } // end of for loop
  }
  
  protected void createRemoteLinks(String cmsOutputDir, String remotedir, ArrayList<String> links) throws ExecException {

    ByteArrayOutputStream streamOut;
    ByteArrayOutputStream streamErr;
    logger.debug("Configured link request: " + links.toString());
    if (links.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append("cd " + remotedir);
      //force ls to list 1 per line and print directory as files (i.e. don't print dirname:)
      sb.append(";ls -1 -d ");
      for (String link : links) {
        sb.append(link);
        sb.append(" ");
      }
      //add unique just in case the link in links parameter repeat
      sb.append(" | uniq");
      streamOut = new ByteArrayOutputStream();
      streamErr = new ByteArrayOutputStream();
      int exitcode = -1;
      logger.debug("Command is : " + sb.toString());
      exitcode = execObject.executeCmd(sb.toString(), streamOut, streamErr);
      String[] linkfiles = null;
      // ls returned valid list instead of error.
      if (exitcode == 0) {
        linkfiles = streamOut.toString().trim().split("[\n\r]");
        logger.debug("parsed link files list of length " + linkfiles.length + " : " + Arrays.toString(linkfiles));
      }
      String linkUrlPrefix = remotedir;
      if (!remotedir.endsWith("/")) {
        linkUrlPrefix = linkUrlPrefix + "/";
      }
      if (linkfiles != null && linkfiles.length > 0) {
        for (String file : linkfiles) {
          if (!file.isEmpty()) {
            //to cmsoutputdir only append the file name. result of ls could include
            //sub directory names if code config had outputs defined with non default dir name
            //for example - <fileset dir="./Outputs" includes="*" actions="link" conditions="exitState=success" />
            //in the above case file variable here would be "Outputs/filename"
            String filename = file;
            String subdir ="";
            int index = filename.lastIndexOf("/");
            if(index!=-1){
              subdir = filename.substring(0,index+1);
              filename = filename.substring(index+1);
            }
            String linkPath = cmsOutputDir + "/" + filename;
            logger.debug("Creating link for file " + linkPath);
            RemoteLink remoteLink = new RemoteLink(VeloConstants.TYPE_FILE, cmsOutputDir,  machine.getFullDomainName(), 
                linkUrlPrefix + subdir+ filename, "Link to remote job output file.", filename);
            
            cmsService.createRemoteLink(remoteLink);
          }

        }
      }
    }
  }
  
  @Override
  public void logMessage(String message) {
    if (message != null) {
      message = message.replaceAll("[\n\r\t]", " ");
      cmsService.setProperty(jobConfig.getContextPath(), VeloTifConstants.JOB_STATUS_MESSAGE, message);
    } 
  }
  
  @Override
  public boolean isSelfPolling() {
    return false;
  }
  
  @Override
  public String getSubmitCommandOptions(JobConfig launchConfig){
    return "";
  }

}
