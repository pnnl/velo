package gov.pnnl.cat.core.resources.tests.myemsl;

import gov.pnnl.cat.core.resources.security.IUser;


public class DumpUsers extends AbstractVeloTest {

  public DumpUsers(String username, String password, String repoFilePath) throws Exception {
    super(username, password, repoFilePath);
    
    // add headers to outputFiles
    appendToFile(getOutputFile(), "Username,Email");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      DumpUsers test = new DumpUsers("admin", "pw", "C:\\Users\\Administrator\\repository.properties.premier");
      test.run();

    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Done");
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
