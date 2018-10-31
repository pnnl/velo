package gov.pnnl.cat.core.test.junit;

import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.security.CatSecurityException;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.velo.util.SpringContainerInitializer;

public class VeloTestUtils {
  
  protected static ISecurityManager securityManager;
  // Credentials here as this is a test account
  protected static String username = "Junit";
  protected static String password = "Akun@T3st";
  
  public static IResourceManager initializeResourceManager() throws ServerException, CatSecurityException{
  
  System.setProperty("logfile.path", "./velo.log");
  System.setProperty("repository.properties.path", "./repository.properties");
  
  SpringContainerInitializer.loadBeanContainerFromClasspath(null);
  securityManager = CmsServiceLocator.getSecurityManager();
  IResourceManager resourceManager = CmsServiceLocator.getResourceManager();
  securityManager.login(username, password);
  return resourceManager;
  
  }


  
}
