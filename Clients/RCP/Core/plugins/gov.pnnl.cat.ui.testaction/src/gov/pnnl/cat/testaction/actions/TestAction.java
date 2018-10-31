package gov.pnnl.cat.testaction.actions;

//import ezjcom.JComObject;
//import ezjcom.JComVariant;
//import gov.pnl.ezjcom.outlook.Application;
//import gov.pnl.ezjcom.outlook.Selection;
//import gov.pnl.ezjcom.outlook._Application;
import gov.pnnl.cat.core.internal.resources.ResourceService;
import gov.pnnl.cat.core.resources.ICatCML;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.util.alfresco.AlfrescoRepoWebserviceUtils;
import gov.pnnl.cat.core.resources.util.alfresco.AlfrescoUtils;
import gov.pnnl.cat.core.util.logger.EZLogger;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.actions.resourceActions.ResourceActionManager;
import gov.pnnl.cat.util.XmlUtility;
import gov.pnnl.cat.webservice.subscription.Subscription;
import gov.pnnl.cat.webservice.subscription.SubscriptionOwner;
import gov.pnnl.cat.webservice.util.AlertingConstants;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.webservice.action.Action;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLCopy;
import org.alfresco.webservice.types.CMLDelete;
import org.alfresco.webservice.types.ParentReference;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.util.Constants;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class TestAction implements IWorkbenchWindowActionDelegate {
  private IWorkbenchWindow window;
  private static Logger logger = CatLogger.getLogger(TestAction.class);

  /**
   * The constructor.
   */
  public TestAction() {
  }

  /**
   * The action has been activated. The argument of the
   * method represents the 'real' action sitting
   * in the workbench UI.
   * @see IWorkbenchWindowActionDelegate#run
   */
  public void run(IAction action) {
    String result = "ok";


    try {
      testActionWebscript();
      
      
//      testRemoveAspect();
      
//      testGetHypoDocsForTripleStore();
      //testAscemWebScript();
//      testSetOwner();
//      testSubscriptionAction("runsearch");
      //testSubscriptionAction("digest");
      //importTaxonomy("C:\\BKC\\process.txt");
      //createTimelineTaxonomy();
      //importTaxonomy("C:\\testSubscriptionActionBKC\\atmospheric.txt");
      //importTaxonomy("C:\\BKC\\weapons.txt");

      //RepositoryTest.setupRepository();
      //      testStringSplit();

      //result = RepositoryTest.getProperty("/physical/personal_libraries/d3k339/CarinaTest.txt/jcr:content", "default", "jcr:rawtext");    
      //RepositoryTest.importFolder("/physical/personal_libraries/d3k339", "\\\\bkc\\shared\\curtTemp\\CatTestData\\SearchTest");    
      //RepositoryTest.importFolder("/physical/personal_libraries/d3k339", "C:\\BKC\\test");
      //RepositoryTest.addProperty("/physical/personal_libraries/d3k339/test.txt/jcr:content", "cat:description", "blah");

      //     EZLogger.logMessage(result);

      //testAddFeaturesAction();
      //setupAndTestUrlImportAction();

      //	testImportWebService();
      //testUPATransformAction();


      //testUPATransformAction();

      //testAddTopic();

      //testGetURL();

      //result = getCompartments();

      //testStartWorkflow();

      //testSendToTaxonomyAction();

      //deleteClassification("/cm:categoryRoot/sme:domainExpertise");

      //copyPrimaryDisciplineToSecondaryDiscipline();

      //testUpdateUserProperty();

      //testCrawlerAction();
      //   testSubscriptionTestAction();
      //testNotificationFlood();
      //runSearchBatchSizeOptimizerTest();
      //testHarvestService();
      //testSearch(); 
      testCreateTonsOfFoldersSlowly();
    } catch (Exception e) {
      result = "Error occurred - see log.";
      EZLogger.logError(e,"Error running TestAction");
    }

    MessageDialog.openInformation(
        window.getShell(),
        "Test Action Plug-in",
        result);
  }
 
private void testRemoveAspect() {
  IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
  IResource parent = mgr.getResource(new CmsPath("/Velo/projects/tutorial/Richard-1D-transport_exa"));
  recurseRemoveAspect(parent);
  
  }

private void recurseRemoveAspect(IResource resource) {

  IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
  if(resource instanceof IFolder){
    for(IResource child: ((IFolder) resource).getChildren()){
      recurseRemoveAspect(child);
    }
  }
  mgr.removeAspect(resource.getPath(), "http://www.alfresco.org/model/system/1.0");
  mgr.removeAspect(resource.getPath(), "http://www.alfresco.org/model/content/1.0");
  
}

//  private void testSearch() {
//    String query = "@velo\\:status:\"Running\" AND @cm\\:creator:\"admin\"";
//    ICatQueryResult queryResult = ResourcesPlugin.getDefault().getSearchManager().queryAll(ISearchManager.LANGUAGE_LUCENE, query);
//    List<IResource> resources = queryResult.getResources();
//    for (IResource resource : resources) {
//      System.out.println(resource.getName());
//    }
//  }

  
  private void testCreateTonsOfFoldersSlowly(){
    int numOfParentFolders = 1500;
    int numOfChildFolders = 15;
    int delayBetweenCreates = 1;
    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
    CmsPath root = new CmsPath("/Velo/testBigFolder2");
    mgr.createFolder(root);
    for(int i = 500; i < numOfParentFolders; i++){
      CmsPath path = root.append("Run"+String.valueOf(i));
      mgr.createFolder(path);
      mgr.setProperty(path, "{http://www.pnl.gov/velo/model/content/1.0}mimetype", "junkFolder");
      try {
        Thread.sleep(delayBetweenCreates*1000);
      } catch (InterruptedException e) {}
      for(int j = 0; j < numOfChildFolders; j++){
        CmsPath subPath = path.append("Step"+String.valueOf(j));
        mgr.createFolder(subPath);
        mgr.setProperty(subPath, "{http://www.pnl.gov/velo/model/content/1.0}mimetype", "junkSubFolder");
        try {
          Thread.sleep(delayBetweenCreates*1000);
        } catch (InterruptedException e) {}
      }
    }
    
  }
  
  
  
private void testGetHypoDocsForTripleStore() throws Exception {
    
    File logFile = new File("C:\\Temp\\TAIDatasetDocsToUUIDs.txt");
    try {
      System.setOut(new PrintStream(new FileOutputStream(logFile)));
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    ResourceActionManager resourceActionMgr = ResourceActionManager.getInstance();
    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
    CmsPath path = new CmsPath("/TAI Dataset");
    IFolder folder = (IFolder)mgr.getResource(path);
    for (IResource resource : folder.getChildren()) {
      if(resource instanceof IFolder){
        for (IResource file : ((IFolder)resource).getChildren()) {
          String uuid = file.getPropertyAsString(VeloConstants.PROP_UUID);
          String urlPath = "/alfresco/d/a/workspace/SpacesStore/" + uuid + "/" + file.getName();
          urlPath = URIUtil.encodePath(urlPath);
          System.out.println(file.getName() + "\t" + urlPath);
        }
      }
      
    }
    
  }

  public void testClassPath() {
    String relativePath = "bin";
    String path = relativePath;
    URL fileUrl = ResourcesPlugin.getDefault().getBundle().getEntry(relativePath);
    
    try {
      if(fileUrl != null) {
        path = FileLocator.toFileURL(fileUrl).getPath();
      }
    } catch (IOException e) {
      logger.error("Failed to convert file url", e);
      e.printStackTrace();
    }
    
    System.out.println(path);

  }
  
  /**
   * Copy primaryDiscipline to secondaryDiscipline, without copying children (2 parents)
   * @throws Exception
   */
  public void copyPrimaryDisciplineToSecondaryDiscipline() throws Exception {
    // Spaces Store
    Store store = new Store(Constants.WORKSPACE_STORE, "SpacesStore");

    // A reference to the classification being copied
    String classificationPath = "/cm:categoryRoot/sme:primaryDiscipline";
    Reference classificationRef = new Reference(store, null, classificationPath);
    Predicate predicate = new Predicate(new Reference[]{classificationRef}, store, null);

    // A reference to the new parent folder where the classification will be copied
    String newClassificationName = "secondaryDiscipline";
    String fullyQualifiedNewName = Constants.createQNameString("http://www.pnl.gov/dmi/model/sme/1.0", 
        newClassificationName);
    ParentReference parentRef = new ParentReference(store, "e2ba811d-72db-11dc-9732-01e0c43e097e", 
        null, "{http://www.alfresco.org/model/content/1.0}categories", 
        fullyQualifiedNewName);

    // Perform the copy
    CML cml = new CML();
    CMLCopy copy = new CMLCopy(parentRef, null, Constants.ASSOC_CONTAINS, newClassificationName, predicate, null, false);
    cml.setCopy(new CMLCopy[]{copy});
//    AlfrescoWebServiceFactory.getRepositoryService().update(cml);

  }
  

  public void deleteClassification(String classificationPath) throws Exception {

    // Get a reference to the respository web service
//    RepositoryServiceSoapBindingStub repositoryService = AlfrescoWebServiceFactory.getRepositoryService();         

    // Spaces Store
    Store store = new Store(Constants.WORKSPACE_STORE, "SpacesStore");

    // A reference to the node being deleted
    Reference reference = new Reference(store, null, classificationPath);
    Predicate predicate = new Predicate(new Reference[]{reference}, store, null);

    // Perform the delete
    CML cml = new CML();
    CMLDelete delete = new CMLDelete(predicate);
    cml.setDelete(new CMLDelete[]{delete});

//    AlfrescoWebServiceFactory.getRepositoryService().update(cml);

  }

//  private void testStartWorkflow() throws Exception {
//
//    try {
//      // Create the action to run the import
//      CmsPath path = new CmsPath("/Temporary Files/dogs168.txt");
//      org.alfresco.webservice.types.Predicate predicate = AlfrescoRepoWebserviceUtils.getPredicate(path);
//
//      org.alfresco.webservice.types.NamedValue workflowName = Utils.createNamedValue( "workflowName", "jbpm$wf:review"); 
//      org.alfresco.webservice.types.NamedValue workflowAssg = Utils.createNamedValue( "bpm:assignee", "admin"); 
//      org.alfresco.webservice.types.NamedValue[] params = new NamedValue[]{workflowName, workflowAssg}; 
//      org.alfresco.webservice.types.Action wfAction = new Action(); 
//      wfAction.setActionName( "start-workflow"); 
//      wfAction.setParameters(params); 
////      AlfrescoWebServiceFactory.getActionService().executeActions(predicate, new Action[]{wfAction});
//      IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
//      mgr.executeActions(predicate, new Action[]{wfAction});
//      System.out.println("action started successfully");
//
//    } catch (Exception e) { 
//      e.printStackTrace();
//    }
//
//  }
  
  private void testActionWebscript() throws Exception {

    try {

//      org.alfresco.webservice.types.Predicate predicate;
//      org.alfresco.webservice.action.Action[] actions;

      IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
      // Run the action on the User Documents tree
      CmsPath userDocsPath = new CmsPath("/User Documents");
      IResource userDocs = mgr.getResource(userDocsPath);
      org.alfresco.webservice.types.Predicate actionedUponNode = AlfrescoRepoWebserviceUtils.getPredicate(userDocsPath);

      org.alfresco.webservice.types.NamedValue visitorParam = new org.alfresco.webservice.types.NamedValue("iterations",false, "500", null);


      // action web service crashes if you don't have a parameter, even
      // if your action takes no parameters - doh
      org.alfresco.webservice.types.NamedValue[] parameters = new org.alfresco.webservice.types.NamedValue[]{visitorParam};        
      org.alfresco.webservice.action.Action newAction1 = new org.alfresco.webservice.action.Action();
      newAction1.setActionName("event-test");
      newAction1.setTitle("Tree Crawler");
      newAction1.setDescription("Tree Crawler.");
      newAction1.setParameters(parameters);        

      // Execute the action
//      AlfrescoWebServiceFactory.getActionService().executeActions(actionedUponNode, new Action[]{newAction1});

      mgr.executeActions(actionedUponNode, new org.alfresco.webservice.action.Action[]{newAction1});
      System.out.println("action started successfully");

    } catch (Exception e) { 
      e.printStackTrace();
    }

  }

  private void testGetURL() {
    try {
      IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
      CmsPath path = new CmsPath("/User Documents/bucket/Personal Library/AnthraxTest.txt");
      IFile file = (IFile)mgr.getResource(path);
      logger.debug("url = " + file.getHttpUrl());
    } catch (Exception e) {
      logger.error(e);
    }
  }

  /**
   * Try to add a discussion to a node
   *
   */
  private void testAddTopic() throws Exception {
    String forumNamespace = "http://www.alfresco.org/model/forum/1.0";

    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();

    CmsPath path = new CmsPath("/Temporary Files");

    IResource resource = mgr.getResource(path);

    if(!resource.hasAspect(VeloConstants.ASPECT_DISCUSSABLE)) {
      // add discussable aspect
      mgr.addAspect(path, VeloConstants.ASPECT_DISCUSSABLE);
    }

    // TODO: create a topic and a post.  How to do this - with a web service or some actions?
    //ICatCML cml = mgr.getCML();


  }

  private String runSearchTest() throws Exception {
    // TODO: update the search test
    return "TODO: update the search test";
    //    //SearchTest.exportDocumentView();
    //    InputDialog input = new InputDialog(window.getShell(), "Test Search", 
    //        "Enter XPath statement", "", null);
    //    
    //    
    //    // Note that JCR XPath always uses the node type for the element test!
    //    return SearchTest.simpleSearch("/jcr:root/physical/personal_libraries/d3k339//element(*, catnt:file)");

  }

  private void runNotificationTests() throws Exception {
    // Test some simple events for notification changes
    //RepositoryTest.setProperty("/physical/reference_library/cat:description", "blah");
    //RepositoryTest.createFolder("/virtual/favorites/d3k339", "junk2", "catnt:virtualFolder");    

  }

  private void runLinkTests() throws Exception {
    // PUT YOUR TEST CODE HERE
    /*
    RepositoryTest.createLink("/physical/reference_library/ATAC Newsletters/atac-vol-04-01.pdf",
         "physical/personal_libraries/d3k339/atac-vol-04-01-link.pdf");


    RepositoryTest.createLink("/physical/personal_libraries/d3k339/atac-vol-04-01-link.pdf",
        "physical/personal_libraries/d3k339/atac-vol-04-01-link-link.pdf");
     */

    //RepositoryTest.deleteNode("/physical/personal_libraries/d3k339/atac-vol-04-01-link.pdf");

    // this should fail because target is not a folder
    //RepositoryTest.createLinkedFolder("/physical/reference_library/ATAC Newsletters/atac-vol-04-01.pdf",
    //"physical/personal_libraries/d3k339/atac-vol-04-01-link-folder.pdf");

    // This should work because target is a folder
    //RepositoryTest.createLinkedFolder("/physical/reference_library/ATAC Newsletters",
    //  "physical/personal_libraries/d3k339/ATAC Newsletters Link");      

    //RepositoryTest.createLink("/physical/reference_library/ATAC Newsletters/atac-vol-04-01.pdf",
    //"virtual/favorites/d3k339/atac-vol-04-01.pdf");      

    // this should fail because I'm trying to import to a virtual folder
    //RepositoryTest.importFolder("/virtual/controlled_vocabulary", "\\\\bkc\\shared\\curtTemp\\CatTestData\\ATAC Newsletters");

  }

  private void testStringSplit() {

    String[] result = "\t\t\t2.4.1 Conversion of U Ore Concentrates".split("\\t");

    for (int x=0; x < result.length; x++)
      System.out.println(result[x]);
    System.out.println("num tokens = " + result.length);

  }


  private void addTestData() {

  }


  /**
   * Selection in the workbench has been changed. We 
   * can change the state of the 'real' action here
   * if we want, but this can only happen after 
   * the delegate has been created.
   * @see IWorkbenchWindowActionDelegate#selectionChanged
   */
  public void selectionChanged(IAction action, ISelection selection) {
  }

  /**
   * We can use this method to dispose of any system
   * resources we previously allocated.
   * @see IWorkbenchWindowActionDelegate#dispose
   */
  public void dispose() {
  }

  /**
   * We will cache window object in order to
   * be able to provide parent shell for the message dialog.
   * @see IWorkbenchWindowActionDelegate#init
   */
  public void init(IWorkbenchWindow window) {
    this.window = window;
  }

  public void setupAndTestUrlImportAction() throws ResourceException {
    BufferedReader reader = null;


    //		CmsPath rootPath = new CmsPath("/Reference Library/Taxonomies/New Taxonomy2/New Folder");

    CmsPath rootPath = new CmsPath("/User Documents/loadtest/Taxonomies/Test Taxonomy/A");
    CmsPath filePath = new CmsPath("/Temporary Files2/CarinaTest.txt");
    IFile file = (IFile)ResourcesPlugin.getDefault().getResourceManager().getResource(filePath);

    URL url = file.getHttpUrl();
    String urlString1 = url.toExternalForm();


    List list = new ArrayList();
    list.add(urlString1);

    //testUrlImportAction(rootPath, list);
    testUrlImportWebService(rootPath, list);
  }

//  public void testUrlImportAction(CmsPath targetFolder, List urlList) throws ResourceException {
//    BufferedReader reader = null;
//
//    Logger logger = CatLogger.getLogger(ResourceService.class);
//    try {
//      String xml = XmlUtility.serialize(urlList);
//
//      // Create the action to run the import
//      Predicate predicate = AlfrescoUtils.getPredicate(targetFolder);
//
//
//      NamedValue urlListParam = new NamedValue("url-list-as-xml",false, xml, null);
//
//      // action web service crashes if you don't have a parameter, even
//      // if your action takes no parameters - doh
//      NamedValue[] parameters = new NamedValue[]{urlListParam};        
//      Action newAction1 = new Action();
//      newAction1.setActionName("import-urllist");
//      newAction1.setTitle("Import Taxonomy");
//      newAction1.setDescription("This will import a tax into the same folder as the tax file.");
//      newAction1.setParameters(parameters);        
//
//      // Execute the action
//      AlfrescoWebServiceFactory.getActionService().executeActions(predicate, new Action[]{newAction1});
//
//      // remove the .tax file (I moved this to server in case of timeout)
//      //this.deleteResource(savedTaxFile.getPath());
//
//    } catch (Exception e) {
//      logger.error("testUrlImportAction failed.", e);
//
//      // TODO: handle this better
//      throw new ResourceException("testUrlImportAction failed.", e);
//
//    } finally {
//      if (reader != null) {
//        try {
//          reader.close();
//        } catch (Exception e) {
//          logger.error("error during testUrlImportAction: ", e);
//        }
//      }     
//    }
//  }

  public void testUrlImportWebService(CmsPath targetFolder, List urlList) throws ResourceException {
    BufferedReader reader = null;

    Logger logger = CatLogger.getLogger(ResourceService.class);
    try {
      String xml = XmlUtility.serialize(urlList);

      // Create the action to run the import
      Reference reference = AlfrescoUtils.getReference(targetFolder);


      // Execute the action
      //AlfrescoWebServiceFactory.getImportService().testMethod(xml, reference);

      // remove the .tax file (I moved this to server in case of timeout)
      //this.deleteResource(savedTaxFile.getPath());

    } catch (Exception e) {
      logger.error("testUrlImportAction failed.", e);

      // TODO: handle this better
      throw new ResourceException("testUrlImportAction failed.", e);

    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e) {
          logger.error("error during testUrlImportAction: ", e);
        }
      }     
    }
  }

  public void testImportWebService() throws ResourceException {
    BufferedReader reader = null;

    Logger logger = CatLogger.getLogger(ResourceService.class);
    try {



      // Execute the action
      //		AlfrescoWebServiceFactory.getImportService().testMethod();

      // remove the .tax file (I moved this to server in case of timeout)
      //this.deleteResource(savedTaxFile.getPath());

    } catch (Exception e) {
      logger.error("testUrlImportAction failed.", e);

      // TODO: handle this better
      throw new ResourceException("testUrlImportAction failed.", e);

    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e) {
          logger.error("error during testUrlImportAction: ", e);
        }
      }     
    }
  }

//  public void testGoogleHarvestService() throws ResourceException {
//
//
//    try {
//
//      NamedValue titleParam = new NamedValue(HarvestConstants.PROP_HARVEST_TITLE, false, "test", null);
//      NamedValue targetFolder = new NamedValue(HarvestConstants.PROP_TARGET_REPOSITORY_PATH, false, "/app:company_home/cm:User_x0020_Documents/cm:admin/cm:destination", null);
//      NamedValue phrase = new NamedValue(HarvestConstants.PROP_GOOGLE_EXACT_PHRASE, false, "battelle", null);
//
//
//
//      // action web service crashes if you don't have a parameter, even
//      // if your action takes no parameters - doh
//      NamedValue[] parameters = new NamedValue[]{titleParam, targetFolder, phrase};  
//
//      // get the harvest service
//      HarvestService hservice = HarvestService.getInstance();
//
//      // create a new harvest request
//      HarvestRequest request = hservice.createNewHarvestRequest(null, HarvestConstants.HARVEST_TEMPLATE_GOOGLE, parameters);
//
//      // launch the request
//      hservice.launchHarvest(null, request);
//
//      // remove the .tax file (I moved this to server in case of timeout)
//      //this.deleteResource(savedTaxFile.getPath());
//
//    } catch (Exception e) {
//      logger.error("testHarvestService failed.", e);
//
//      // TODO: handle this better
//
//    } 
//  }
//
//  public void testHarvestService() throws ResourceException {
//
//
//    try {
//
//      NamedValue titleParam = new NamedValue(HarvestConstants.PROP_HARVEST_TITLE, false, "test", null);
//      NamedValue targetFolder = new NamedValue(HarvestConstants.PROP_TARGET_REPOSITORY_PATH, false, "/app:company_home/cm:User_x0020_Documents/cm:admin/cm:destination", null);
//
//      String[] websites = new String[]{"http://www.pnl.gov"};
//      NamedValue websitesParam = new NamedValue(HarvestConstants.PROP_WEB_URL_LIST, true, null, websites);
//
//
//
//      // action web service crashes if you don't have a parameter, even
//      // if your action takes no parameters - doh
//      NamedValue[] parameters = new NamedValue[]{titleParam, targetFolder, websitesParam};  
//
//      // get the harvest service
//      HarvestService hservice = HarvestService.getInstance();
//
//      // create a new harvest request
//      HarvestRequest request = hservice.createNewHarvestRequest(null, HarvestConstants.HARVEST_TEMPLATE_WEB, parameters);
//
//      // launch the request
//      hservice.launchHarvest(null, request);
//
//      // remove the .tax file (I moved this to server in case of timeout)
//      //this.deleteResource(savedTaxFile.getPath());
//
//    } catch (Exception e) {
//      logger.error("testHarvestService failed.", e);
//
//      // TODO: handle this better
//
//    } 
//  }





//  public void testUPATransformAction() throws ResourceException {
//
//    Logger logger = CatLogger.getLogger(ResourceService.class);
//    try {
//
//      IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
//
//      CmsPath dropBoxPath = new CmsPath("/User Documents/bucket/Bucket Drop Box");
//      IResource dropBox = mgr.getResource(dropBoxPath);
//      String dropBoxReference = AlfrescoUtils.getReferenceString(AlfrescoUtils.CAT_STORE, dropBox);
//      CmsPath filePath = new CmsPath("/User Documents/bucket/Personal Library/CarinaTest.txt");
//
//      // Create the action to run the import
//      Predicate actionedUponNode = AlfrescoUtils.getPredicate(filePath);
//
//      NamedValue dropBoxParam = new NamedValue("drop-box",false, dropBoxReference, null);
//      NamedValue referenceListParam = new NamedValue("reference-list", false, "", null);
//
//      // action web service crashes if you don't have a parameter, even
//      // if your action takes no parameters - doh
//      NamedValue[] parameters = new NamedValue[]{dropBoxParam, referenceListParam};        
//      Action newAction1 = new Action();
//      newAction1.setActionName("upa-transform");
//      newAction1.setTitle("UPA Transform");
//      newAction1.setDescription("This will use UPA to transform file to xml.");
//      newAction1.setParameters(parameters);        
//
//      // Execute the action
//      AlfrescoWebServiceFactory.getActionService().executeActions(actionedUponNode, new Action[]{newAction1});
//
//
//    } catch (Exception e) {
//      logger.error("testUPATransformAction failed.", e);
//
//      // TODO: handle this better
//      throw new ResourceException("testUPATransformAction failed.", e);
//
//    } 
//  }

  /**
   * Pass in the subscription test action you want to run on the server.  
   * Options are:
   * 
   *          "create" - create a repository sub for the node acted upon for user "dave"
       "create-search" - create a search sub for the node acted upon for user "dave"
          "create-rss" - create an rss sub for the node acted upon for user "dave"
              "update" - updates a repository sub, but you better have created on first
              "digest" - manually runs digester for hourly repo subscriptions
           "runsearch" - manually runs search digester for daily frequency 
              "runrss" - manually runs rss digester for hourly frequency
           "getAlerts" - gets alerts for user "dave", and mark last one as "read"
           any thing else - removes all subscriptions for user "dave"
          }
   * @param actionName
   * @throws ResourceException
   */
//  public void testSubscriptionAction(String actionName) throws ResourceException {
//    BufferedReader reader = null;
//
//    Logger logger = CatLogger.getLogger(ResourceService.class);
//    try {
//
//      CmsPath filePath = new CmsPath("/User Documents/");
//      //			CmsPath filePath = new CmsPath("/Reference Library/");
//
//      Predicate predicate = AlfrescoUtils.getPredicate(filePath);
//
//
//
//      NamedValue actionParam = new NamedValue("action", false, actionName, null);
//
//      // action web service crashes if you don't have a parameter, even
//      // if your action takes no parameters - doh
//      NamedValue[] parameters = new NamedValue[]{actionParam};        
//      Action newAction1 = new Action();
//      newAction1.setActionName("subscription-action");
//      newAction1.setTitle("");
//      newAction1.setDescription("");
//      newAction1.setParameters(parameters);        
//
//      // Execute the action
//      AlfrescoWebServiceFactory.getActionService().executeActions(predicate, new Action[]{newAction1});
//
//      // remove the .tax file (I moved this to server in case of timeout)
//      //this.deleteResource(savedTaxFile.getPath());
//
//    } catch (Exception e) {
//      logger.error("Call to subscription-action failed.", e);
//
//      // TODO: handle this better
//      throw new ResourceException("Call to subscription-action failed.", e);
//
//    } finally {
//      if (reader != null) {
//        try {
//          reader.close();
//        } catch (Exception e) {
//          logger.error("Call to subscription-action failed: ", e);
//        }
//      }     
//    }
//  }

  public void testSubscriptionWebService() throws ResourceException {
    try {
      IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();

      // get a list of all delivery channels available
//      DeliveryChannel[] channels = AlfrescoWebServiceFactory.getSubscriptionService().getDeliveryChannels();

      // get a list of all subscription types available
//      SubscriptionType[] types = AlfrescoWebServiceFactory.getSubscriptionService().getSubscriptionTypes();

      // create a new subscription
      Subscription sub = new Subscription();

      // randomly select the first delivery channel[0]
      String[] selectedChannels = new String[1];
      selectedChannels[0] = VeloConstants.SUBSCRIPTION_CHANNEL_REPOSITORY;
      sub.setDeliveryChannel(selectedChannels);

      // select the second subscription type
      sub.setFrequency(VeloConstants.SUBSCRIPTION_FREQ_DAILY);

      // set some other subscription params
      // add currentTimeMillis to the end to create a unique name for testing
      sub.setName("My Subscription " + System.currentTimeMillis());
      SubscriptionOwner owner = new SubscriptionOwner();
      owner.setId("dave");
      owner.setType("user");
      sub.setOwner(owner);
      sub.setTitle("Title of my susbscription");

      // set the node we want to watch
      CmsPath resourcePath = new CmsPath("/Temporary Files/");
      IResource resource = mgr.getResource(resourcePath);
      String resourceReference = AlfrescoUtils.getReferenceString(AlfrescoUtils.CAT_STORE, resource.getPropertyAsString(VeloConstants.PROP_UUID));

      // define the change types
      String[] changeTypes = new String[3];
      changeTypes[0] = AlertingConstants.CHANGE_TYPE_DELETED;
      changeTypes[1] = AlertingConstants.CHANGE_TYPE_NEW;
      changeTypes[2] = AlertingConstants.CHANGE_TYPE_MODIFIED;

      // define the properties on this subscription
      org.alfresco.webservice.types.NamedValue[] props = new org.alfresco.webservice.types.NamedValue[] {
          org.alfresco.webservice.util.Utils.createNamedValue(AlertingConstants.PROP_SUB_REP_INCLUDE_CHILDREN, "true"),
          org.alfresco.webservice.util.Utils.createNamedValue(AlertingConstants.PROP_SUB_REP_CHANGE_TYPE, changeTypes),
          org.alfresco.webservice.util.Utils.createNamedValue(AlertingConstants.PROP_SUB_REP_SUBSCRIPTION_NODE, resourceReference)
      };
      sub.setProperties(props);

      // set the subscrioption type based on the type selected above
      //sub.setType(type.getName());

      // or use a constant to set the subscription type
      sub.setType(AlertingConstants.TYPE_SUBSCRIPTION_REPOSITORY_FACTSHEET);

//      AlfrescoWebServiceFactory.getSubscriptionService().createSubscription(sub);
      mgr.createSubscription(sub);
      System.out.println("subscription created");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }  

//  public void testSendToTaxonomyAction() throws ResourceException {
//
//    Logger logger = CatLogger.getLogger(ResourceService.class);
//    try {
//
//      IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
//
//      CmsPath taxonomyPath = new CmsPath("/Temporary Files/Test Taxonomy");
//      IResource tax = mgr.getResource(taxonomyPath);
//      String taxReference = AlfrescoUtils.getReferenceString(AlfrescoUtils.CAT_STORE, tax);
//      CmsPath filePath = new CmsPath("/Temporary Files/CarinaTest.txt");
//
//      // Create the action to run the import
//      Predicate actionedUponNode = AlfrescoUtils.getPredicate(filePath);
//
//      NamedValue taxParam = new NamedValue("taxonomy-ref",false, taxReference, null);
//      NamedValue referenceListParam = new NamedValue("reference-list", false, "", null);
//
//      // action web service crashes if you don't have a parameter, even
//      // if your action takes no parameters - doh
//      NamedValue[] parameters = new NamedValue[]{taxParam, referenceListParam};        
//      Action newAction1 = new Action();
//      newAction1.setActionName("send-to-taxonomy");
//      newAction1.setTitle("Send to Taxonomy");
//      newAction1.setDescription("Send to taxonomy.");
//      newAction1.setParameters(parameters);        
//
//      // Execute the action
//      AlfrescoWebServiceFactory.getActionService().executeActions(actionedUponNode, new Action[]{newAction1});
//
//
//    } catch (Exception e) {
//      logger.error("testSendToTaxonomyAction failed.", e);
//
//      // TODO: handle this better
//      throw new ResourceException("testUPATransformAction failed.", e);
//
//    } 
//  }

//  /**
//   * Example code how to look up the raw text node.
//   */
//  public void testGetRawText() throws Exception {
//    // Spaces Store
//    Store store = new Store(Constants.WORKSPACE_STORE, "SpacesStore");
//
//    // A reference to the text node from the document "testFile.doc"
//    // (text transform is always the original file name concatenated with .txt)
//    String textPath = "/app:company_home/cm:testFile.doc/cat:Transforms/cat:testFile.doc.txt";
//    Reference textRef = new Reference(store, null, textPath);
//
//    // Get the content service
//    ContentServiceSoapBindingStub contentService = AlfrescoWebServiceFactory.getContentService();        
//
//    String propertyName = "{http://www.pnl.gov/cat/model/content/1.0}transformedContent";
//
//    // Read the content from the respository
//    Content[] contents = contentService.read(new Predicate(new Reference[]{textRef}, store, null),
//        propertyName);
//
//    Content content = contents[0];
//    // NOTE: since we are using a non-standard content property, we must add this property to the URL,
//    // since Alfresco does not include it by default
//    String url = content.getUrl();
//    url = url + "?property=" + propertyName;
//    content.setUrl(url);
//
//    // Get the content as a String
//    String rawText = AlfrescoContentUtils.getContentAsString(content);
//
//    // OR, get the content as a stream
//    InputStream textStream = AlfrescoContentUtils.getContentAsInputStream(content);
//  }

//  public void testMove() throws Exception {
//    // Spaces Store
//    Store store = new Store(Constants.WORKSPACE_STORE, "SpacesStore");
//
//    // A reference to the node being moved (e.g., a category)
//    String categoryUUID = ""; // get the UUID from the existing category Node
//    Reference categoryRef = new Reference(store, categoryUUID, null);
//    Predicate predicate = new Predicate(new Reference[]{categoryRef}, store, null);
//
//    // A reference to the new association
//    String parentUUID = ""; // Get the UUID for the category we are moving under
//    String categoryName = ""; // get the cm:name property from the existing category Node being moved
//    String fullyQualifiedCategoryName = Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, categoryName);
//    String ASSOC_SUBCATEGORIES = "{http://www.alfresco.org/model/content/1.0}subcategories";
//    ParentReference parentRef = new ParentReference(store, parentUUID, null, ASSOC_SUBCATEGORIES, fullyQualifiedCategoryName);    
//
//    // Perform the move - NOTE, this only changes the child association name, the cm:name property must be changed separately!
//    CML cml = new CML();
//    CMLMove move = new CMLMove(parentRef, null, ASSOC_SUBCATEGORIES, categoryName, predicate, null);
//    cml.setMove(new CMLMove[]{move});
//
//    // Also change the cm:name property on the moved node, to be consistent with newFolderName as set in the child association
//    NamedValue[] properties = new NamedValue[]{Utils.createNamedValue(Constants.PROP_NAME, categoryName)};
//    CMLUpdate cmlUpdate = new CMLUpdate(properties, predicate, null);
//    cml.setUpdate(new CMLUpdate[]{cmlUpdate});
//
//    AlfrescoWebServiceFactory.getRepositoryService().update(cml);
//
//  }

//  public void testUpdateUserProperty() throws Exception{
//    // Spaces Store
//    Store store = new Store(Constants.WORKSPACE_STORE, "SpacesStore");
//
//    // Create a query to find the current user node
//    String currentUserName = "admin";
//    StringBuilder queryBuilder = new StringBuilder();
//    queryBuilder.append("+TYPE:\"");
//    queryBuilder.append("{http://www.alfresco.org/model/content/1.0}person");
//    queryBuilder.append("\"");
//    queryBuilder.append(" +@\\{http\\://www.alfresco.org/model/content/1.0\\}userName:\"");
//    queryBuilder.append(currentUserName);
//    queryBuilder.append("\"");
//
//    Query query = new Query(Constants.QUERY_LANG_LUCENE, queryBuilder.toString());
//    Predicate predicate = new Predicate(null, store, query);
//
//    // change a property value for the user the query is pointing to
//    NamedValue[] properties = new NamedValue[]{
//        // Put the current task property here
//        Utils.createNamedValue(Constants.PROP_USER_LASTNAME, "New Last Name")
//    };
//
//    CML cml = new CML();
//    CMLUpdate cmlUpdate = new CMLUpdate(properties, predicate, null);
//    cml.setUpdate(new CMLUpdate[]{cmlUpdate});
//    AlfrescoWebServiceFactory.getRepositoryService().update(cml);
//
//  }

  public void testLookupAspect() throws Exception {

//    ClassPredicate aspects = new ClassPredicate(
//        new String[]{"cat:transformable"}, // need to use prefixes for dictionary service
//        false,
//        false);
//
//    ClassDefinition[] defs = AlfrescoWebServiceFactory.getDictionaryService().getClasses(null, aspects);

  }

//  public void testCopyTemplate() throws Exception {
//    // Spaces Store
//    Store store = new Store(Constants.WORKSPACE_STORE, "SpacesStore");
//
//    // A reference to the folder being copied (i.e., the space template)
//    String templatePath = "/app:company_home/app:dictionary/app:space_templates/cm:TeamHomeFolder";
//    Reference templateRef = new Reference(store, null, templatePath);
//    Predicate predicate = new Predicate(new Reference[]{templateRef}, store, null);
//
//    // A reference to the new parent folder where the folder will be copied
//    String newFolderName = "testFolder";
//    String destinationFolderPath = "/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/content/1.0}User Spaces/{http://www.alfresco.org/model/content/1.0}Test";
//
//    String fullyQualifiedNewFolderName = Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, newFolderName);
//    ParentReference parentRef = new ParentReference(store, null, destinationFolderPath, Constants.ASSOC_CONTAINS, fullyQualifiedNewFolderName);
//
//    // Perform the copy AND change the cm:name property in one transaxtion
//    CML cml = new CML();
//    CMLCopy copy = new CMLCopy(parentRef, null, Constants.ASSOC_CONTAINS, newFolderName, predicate, null, true);
//    cml.setCopy(new CMLCopy[]{copy});
//    AlfrescoWebServiceFactory.getRepositoryService().update(cml);
//  }

  public void testClassificationWebService() throws Exception {

    // Spaces Store
    Store store = new Store(Constants.WORKSPACE_STORE, "SpacesStore");

    // A reference to the file where we want to set the category
    String filePath = "/app:company_home/cm:patchlog.txt";
    Reference fileRef = new Reference(store, null, filePath);
    Predicate predicate = new Predicate(new Reference[]{fileRef}, store, null);

    //ClassificationServiceSoapBindingStub classificationService = AlfrescoWebServiceFactory.getClassificationService();

    // Get the category we want to add - cm:classifiable
    // Alfresco returns one Classification object for each root category - so for my test server,
    // I have 5 taxonomy categories and 1 cm:generalclassifiable
    //Classification[] classifications = classificationService.getClassifications(store);
    //String classification = classifications[5].getClassification();
    //Reference category = classifications[5].getRootCategory().getId();

    // Set the category to our test node
    //    AppliedCategory appliedCategory = new AppliedCategory();
    //    appliedCategory.setCategories(new Reference[]{category});
    //    appliedCategory.setClassification(classification);    
    //    AppliedCategory[] appliedCategories = new AppliedCategory[]{appliedCategory};
    //    classificationService.setCategories(predicate, appliedCategories);

    // Now remove the category
    //AppliedCategory appliedCategory = new AppliedCategory();
    //appliedCategory.setCategories(new Reference[]{});
    //appliedCategory.setClassification(classification);    
    //AppliedCategory[] appliedCategories = new AppliedCategory[]{appliedCategory};
    //classificationService.setCategories(predicate, appliedCategories);    
  }


  public void testSetOwner() throws Exception {
    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
    ICatCML cml = mgr.getCML();
    CmsPath ownerFolderPath = new CmsPath("/FSFA/Projects");
    IResource test = mgr.getResource(ownerFolderPath);
    String namespace = "http://www.alfresco.org/model/content/1.0";
    String name = "owner";
    String qName = "{" + namespace + "}" + name;
    cml.addAspect(test.getPath(), "{http://www.alfresco.org/model/content/1.0}ownable");
    cml.setProperty(test.getPath(), qName, "GROUP_/FSFA");
    mgr.executeCml(cml);
  }
  
//  public void testCrawlerAction() throws Exception {
//    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
//
//    // Run the action on the User Documents tree
//    CmsPath userDocsPath =  new CmsPath("/FSFA/Categories");
//    IResource userDocs = mgr.getResource(userDocsPath);
//    Predicate actionedUponNode = AlfrescoUtils.getPredicate(userDocsPath);
//
//    NamedValue visitorParam = new NamedValue("visitor-id",false, "ownableNodeVisitor", null);
//    NamedValue transactionParam = new NamedValue("transaction-mode", false, "true", null);
//
//    // action web service crashes if you don't have a parameter, even
//    // if your action takes no parameters - doh
//    NamedValue[] parameters = new NamedValue[]{visitorParam, transactionParam};        
//    Action newAction1 = new Action();
//    newAction1.setActionName("tree-crawler");
//    newAction1.setTitle("Tree Crawler");
//    newAction1.setDescription("Tree Crawler.");
//    newAction1.setParameters(parameters);        
//
//    // Execute the action
//    AlfrescoWebServiceFactory.getActionService().executeActions(actionedUponNode, new Action[]{newAction1});
//
//
//  }

  public void testSubscriptionTestAction() throws Exception {
    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();

    // Run the action on the User Documents tree
    CmsPath userDocsPath = new CmsPath("/");
    IResource userDocs = mgr.getResource(userDocsPath);
    org.alfresco.webservice.types.Predicate actionedUponNode = AlfrescoRepoWebserviceUtils.getPredicate(userDocsPath);

    // create
    // create-search
    // create-rss
    // update
    // digest
    // runsearch
    // runrss
    // getAlerts
    // deleteall
    org.alfresco.webservice.types.NamedValue actionParam = new org.alfresco.webservice.types.NamedValue("action",false, "runsearch", null);

    // action web service crashes if you don't have a parameter, even
    // if your action takes no parameters - doh
    org.alfresco.webservice.types.NamedValue[] parameters = new org.alfresco.webservice.types.NamedValue[]{actionParam};        
    Action newAction1 = new Action();
    newAction1.setActionName("subscription-action");
    newAction1.setTitle("Tree Crawler");
    newAction1.setDescription("Tree Crawler.");
    newAction1.setParameters(parameters);        

    // Execute the action
    mgr.executeActions(actionedUponNode, new Action[]{newAction1});


  }

  public void runSearchBatchSizeOptimizerTest() throws Exception
  {
//    int batchSize = 10;
//    ISearchManager mgr = ResourcesPlugin.getDefault().getSearchManager();
//
//    do {
//      //String query = "TEXT:(china) AND TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
//      String query = "TEXT:(china)";
//
//      long start = System.currentTimeMillis();     
//      ICatQueryResult result = mgr.queryAll(ISearchManager.LANGUAGE_LUCENE, query, new Integer(batchSize));
//      long end = System.currentTimeMillis();
//      int hits = result.getResources().size();
//      System.out.println("ms to load all " + hits + " search results = " + (end - start));
//      batchSize += 10;
//    } while(batchSize <= 50);


  }

//  public void testNotificationFlood() throws Exception {
//    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
//
//    // Run the action on the User Documents tree
//    CmsPath userDocsPath = new CmsPath("/Temporary Files");
//    IResource userDocs = mgr.getResource(userDocsPath);
//    Predicate actionedUponNode = AlfrescoUtils.getPredicate(userDocsPath);
//
//    NamedValue visitorParam = new NamedValue("iterations",false, "500", null);
//
//
//    // action web service crashes if you don't have a parameter, even
//    // if your action takes no parameters - doh
//    NamedValue[] parameters = new NamedValue[]{visitorParam};        
//    Action newAction1 = new Action();
//    newAction1.setActionName("event-test");
//    newAction1.setTitle("Tree Crawler");
//    newAction1.setDescription("Tree Crawler.");
//    newAction1.setParameters(parameters);        
//
//    // Execute the action
//    AlfrescoWebServiceFactory.getActionService().executeActions(actionedUponNode, new Action[]{newAction1});
//
//
//  }

//  public void testAddFeaturesAction() throws Exception {
//    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
//
//    // Run the action on the User Documents tree
//    CmsPath targetPath = new CmsPath("/Temporary Files/CarinaTest.txt");
//    Predicate actionedUponNode = AlfrescoUtils.getPredicate(targetPath);
//
//    NamedValue aspectNameParam = new NamedValue("aspect-name",false, "{http://www.pnl.gov/dmi/model/taxonomy/1.0}classification", null);
//
//    // action web service crashes if you don't have a parameter, even
//    // if your action takes no parameters - doh
//    NamedValue[] parameters = new NamedValue[]{aspectNameParam};        
//    Action newAction1 = new Action();
//    newAction1.setActionName("add-features");
//    newAction1.setTitle("Add Features");
//    newAction1.setDescription("Add Features");
//    newAction1.setParameters(parameters);        
//
//    // Execute the action
//    AlfrescoWebServiceFactory.getActionService().executeActions(actionedUponNode, new Action[]{newAction1});
//  }

//  public void testGetTextTransform() throws Exception {
//
//    // Spaces Store
//    Store store = new Store(Constants.WORKSPACE_STORE, "SpacesStore");
//
//    // A reference to the original document "testFile.doc"
//    String filePath = "/app:company_home/cm:testFile.doc";
//    Reference fileRef = new Reference(store, null, filePath);
//
//    // Get the content service
//    ContentServiceSoapBindingStub contentService = AlfrescoWebServiceFactory.getContentService();        
//
//    // The transformed text property
//    String textPropertyName = "{http://www.pnl.gov/cat/model/transform/text/1.0}transformedContent";
//
//    // The error property (will be not null if an error occurred during the transform
//    String errorPropertyName = "{http://www.pnl.gov/cat/model/transform/text/1.0}transformError";    
//
//    // Read the text property content
//    Content[] contents = contentService.read(new Predicate(new Reference[]{fileRef}, store, null),
//        textPropertyName);
//
//    Content content = contents[0];
//    // NOTE: since we are using a non-standard content property, we must add this property to the URL,
//    // since Alfresco does not include it by default
//    String url = content.getUrl();
//    url = url + "?property=" + textPropertyName;
//    content.setUrl(url);
//
//    // Get the content as a String
//    String rawText = AlfrescoContentUtils.getContentAsString(content);
//
//    // OR, get the content as a stream
//    InputStream textStream = AlfrescoContentUtils.getContentAsInputStream(content);
//
//
//  }

//  private void testEZJComOutlookBridge() throws Exception {
//    Application outlookComObject = new Application();
//    _Application outlook = outlookComObject.get_Application();
//
//    Selection selectedEmail = outlook.ActiveExplorer().getSelection();
//    int numEmail = selectedEmail.getCount();
//
//    for (int i = 1; i <= numEmail; i++) {
//      JComObject email = selectedEmail.Item(new JComVariant(i));
//      JComVariant subject = email.JComGetProperty("Subject", null);
//      String subjectStr = subject.getString();
//      System.out.println("Email subject is: " + subjectStr);
//    }
//  }
}