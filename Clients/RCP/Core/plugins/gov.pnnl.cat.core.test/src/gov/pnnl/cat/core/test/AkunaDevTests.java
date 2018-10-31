package gov.pnnl.cat.core.test;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloTifConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Test services against Akuna Dev server
 * @author d3k339
 *
 */
public class AkunaDevTests extends AbstractVeloTest {

 
  public AkunaDevTests(String username, String password, String repositoryPropertiesFilePath, String cmsServicesFilePath, String tifServicesFilePath)
      throws Exception {
    super(username, password, repositoryPropertiesFilePath, cmsServicesFilePath, tifServicesFilePath);
  }

  @Override
  protected void run() throws Exception {
    CmsPath path = new CmsPath("/Velo/projects/VisIt Testing/Tanks-Structured/SR-hopper");
    resourceManager.setProperty(path, VeloTifConstants.JOB_RUNDIR, "/scratch/scratchdirs/vfreedma/VisIt_Testing/Tanks-Structured/SR-hopper_082815_101017");
    //createRemoteLink();
  }
  
  private void createFolder() {
    // create a folder
    CmsPath path = new CmsPath("/User Documents/Carina").append("testFolder");
    resourceManager.createFolder(path);
    System.out.println("Created folder " + path.toDisplayString());    
  }
  
  private void createRemoteLink() {
    String type = VeloConstants.TYPE_FOLDER;
    CmsPath parentPath = new CmsPath("/User Documents/Carina/CLM/CLM SA/Inputs");
    RemoteLink link = new RemoteLink(type, parentPath.toAssociationNamePath(), 
        "hopper.nersc.gov", "/global/u2/c/carinal/F_1850_T31_g37_titan", "Case Folder", "Remote Case Folder");

    List<RemoteLink>remoteLinks = new ArrayList<RemoteLink>();
    remoteLinks.add(link);
    
    resourceManager.createRemoteLinks(remoteLinks);
  }
  
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out.println("Starting test...");
    try {
      // Set up the environment
      String username = "Carina";
      String password = "Carina1";
      String repositoryPropertiesFilePath = "./config/repository.properties.akunaDev";
      String cmsServicesFilePath = "./config/cms-services.xml";
      String tifServicesFilePath = "./config/tif-services.xml";
          
      AkunaDevTests test = new AkunaDevTests(username, password, repositoryPropertiesFilePath, cmsServicesFilePath, tifServicesFilePath);
      test.run();

    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
    
    System.out.println("Done!");
    System.exit(0);
  }

}
