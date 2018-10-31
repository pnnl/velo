package gov.pnnl.cat.core.test.junit;
import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.velo.util.SpringContainerInitializer;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestISecurityManager {
	protected static IResourceManager resourceManager;
  protected static ISearchManager searchManager;
  protected static ISecurityManager securityManager;
  // Credentials here as this is a test account
  protected static String username = "Junit";
  protected static String password = "Akun@T3st";
  protected static String basePath = "/User Documents/Junit";
	
	@BeforeClass
	public static void abstractVeloTestImpelemtation() throws Exception {
		
	  // Configure system properties for Velo to work properly (can also be provided as runtime args)
    
    System.setProperty("logfile.path", "./velo.log");
    System.setProperty("repository.properties.path", "./repository.properties");
    
    SpringContainerInitializer.loadBeanContainerFromClasspath(null);
    securityManager = CmsServiceLocator.getSecurityManager();
    resourceManager = CmsServiceLocator.getResourceManager();
    searchManager = CmsServiceLocator.getSearchManager();

    securityManager.login(username, password);
	}

	// Procedure for all test classes:
	//	1. Setup - Create needed resources
	//	2. Pre-check - Ensure resource does not exist already.
	//	3. Create
	//	4. Verify it was created correctly
	//	5. Clean up
	
	@Test
	public void dummyTest() {
//		CmsPath path = new CmsPath(basePath).append("junitTestFolderCreated1"); // setup
//		
//		assertFalse(resourceManager.resourceExists(path)); // pre-check
//		
//		IResource resource = resourceManager.createFolder(path); // create
//		
//    assertTrue(resourceManager.resourceExists(path)); // verify
//    assertTrue(resourceManager.resourceExists(resource.getPath())); // verify again
//    
//    resourceManager.deleteResource(path); // clean up
//    assertFalse(resourceManager.resourceExists(path));
	}
	
}
