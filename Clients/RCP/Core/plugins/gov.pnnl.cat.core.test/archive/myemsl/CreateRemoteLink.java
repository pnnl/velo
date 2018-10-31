package gov.pnnl.cat.core.resources.tests.myemsl;

import java.util.ArrayList;


import java.util.List;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.util.VeloConstants;


public class CreateRemoteLink extends AbstractVeloTest {

  public CreateRemoteLink(String username, String password, String repoFilePath) throws Exception {
    super(username, password, repoFilePath);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      CreateRemoteLink test = new CreateRemoteLink("Carina", "pw", "C:\\Users\\Administrator\\repository.properties.akunaDev");
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
    String type = VeloConstants.TYPE_FOLDER;
    CmsPath parentPath = new CmsPath("/User Documents/Carina/CLM/CLM SA/Inputs");
    RemoteLink link = new RemoteLink(type, parentPath.toAssociationNamePath(), 
        "hopper.nersc.gov", "/global/u2/c/carinal/F_1850_T31_g37_titan", "Case Folder", "Remote Case Folder");

    List<RemoteLink>remoteLinks = new ArrayList<RemoteLink>();
    remoteLinks.add(link);
    
    resourceManager.createRemoteLinks(remoteLinks);
  }
  
}
