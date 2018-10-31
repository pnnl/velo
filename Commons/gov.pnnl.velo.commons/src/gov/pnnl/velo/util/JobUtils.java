package gov.pnnl.velo.util;

import gov.pnnl.velo.tif.model.Fileset;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.service.TifServiceLocator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kepler.job.JobException;
import org.kepler.ssh.ExecException;
import org.kepler.ssh.ExecInterface;
import org.kepler.ssh.LocalExec;
import org.kepler.ssh.RemoteExec;
import org.kepler.ssh.SshExec;

public class JobUtils {
	private static Log logger = LogFactory.getLog(JobUtils.class);
	public static boolean CACHE_SSH_SESSION = true;
	
	public static ExecInterface getDefaultExecObject(JobConfig jobConfig) {
		  Machine machine = jobConfig.getMachine();
		  if (isLocalhost(machine)) {
	        return new LocalExec();

	      } else {
	        String machineDomainName = machine.getFullDomainName();
	        String username = jobConfig.getUserName();
	            
	        logger.debug("Creating sshexec for machine "
	            + machineDomainName + " and user " + username);
	        //test for environment var here
	        return new SshExec(username, machineDomainName, CACHE_SSH_SESSION);
	      }
	}

	public static boolean isLocalhost(Machine machine) {

		String fullDomainName = machine.getFullDomainName();
		if (fullDomainName.trim().equals("")
				|| fullDomainName.equals("localhost")
				|| fullDomainName.equals("local")) {

			return true;

		}
		
		String hostname = "";
		try {
			java.net.InetAddress localMachine = java.net.InetAddress
					.getLocalHost();
			hostname = localMachine.getHostName();
		} catch (java.net.UnknownHostException uhe) {
			throw new RuntimeException(uhe);
		}

		if (hostname.equals(machine.getFullDomainName())) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void writeForkMgr(String path) throws JobException, IOException {
	    logger.debug("In writeForkMgr. Script registry:" + TifServiceLocator.getScriptRegistry().getClass().getName());
	    int bytesCopied = FileUtils.copy(TifServiceLocator.getScriptRegistry().getForkScript(), new File(path));
	    logger.debug("writeForkMgr - bytes written " + bytesCopied);
	}
	
	public static void writeJobScript(JobConfig jobConfig, String jobScriptPath) throws JobException, IOException {
	    logger.debug("In writeForkMgr. Script registry:" + TifServiceLocator.getScriptRegistry().getClass().getName());
	    
	    File jobScriptTemplate = TifServiceLocator.getScriptRegistry().getJobScriptTemplate(jobConfig.getCodeId(),
	    																jobConfig.getCodeVersion(), jobConfig.getMachineId());
	    TifServiceLocator.getScriptRegistry().createJobScript(jobScriptTemplate, new File(jobScriptPath), jobConfig);
	}
	
	public static void extractFilesAndLinks(List<Fileset> outputs,
      ArrayList<String> files, ArrayList<String> links, String state) {
	  ArrayList<String> templist;
    for(Fileset output : outputs) {
      // TODO:  for now we aren't using excludes
      if(output.isCopy()){
        templist = files;
      }else{
        templist = links;
      }
      
      if(output.isValidExitState(state)) {
        if(output.getDir().equals(".")){
          templist.addAll(output.getIncludesList());
        }else{
          String dir = output.getDir().replaceFirst("./", "");
          if(!dir.endsWith("/"))
            dir = dir + "/";
          for(String includePattern: output.getIncludesList()){
            templist.add(dir+includePattern);
          }
        }
      }
    }
  }
	
  public static void makeExecutable(ExecInterface execObject, String path) throws JobException {
	    ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
	    ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
	    String cmd = "chmod a+x " + path;
	    int exitCode = executeCommand(execObject, cmd, commandStdout, commandStderr);
	    logger.debug("******Made " + path
	        + " executable. Command used - " + cmd);
	    if (exitCode != 0) {
	      throw new JobException("Error at job submission. Stdout:\n"
	          + commandStdout + "\nStderr:\n" + commandStderr);
	    }
  }
  
  public static void executeScript(ExecInterface execObject, String scriptPath) {
    // make the rename script executable
   
    JobUtils.makeExecutable(execObject,scriptPath);

    // execute the script
    ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
    ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
    String cmd = scriptPath;
    int exitCode = executeCommand(execObject, cmd, commandStdout, commandStderr);

    if (exitCode != 0) {
      throw new JobException("Error at job submission. Stdout:\n"
          + commandStdout + "\nStderr:\n" + commandStderr);
    }
    
  }
  
  private static int executeCommand(ExecInterface execObject, String commandStr, ByteArrayOutputStream commandStdout,
      ByteArrayOutputStream commandStderr) throws JobException {

    int exitCode = 0;
    try {

      // if (isDebugging) log.debug("Execute on " + user + "@" + host +
      // ": " + commandStr);
      exitCode = execObject.executeCmd(commandStr, commandStdout,
          commandStderr);

    } catch (ExecException e) {
      
      String host = "localhost";
      String user = System.getProperty("user.name"); ;
      if(execObject instanceof RemoteExec){
        user= ((RemoteExec) execObject).getUser();
        host = ((RemoteExec) execObject).getHost();
      }
      throw new JobException("Jobmanager._exec: Error at execution on Machine id:" + host 
          + " as user:" + user + "  command: " + commandStr + "\n" + e, e);
    }

    return exitCode;
  }

}
