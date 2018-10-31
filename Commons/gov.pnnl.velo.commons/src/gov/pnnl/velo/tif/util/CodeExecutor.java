package gov.pnnl.velo.tif.util;

import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.model.Parameter;
import gov.pnnl.velo.tif.service.CodeRegistry;
import gov.pnnl.velo.tif.service.TifServiceLocator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.kepler.ssh.ExecException;
import org.kepler.ssh.LocalExec;

/**
 * Executes simple codes from registry without requiring a job handler or staging/launching
 * process.
 */
public class CodeExecutor {
  public static final ArrayList<String> APPEND_TO_ENV_VARIABLES = new ArrayList<String>(
      Arrays.asList("PATH","CLASSPATH","DYLD_LIBRARY_PATH","LD_LIBRARY_PATH"));
  
  /**
   * Convenience method. Same as execute(codeID,null,localWorkingDir)
   * Executes the code represented by the given codeid on the local machine as a synchronous process. 
   * Retrieve details about code such as path, input parameters, output parameters etc
   * from the code registry. If code registry entry for the code id is null
   * @param codeID          - id based on which code details can be looked up from akuna_coderegistry.ini
   * @param commandArgs     - optional command line arguments that need to be passed to the code. This string 
   *                          is not null is appended to the command before execution. 
   * @param localWorkingDir - local directory which contains the input files for the code. 
   *                          code is executed in this directory
   * @return exitcode of the code execution. 0 for success and non zero for failure
   * @throws ExecException  - when the job execution fails with exceptions (eg. IOException, SecurityException)
   */
  public static int execute(String codeID, File localWorkingDir) throws ExecException  {
    return execute(codeID, null, localWorkingDir,false, true);
  }

  /**
   * Executes the code represented by the given codeid on the local machine as a synchronous process. 
   * Retrieve details about code such as path, input parameters, output parameters etc
   * from the code registry. If code registry entry for the code id is null
   * @param codeID          - id based on which code details can be looked up from akuna_coderegistry.ini
   * @param commandArgs     - optional command line arguments that need to be passed to the code. This string 
   *                          is not null is appended to the command before execution. 
   * @param localWorkingDir - local directory which contains the input files for the code. 
   *                          code is executed in this directory
   * @return exitcode of the code execution. 0 for success and non zero for failure
   * @throws ExecException  - when the job execution fails with exceptions (eg. IOException, SecurityException)
   */
  @SuppressWarnings("unused")
  public static int execute(String codeID, String commandArgs, File localWorkingDir) throws ExecException  {
    return execute(codeID, commandArgs, localWorkingDir, false, true);
  }

  /**
   * Convenience method. Same as execute(codeID,null,localWorkingDir)
   * Executes the code represented by the given codeid on the local machine as a synchronous process. 
   * Retrieve details about code such as path, input parameters, output parameters etc
   * from the code registry. If code registry entry for the code id is null
   * @param codeID          - id based on which code details can be looked up from akuna_coderegistry.ini
   * @param commandArgs     - optional command line arguments that need to be passed to the code. This string 
   *                          is not null is appended to the command before execution. 
   * @param localWorkingDir - local directory which contains the input files for the code. 
   *                          code is executed in this directory
   * @return exitcode of the code execution. 0 for success and non zero for failure
   * @throws ExecException  - when the job execution fails with exceptions (eg. IOException, SecurityException)
   */
  public static int execute(String codeID, File localWorkingDir,boolean backgroundProcess) throws ExecException  {
    return execute(codeID, null, localWorkingDir,backgroundProcess, true);
  }
  
  /**
   * Executes the code represented by the given codeid on the local machine as a synchronous process. 
   * Retrieve details about code such as path, input parameters, output parameters etc
   * from the code registry. If code registry entry for the code id is null
   * @param codeID          - id based on which code details can be looked up from akuna_coderegistry.ini
   * @param commandArgs     - optional command line arguments that need to be passed to the code. This string 
   *                          is not null is appended to the command before execution. 
   * @param localWorkingDir - local directory which contains the input files for the code. 
   *                          code is executed in this directory
   * @return exitcode of the code execution. 0 for success and non zero for failure
   * @throws ExecException  - when the job execution fails with exceptions (eg. IOException, SecurityException)
   */
  public static int execute(final String codeID, final Object commandArgs, final File localWorkingDir,
      boolean backgroundProcess, final boolean useWrapperShell) throws ExecException  {
    int exitCode = 0;
    
    if(!backgroundProcess) {
      exitCode = executeInternal(codeID, commandArgs, localWorkingDir, useWrapperShell);

    } else {
      Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
          executeInternal(codeID, commandArgs, localWorkingDir, useWrapperShell);        
        }
      } );
      thread.start();
    }

    return exitCode; 

  }
  
  private static Code getCodeDefinition(String codeID, File localdir) {
    //Read code registry
    //if can be executed locally use locally
    CodeRegistry cr = TifServiceLocator.getCodeRegistry();
    Code codeDef = cr.get(codeID, null);
    
    int tries = 1;
    while(tries < 10 && codeDef == null) {
      // Timing issue, this is returning null sometimes when Akuna just starts
      try {
        // To reproduce the bug, open akuna, create new model, set domain using surfaces
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
        cr = TifServiceLocator.getCodeRegistry();
        codeDef = cr.get(codeID, null);
        tries++;
    }

//    boolean remote = codeDef.get("remote_execute",boolean.class);
//    if(remote){
//      System.out.println("Code executor does not support remote execution");
//      throw new RuntimeException("Remote execution through CodeExecutor is not supported. " +
//          "Please use Job Launcher tool to launch as remote job");
//    }
    
    //Check if localWorkingDir is valid
    if(!localdir.isDirectory()){
      throw new IllegalArgumentException("Invalid local working directory " + localdir.getAbsolutePath());
    }

    return codeDef;
  }
  

  
  public static String[] getEnvironmentVariables(Code codeDef) {
    
    //    System.out.println("PATH : "
    //        + System.getProperty("java.library.path"));
    String[] updatedEnv = null;
    List<Parameter> configEnv = codeDef.getJobLaunching().getEnvParameters();
    
    if(configEnv!=null && configEnv.size() > 0) {
      List<Parameter> fixedConfigEnv = new ArrayList<Parameter>();
      CodeRegistry cr = TifServiceLocator.getCodeRegistry();

      // Replace ${exeDirPath}
      File codesFolder = new File(cr.getExeFolderPath(codeDef.getId()));

      for(Parameter parameter : configEnv) {
        String newValue = parameter.getValue().replace("${exeDirPath}", codesFolder.getAbsolutePath());
        fixedConfigEnv.add(new Parameter(parameter.getName(), newValue));
      }

      Map<String, String> newEnv = new HashMap<String, String>();
      //get default environment overwrite only the variables
      //configured. Java uses either all  the defaults env variables or if we
      //send a non null env uses only that. So this is a workaround
      Map<String, String> defaultEnv = System.getenv();
      newEnv.putAll(defaultEnv);
      for(Parameter param : fixedConfigEnv){
        
        if(APPEND_TO_ENV_VARIABLES.contains(param.getName())
            && defaultEnv.containsKey(param.getName())) {
          newEnv.put(param.getName(), param.getValue() + ":" + defaultEnv.get(param.getName()));
        } else{
          newEnv.put(param.getName(), param.getValue());
        }
      }
      updatedEnv = new String[newEnv.size()];
      int i=0;
      for (Map.Entry<String, String> entry : newEnv.entrySet())  
      {  
        updatedEnv[i] = entry.getKey() + "=" + entry.getValue();  
        i++;
      }
    }   
    return updatedEnv;
  }
  
  private static String constructCommandString(String pathToExe, String commandArgs, boolean useWrapperShell, boolean isWindows) {
    //Construct command
    final StringBuffer command = new StringBuffer();

    //enclose executable path in double quotes for OS other than windows 
    //to escape special characters
    if(!isWindows) {
      if(useWrapperShell) {
        command.append("\"");
        command.append(pathToExe); 
        command.append("\"");
      } else {
        command.append(pathToExe); 
      }

    } else {
      // On windows, using Runtime.exec to exec a cmd shell, passing the path as a parameter surrounded by
      // quotes fails (even though this works on the command line):
      // cmd.exe /C "C:\path with spaces\something.exe" args1 arg2 <-- fails

      // However we can work around this problem if we exclude the drive letter from the part of the path
      // surrounded by quotes:
      // cmd.exe /C C:"\path with spaces\something.exe" args1 arg2 <-- works      

      int colon = pathToExe.indexOf(':'); // parse off drive letter
      StringBuilder tmp = new StringBuilder(pathToExe);
      tmp.insert(colon+1, "\"");
      tmp.append("\"");
      if(useWrapperShell) {
        command.append(tmp.toString());
      } else {
        command.append(pathToExe);
      }
    
    }

    if(commandArgs!=null){
      command.append(" ");
      command.append(commandArgs);
    }
    
    return command.toString();

  }

  private static String[] constructCommandArray(String pathToExe, String[] commandArgs, boolean useWrapperShell) {
    
    //Construct command
    List<String> command = new ArrayList<String>();
    
    command.add(pathToExe);

    for(String arg : commandArgs) {
      command.add(arg);
    }
    
    String[] cmdArray = command.toArray(new String[command.size()]);
    return cmdArray;
  }
  
  public static String getPathToExecutable(String codeID) {
    CodeRegistry cr = TifServiceLocator.getCodeRegistry();
    Code code = cr.get(codeID, null);

    // if ${exeDirPath} is in the path, this means that we need to dynamically determine
    // the value of ${exeDirPath} from the plugin
    String command = code.getJobLaunching().getCommand();
    File codesFolder = new File(cr.getExeFolderPath(codeID));
    if(command.startsWith("${exeDirPath}")) {
      command = command.replace("${exeDirPath}", codesFolder.getAbsolutePath());

    } 
    
    return command;
  }

  private static void makeExecutable(String exePath) {
    try {
      // we have to make sure that executable has execute permissions
      String command =  "chmod a+x " + exePath;
      execComandWithoutWrapperShell(command, null, null);

    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private static int executeInternal(String codeID, Object commandArgs, File runDir, final boolean useWrapperShell) {
    
    // get code definition and validate inputs
    Code codeDef = getCodeDefinition(codeID, runDir);
    
    //CodeRegistry class would have already 
    //fixed any variables/references in path and executable values
    //so simply append both
    String osName = System.getProperty("os.name").toLowerCase();
    boolean isWindows = osName.contains("windows") ? true : false ;
    String pathToExe = getPathToExecutable(codeID);

    if(!isWindows) {
      // we have to make sure that executable has execute permissions
      makeExecutable(pathToExe);
    }

    Object command;
    
    if(commandArgs instanceof String) {
      command = constructCommandString(pathToExe, (String)commandArgs, useWrapperShell, isWindows);
    } else {
      command = constructCommandArray(pathToExe, (String[])commandArgs, useWrapperShell);
    }
    
    String[] envProps = getEnvironmentVariables(codeDef);
    
    int exitcode = -1;
    if(useWrapperShell) {
      // wrap shell execution can ONLY take string as command, not array
      exitcode = execCommandWithWrapperShell((String)command, runDir, envProps);        
    
    } else {
      exitcode = execComandWithoutWrapperShell(command, runDir, envProps);      
    }
    return exitcode;
  }

  private static int execCommandWithWrapperShell(String command, File runDir,  String[] envProps) {

    try {
      LocalExec exec = new LocalExec();
      ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
      ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
      System.out.println("trying to execute command: " + command);

      int exitcode = exec.executeCmd(command.toString(), commandStdout, commandStderr, null, envProps, runDir);
      processCommandOutput(exitcode, commandStdout.toString(), commandStderr.toString());

      return exitcode;

    } catch (RuntimeException e) {
      throw e;

    } catch (Throwable e) {
      throw new RuntimeException("Failed to execute command.", e);
    }

  }
  
  /**
   * @param command - can be either String or String[]
   * @param runDir
   * @param envProps
   * @return
   */
  private static int execComandWithoutWrapperShell(Object command, File runDir,  String[] envProps) {
    try {
      Runtime rt = Runtime.getRuntime();
      Process proc;
      if(command instanceof String) {
        System.out.println("executing command: " + command);      
        System.out.println("In folder: " + runDir);
        proc = rt.exec((String)command, envProps, runDir);

      } else {
        System.out.println("executing command: ");      
        for(String arg : (String[])command) {
          System.out.println(arg);
        }
        System.out.println("In folder: " + runDir);
        proc = rt.exec((String[])command, envProps, runDir);
      }

      StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
      StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());

      errorGobbler.start();
      outputGobbler.start();

      int exitVal = proc.waitFor();
      if (exitVal != 0) {
        errorGobbler.join();
      }

      // handle response
      processCommandOutput(exitVal, errorGobbler.getMessage(), outputGobbler.getMessage());
      return exitVal;

    } catch (RuntimeException e) {
      throw e;

    } catch (Throwable e) {
      throw new RuntimeException("Failed to execute command.", e);
    }

  }

  private static void processCommandOutput(int exitcode, String stdOut, String stdErr) {
    // log standard out:
    System.out.println("Command standard out: \n" + stdOut);
    System.err.println("Command standard err: \n" + stdErr);

    // we assume a non-zero exit code is an error, so alert callers
    if (exitcode != 0) {
      throw new RuntimeException("Command failed with exit code: " + exitcode + "\n" + stdErr);
    }

  }


  private static Map<String, String> getConfiguredEnv(String[] envArray) {
    Map<String,String> envMap = new HashMap<String, String>();
    for(String variable:envArray){
      String[] pair = variable.split("=");
      envMap.put(pair[0],pair[1]);
    }
    return envMap;
  }

  //  private void runBackgroundCommand(String codeID, String command, String[] env,
  //      File localdir) {
  //    _backgroundCommand bgt = new _backgroundCommand(codeID,command,env,localdir);
  //    bgt.start();
  //  }

  private static void writeOutputFiles(String codeID, File localdir,
      ByteArrayOutputStream commandStdout, ByteArrayOutputStream commandStderr) {
    File outfile = new File(localdir, codeID + ".out");
    File errfile = new File(localdir, codeID + ".err");
    try {
      BufferedWriter br = new BufferedWriter(new FileWriter(outfile));
      br.write(commandStdout.toString());
      br.close();
      br = new BufferedWriter(new FileWriter(errfile));
      br.write(commandStderr.toString());
      br.close();
    } catch (IOException e) {
      System.out.println("Warning: Code - "+codeID + "completed but unable to write stdout and stderr to file: Exception: " +e.getMessage());
    }
  }

  private class _backgroundCommand extends Thread {
    int exitcode = -1;
    private String command;
    String[] env;
    File localdir;
    String codeID;

    public _backgroundCommand(String codeID, String command,String[] env,File localdir){
      this.codeID = codeID;
      this.command = command;
      this.env = env;
      this.localdir = localdir;
    }

    public int getExitcode(){
      return exitcode;
    }
    public void run() {
      LocalExec exec = new LocalExec();
      ByteArrayOutputStream commandStdout = new ByteArrayOutputStream();
      ByteArrayOutputStream commandStderr = new ByteArrayOutputStream();
      System.out.println("trying to execute command: " + command);
      try {
        exitcode = exec.executeCmd(command, commandStdout, commandStderr, null, env, localdir);
        writeOutputFiles(codeID, localdir, commandStdout, commandStderr);
      } catch (ExecException e) {
        try {
          System.out.println("Exception executing command" + e.getMessage() );
          FileUtils.writeStringToFile(new File(localdir,codeID+".err"), "Exception executing command: " + command + ": "+ e);
        } catch (IOException e1) {
          System.out.println("Unable to write exception to .err file." );
        }
      }
    }
  }

  public static void main(String a[]){
    try {
      File rundir = new File( "/Users/d3x140/Work/ASCEM");
      int exitcode = CodeExecutor.execute("visit","/Users/d3x140/Work/ASCEM/noise.silo", rundir);
      System.out.println("Returned exitcode="+exitcode);
    } catch (ExecException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }

  public static class StreamGobbler extends Thread {
    private InputStream is;
    private StringBuffer msg = new StringBuffer();

    public StreamGobbler(InputStream is) {
      this.is = is;
    }

    public String getMessage() {
      return msg.toString();
    }

    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
          msg.append(line + "\n");
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}
