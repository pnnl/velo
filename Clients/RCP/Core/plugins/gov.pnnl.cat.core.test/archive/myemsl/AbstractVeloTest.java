package gov.pnnl.cat.core.resources.tests.myemsl;

import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.velo.util.SpringContainerInitializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public abstract class AbstractVeloTest {
  protected IResourceManager resourceManager;
  protected ISearchManager searchManager;
  protected ISecurityManager securityManager;
  protected File outputFile;
  protected String username;
  protected String password;
  protected Properties properties;
  protected File repositoryPropertiesFile;
  
  public AbstractVeloTest(String[] commandLineArgs) throws Exception {
    String fileName = this.getClass().getSimpleName() + "Output.csv";
    outputFile = new File(fileName); // located in current run dir  
    parseCommandLineArgs(commandLineArgs);
    loadInputProperties();
    initializeServices();
    getOutputFile().delete();
  }
  
  public AbstractVeloTest(String username, String password, String repoFileName) throws Exception {
    String fileName = this.getClass().getSimpleName() + "Output.csv";
    outputFile = new File(fileName); // located in current run dir  
    this.username = username;
    this.password = password;
    this.repositoryPropertiesFile = new File(repoFileName);
    loadInputProperties();
    initializeServices();
    getOutputFile().delete();   
  }
    
  protected File getInputPropertiesFile() {
    String fileName = this.getClass().getSimpleName() + ".properties";
    File inputPropertiesFile = new File(fileName); // located in current run dir 
    System.out.println("Reading input properties from: " + inputPropertiesFile.getAbsolutePath());
    return inputPropertiesFile;
  }
  
  protected void loadInputProperties() {
    File propsFile = getInputPropertiesFile();
    properties = new Properties();
    try {
      properties.load(new FileInputStream(propsFile));
    } catch (IOException e) {
      System.out.println(e.toString());
    }
  }
  
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
  
  protected File getOutputFile() {
    return outputFile;
  }

  protected abstract void run() throws Exception;
  
  protected void initializeServices() throws Exception {
    
    // Configure system properties for Velo to work properly (can also be provided as runtime args)
    
    System.setProperty("logfile.path", "./velo.log");
    if(repositoryPropertiesFile == null) {
      System.setProperty("repository.properties.path", "./repository.properties");
    } else {
      System.setProperty("repository.properties.path", repositoryPropertiesFile.getAbsolutePath());    
    }
    
    SpringContainerInitializer.loadBeanContainerFromClasspath(null);
    securityManager = CmsServiceLocator.getSecurityManager();
    resourceManager = CmsServiceLocator.getResourceManager();
    searchManager = CmsServiceLocator.getSearchManager();
    
    securityManager.login(username, password);

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


}
