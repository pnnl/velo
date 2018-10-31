package gov.pnnl.cat.core.resources.tests.myemsl;

import java.io.File;

import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.velo.model.CmsPath;


public class UploadProfile extends AbstractVeloTest {

  public UploadProfile(String username, String password, String repoFilePath) throws Exception {
    super(username, password, repoFilePath);
    
    // add headers to outputFiles
    appendToFile(getOutputFile(), "Username,Email");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      UploadProfile test = new UploadProfile("admin", "pw", "C:\\Users\\Administrator\\repository.properties.carina");
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
    File fileToUpload = new File("E:\\Downloads\\jdk-7u71-windows-x64.exe");
    CmsPath path = new CmsPath("/User Documents/admin/test/jdk-7u71-windows-x64.exe");
    for (int i = 1; i <= 10; i++) {
      long start = System.currentTimeMillis();
      resourceManager.createFile(path, fileToUpload);      
      long end = System.currentTimeMillis();
      
      resourceManager.deleteResource(path);
      
      long seconds = (end - start) / 1000;
      appendToFile(getOutputFile(), i + "," + seconds);
    }    
  }
  
}
