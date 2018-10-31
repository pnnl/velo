package gov.pnnl.cat.core.test;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.util.alfresco.AlfrescoRepoWebserviceUtils;
import gov.pnnl.velo.model.CmsPath;

import java.util.Properties;

import org.alfresco.webservice.action.Action;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Predicate;

//This class requires repository.properties and RunTreeCrawlerTest.properties in classpath
public class RunTreeCrawlerTest extends AbstractVeloTest {
 
  //bean id of the visitor that needs to be invoked by the TreeCrawler on each node
  public static String PROP_VISITOR = "visitor-id";
  //true if action on each node needs separate txn. false other wise
  public static String PROP_TXN = "transaction-mode"; 
  //path to start with. Start path after Company Home
  public static String PROP_START_PATH = "start-path";

  public RunTreeCrawlerTest(String username, String password, String repositoryPropertiesFilePath, String cmsServicesFilePath, String tifServicesFilePath)
      throws Exception {
    super(username, password, repositoryPropertiesFilePath, cmsServicesFilePath, tifServicesFilePath);
  }


  @Override
  protected void run() throws Exception {
    Properties properties = getInputProperties("./config/visitor.properties");
    String visitorName = properties.getProperty(PROP_VISITOR);
    String txPerNode =  properties.getProperty(PROP_TXN);
    String startPath =  properties.getProperty(PROP_START_PATH);
  
    try {
      IResourceManager mgr = ResourcesPlugin.getResourceManager();
      // Run the action on the User Documents tree
      CmsPath userDocsPath = new CmsPath(startPath);
      IResource userDocs = mgr.getResource(userDocsPath);
      Predicate actionedUponNode = AlfrescoRepoWebserviceUtils.getPredicate(userDocsPath);

      NamedValue visitorParam = new NamedValue(PROP_VISITOR,false, visitorName, null);
      NamedValue txParam = new NamedValue(PROP_TXN,false, txPerNode, null);


      // action web service crashes if you don't have a parameter, even
      // if your action takes no parameters - doh
      NamedValue[] parameters = new NamedValue[]{visitorParam, txParam};        
      Action newAction1 = new Action();
      newAction1.setActionName("tree-crawler");
      newAction1.setTitle("Tree Crawler");
      newAction1.setDescription("Tree Crawler.");
      newAction1.setParameters(parameters);        

      // Execute the action
      mgr.executeActions(actionedUponNode, new Action[]{newAction1});
      System.out.println("action started successfully");

    } catch (Exception e) { 
      e.printStackTrace();
    }
    
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
          
      RunTreeCrawlerTest test = new RunTreeCrawlerTest(username, password, repositoryPropertiesFilePath, cmsServicesFilePath, tifServicesFilePath);
      test.run();

    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
    
    System.out.println("Done!");
    System.exit(0);
  }

}
