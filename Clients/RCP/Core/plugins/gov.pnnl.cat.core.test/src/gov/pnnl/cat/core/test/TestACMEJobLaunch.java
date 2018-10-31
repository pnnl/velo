package gov.pnnl.cat.core.test;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.model.Credentials;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.KeyboardInteractiveCredentials;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.service.CodeRegistry;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloTifConstants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Test services against Akuna Dev server
 * @author d3k339
 *
 */
public class TestACMEJobLaunch extends AbstractVeloTest {

 
  public TestACMEJobLaunch(String username, String password, String repositoryPropertiesFilePath, String cmsServicesFilePath, String tifServicesFilePath)
      throws Exception {
    super(username, password, repositoryPropertiesFilePath, cmsServicesFilePath, tifServicesFilePath);
  }

  @Override
  protected void run() throws Exception {
    launchJob();
  }
  
  private void launchJob() {
//	ArrayList<String> keys = new ArrayList<String>();
//	keys.add(VeloTifConstants.JOB_STATUS);
//	List<String> propertiesAsString = resourceManager.getPropertiesAsString(new CmsPath("/company_home/User Documents/admin/fake_acme_case_03-16-2015_15-01-04"), keys);
	  
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
	JobConfig config = new JobConfig("fake_acme_job");
	
	config.setCmsUser(username);
	config.setCodeId("acmeworkflow");
	config.setCodeVersion(CodeRegistry.VERSION_DEFAULT);
	String remoteDir = "/people/d3k339/fake_acme_case";
	config.setRemoteDir(remoteDir);
	//context‰ path should be based on case folder path
	String contextPathName = new File(remoteDir).getName()+"_" +dateFormat.format(new Date());
	CmsPath contextPath = new CmsPath(resourceManager.getHomeFolder() + "/" + contextPathName);
	resourceManager.createFolder(contextPath);
	config.setContextPath(contextPath.toAssociationNamePath());
	config.setDoNotQueue(true);
	config.setJobId(contextPathName);
	config.setMachineId("olympus");
	config.setUserName("d3k339");
	config.setPollingInterval(5);
	config.setLocalMonitoring(false);
	
	//config.setUserName(userName);
	
    //needed for actual workflow
    //	Machine machine = machineRegistry.get("localhost");
    //	Code code = machine.getCodes().get(0);
	//String commandWithoutArgs = code.getJobLaunching().getCommand();
	//config.setCommand(commandWithoutArgs + args);

	try {
		jobLaunchService.launchJob(config, null);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
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
      String username = "admin";
      String password = "password";
      File f = new File(".");
      System.out.println(f.getAbsolutePath());
      String repositoryPropertiesFilePath = "config/repository.properties.carina";
      String cmsServicesFilePath =  "config/cms-services.xml";
      String tifServicesFilePath = "config/tif-services.xml";
          
      TestACMEJobLaunch test = new TestACMEJobLaunch(username, password, repositoryPropertiesFilePath, cmsServicesFilePath, tifServicesFilePath);
      test.run();

    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
    
    System.out.println("Done!");
    System.exit(0);
  }

}
