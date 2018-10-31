package gov.pnnl.cat.core.test;

import gov.pnnl.cat.core.internal.resources.events.NotificationManagerJMS;
import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.velo.tif.service.CodeRegistry;
import gov.pnnl.velo.tif.service.JobConfigService;
import gov.pnnl.velo.tif.service.JobLaunchService;
import gov.pnnl.velo.tif.service.MachineRegistry;
import gov.pnnl.velo.tif.service.ScriptRegistry;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.tif.service.VeloWorkspace;
import gov.pnnl.velo.util.SpringContainerInitializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class AbstractVeloTest {
  // service beans
  protected IResourceManager resourceManager;
  protected ISearchManager searchManager;
  protected ISecurityManager securityManager;
  protected NotificationManagerJMS notificationManager;
  protected JobLaunchService jobLaunchService;
  
  // configuration params
  protected String username;
  protected String password;
  protected File repositoryPropertiesFile;
  protected File cmsServicesFile;
  protected File tifServicesFile;
  protected CodeRegistry codeRegistry;
  protected MachineRegistry machineRegistry;
  protected ScriptRegistry scriptRegistry;
  protected JobConfigService jobConfigService;
  protected VeloWorkspace veloWorkspace;
  protected File outputFile;
    
  public AbstractVeloTest(String username, String password, String repositoryPropertiesFilePath,
      String cmsServicesFilePath, String tifServicesFilePath) throws Exception {
    this.username = username;
    this.password = password;
    this.repositoryPropertiesFile = new File(repositoryPropertiesFilePath);
    this.cmsServicesFile = new File(cmsServicesFilePath);
    this.tifServicesFile = new File(tifServicesFilePath);
    initializeServices();
  }
  
  public AbstractVeloTest(String[] commandLineArgs) throws Exception {
    parseCommandLineArgs(commandLineArgs);
    initializeServices();
  }
  
  /**
   * TODO: add support for other parameters like repository.properties path
   * @param args
   */
  protected void parseCommandLineArgs(String[] args) {
    // parse username/password;
    for(int i = 0; i < args.length; i++) {
      if(args[i].equals("-u")) {
        username = args[i+1];
      }
      if(args[i].equals("-p")) {
        password = args[i+1];
      }
    }
    
  }
  
  protected abstract void run() throws Exception;
  
  protected void initializeServices() throws Exception {
    // create an output file for quickly dumping the output of the test
    String fileName = this.getClass().getSimpleName() + "Output.csv";
    outputFile = new File(fileName); // located in current run dir  
    getOutputFile().delete(); // delete any previous version
    
    // Configure system properties for Velo to work properly (can also be provided as runtime args)
    System.setProperty("logfile.path", "./velo.log");
    if(repositoryPropertiesFile == null) {
      System.setProperty("repository.properties.path", "./repository.properties");
    } else {
      System.setProperty("repository.properties.path", repositoryPropertiesFile.getAbsolutePath());    
    }
    
    // Initialize the spring container
    if(cmsServicesFile == null && tifServicesFile == null) {
      SpringContainerInitializer.loadBeanContainerFromClasspath(null);
    
    } else {
      List<String> beanFilePaths = new ArrayList<String>();
      if(cmsServicesFile != null) {
        beanFilePaths.add("file:" + cmsServicesFile.getAbsolutePath());
      }
      if(tifServicesFile != null) {
        beanFilePaths.add("file:" + tifServicesFile.getAbsolutePath());
      }
      SpringContainerInitializer.loadBeanContainerFromFilesystem(beanFilePaths.toArray(new String[beanFilePaths.size()]));
    }
    
    // set the service classes
    securityManager = CmsServiceLocator.getSecurityManager();
    resourceManager = CmsServiceLocator.getResourceManager();
    searchManager = CmsServiceLocator.getSearchManager();
    notificationManager = CmsServiceLocator.getNotificationManager();
    
    if(tifServicesFile != null) {
      jobLaunchService = TifServiceLocator.getJobLaunchingService();
      codeRegistry = TifServiceLocator.getCodeRegistry();
      machineRegistry = TifServiceLocator.getMachineRegistry();
      scriptRegistry = TifServiceLocator.getScriptRegistry();
      jobConfigService = TifServiceLocator.getJobConfigService();
      veloWorkspace = TifServiceLocator.getVeloWorkspace();
    }
  
    
    securityManager.login(username, password);

  }
  
  protected File getOutputFile() {
    return outputFile;
  }
  
  /**
   * Append given string to end of given file.
   * 
   * @param file
   * @param additionalContent
   */
  public void appendToFile(File file, String additionalContent) {
    BufferedWriter bw = null;

    try {
      if(!file.exists()) {
        file.createNewFile();
      }
      // open file in append mode
      bw = new BufferedWriter(new FileWriter(file, true));
      bw.write(additionalContent);
      bw.newLine();
      bw.flush();

    } catch (IOException ioe) {
      // for now, throw exception up to caller
      throw new RuntimeException(ioe);

    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException ioe2) {
        }
      }
    }
  }
  
  /**
   * @param filePath
   * @return
   */
  protected Properties getInputProperties(String filePath) {
    File propsFile = new File(filePath); // located in current run dir 
    System.out.println("Reading input properties from: " + propsFile.getAbsolutePath());
    Properties properties = new Properties();
    
    try {
      properties.load(new FileInputStream(propsFile));
      
    } catch (IOException e) {
      System.out.println(e.toString());
    }
    return properties;
  }

  
}
