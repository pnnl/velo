package gov.pnnl.velo.util;

import gov.pnnl.cat.core.resources.CmsServiceLocator;

/**
 * TODO: Convert this into utility class that can start up bean
 * container and load repository.properties
 *
 */
public class VeloServiceInitializer {

  public static void initializeServices(String logfileName, String repositoryPropertiesPath, String runtimeWorkspacePath) throws Exception {
    initializeServices(logfileName, repositoryPropertiesPath, runtimeWorkspacePath, null, null);
  }
  
  public static void initializeServices(String logfileName, String repositoryPropertiesPath, String runtimeWorkspacePath,
      String username, String password) throws Exception {
    
    // Configure system properties for Velo to work properly (can also be provided as runtime args)
    System.setProperty("velo.folder.path", runtimeWorkspacePath);
    System.setProperty("logfile.name", logfileName);
    System.setProperty("repository.properties.path", repositoryPropertiesPath);
    
    // these two files better be on classpath
    String[] classpathBeanFiles = {SpringContainerInitializer.BEAN_FILE_NAME_CMS_SERVICES, SpringContainerInitializer.BEAN_FILE_NAME_TIF_SERVICES};
    SpringContainerInitializer.loadBeanContainerFromClasspath(classpathBeanFiles);
    
    // log in
    if(username != null && password != null) {
      CmsServiceLocator.getSecurityManager().login(username, password);
    }

  }

}
