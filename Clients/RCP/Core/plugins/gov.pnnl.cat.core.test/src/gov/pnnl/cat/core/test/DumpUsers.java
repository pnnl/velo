package gov.pnnl.cat.core.test;

import gov.pnnl.cat.core.resources.security.IUser;


public class DumpUsers extends AbstractVeloTest {

  public DumpUsers(String username, String password, String repositoryPropertiesFilePath, String cmsServicesFilePath, String tifServicesFilePath)
      throws Exception {
    super(username, password, repositoryPropertiesFilePath, cmsServicesFilePath, tifServicesFilePath);
        
    // add headers to outputFiles
    appendToFile(getOutputFile(), "Username,Email");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out.println("Starting test...");
    try {
      // Set up the environment
      String username = "admin";
      String password = "Koolcat1";
      String repositoryPropertiesFilePath = "./config/repository.properties.premier";
      String cmsServicesFilePath = "./config/cms-services.xml";
      String tifServicesFilePath = "./config/tif-services.xml";
          
      DumpUsers test = new DumpUsers(username, password, repositoryPropertiesFilePath, cmsServicesFilePath, tifServicesFilePath);
      test.run();

    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
    
    System.out.println("Done!");
    System.exit(0);

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.tests.myemsl.AbstractVeloTest#run()
   */
  @Override
  protected void run() throws Exception {
    IUser[] users = securityManager.getUsers();
    for(IUser user : users) {
      appendToFile(getOutputFile(), user.getUsername() + "," + user.getEmail());
    }
    
  }
  
}
