package gov.pnnl.cat.core.test.junit;

import java.io.*;

import static org.junit.Assert.*;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.webservice.subscription.Subscription;
import gov.pnnl.cat.webservice.subscription.SubscriptionOwner;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Comment;
import gov.pnnl.velo.model.Properties;
//import java.util.Properties;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.FileUtils;
import gov.pnnl.velo.util.VeloConstants;

import org.alfresco.webservice.action.Action;
//import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.Store;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.crypto.NodeSetData;



public class TestCatCoreResources {
  protected static IResourceManager resourceManager;
  protected static IFolder childFolder; 
  protected static IResource folderPath;
  protected static Resource foldersPath1 , foldersPath2;
  protected static List<IFolder> folders;
  protected static File file;
  protected static String content, file1Content , file2Content;
  protected static IFile fileCreated;
  protected static IResource fileResource;
  protected static IProgressMonitor monitor;
  protected static Map<File, CmsPath> filesToServerPath , filesToServerPath1;
  protected static Properties prop , prop1;
  protected static Map<String, Properties> propMap;
  protected static List<Resource> resourceList = new ArrayList<Resource>();
  protected static CmsPath junitHomeFolder = new CmsPath("/User Documents/Junit");
  protected static CmsPath testJUnitFolder= junitHomeFolder.append("TestJunit");
  protected static CmsPath sourceFolder= testJUnitFolder.append("SourceFolder");
  protected static CmsPath sourceSubFolder= sourceFolder.append("SourceSubFolder");
  protected static CmsPath destFolder= testJUnitFolder.append("DestFolder");

  protected static CmsPath destFolderOverwrite= testJUnitFolder.append("DestFolderOverwrite");

  protected static CmsPath destSubFolderOverwrite= destFolderOverwrite.append("DestSubFolder");
  protected static CmsPath moveOldPath = testJUnitFolder.append("MoveOldFolder");
  protected static CmsPath moveOldPathContent = moveOldPath.append("MoveOldSubFolder");
  protected static CmsPath moveNewPath = testJUnitFolder.append("MoveNewFolder");
  protected static CmsPath testDelete1= testJUnitFolder.append("TestDelete1");
  protected static CmsPath testDelete2= testJUnitFolder.append("TestDelete2");
  protected static CmsPath testDelete3= testJUnitFolder.append("TestDelete3");
  protected static CmsPath testDelete4= testJUnitFolder.append("TestDelete4");
  protected static CmsPath testDelete5= testJUnitFolder.append("TestDelete5");
  protected static CmsPath testDelete6= testJUnitFolder.append("TestDelete6");
  protected static CmsPath testProperty= testJUnitFolder.append("TestProperty");
  protected static CmsPath testPropertyStringPath= testJUnitFolder.append("TestPropertyStringPath");
  protected static CmsPath testProperties= testJUnitFolder.append("TestProperties");
  protected static CmsPath testRemoveProperties= testJUnitFolder.append("TestRemoveProperties");

  protected static CmsPath testMultiProperties= testJUnitFolder.append("TestMultiProperties");
  protected static CmsPath testUpdateLink= testJUnitFolder.append("TestUpdateLink");
  protected static List<CmsPath> deleteList1 , deleteList2;
  protected static CmsPath testFolder = junitHomeFolder.append("TestingFolder");
  protected static CmsPath testFolderRecursive = junitHomeFolder.append("TestingFolderRecursive");
  protected static CmsPath testFolderMimetype = junitHomeFolder.append("TestingFolderMimetype");
  protected static CmsPath testProjectMimetype = junitHomeFolder.append("TestingProjectMimetype");
  protected static CmsPath testResourcePath = junitHomeFolder.append("TestingResource");
  protected static CmsPath testFoldersPath1 = junitHomeFolder.append("TestingFoldersResource1");
  protected static CmsPath testFoldersPath2 = junitHomeFolder.append("TestingFoldersResource2");
  protected static ILinkedResource newLink;
  protected static Predicate actionedUponNode;
  protected static Action actions[];
  protected static Properties properties;
  public static String PROP_VISITOR = "visitor-id";
  //true if action on each node needs separate txn. false other wise
  public static String PROP_TXN = "transaction-mode"; 
  //path to start with. Start path after Company Home
  public static String PROP_START_PATH = "start-path";



  @BeforeClass
  public static void init() throws Exception
  {
    resourceManager = VeloTestUtils.initializeResourceManager();
    // test create file


    resourceManager.createFolder(testJUnitFolder);
    resourceManager.createFolder(sourceFolder);
    resourceManager.createFolder(sourceSubFolder);
    resourceManager.createFolder(destFolderOverwrite);
    resourceManager.createFolder(destSubFolderOverwrite);
    resourceManager.createFolder(moveOldPath);

    resourceManager.createFolder(moveOldPathContent);
    resourceManager.createFolder(testProperty);
    resourceManager.createFolder(testPropertyStringPath);
    resourceManager.createFolder(testProperties);
    resourceManager.createFolder(testMultiProperties);
    resourceManager.createFolder(testUpdateLink);
    resourceManager.createFolder(testRemoveProperties);
    //Test deleteResource methods
    resourceManager.createFolder(testDelete1);
    resourceManager.createFolder(testDelete2);
    resourceManager.createFolder(testDelete3);
    resourceManager.createFolder(testDelete4);
    resourceManager.createFolder(testDelete5);
    resourceManager.createFolder(testDelete6);
    deleteList1 = new ArrayList<CmsPath>();
    deleteList1.add(testDelete3);
    deleteList1.add(testDelete4);
    deleteList2 = new ArrayList<CmsPath>();
    deleteList2.add(testDelete5);
    deleteList2.add(testDelete6);

    /* //Test execute action
    String visitorName = properties.getProperty(PROP_VISITOR);
    String txPerNode =  properties.getProperty(PROP_TXN);
    String startPath =  properties.getProperty(PROP_START_PATH);
    CmsPath userDocsPath = new CmsPath(startPath);


    NamedValue visitorParam = new NamedValue(PROP_VISITOR,false, visitorName, null);
    NamedValue txParam = new NamedValue(PROP_TXN,false, txPerNode, null);
    actionedUponNode = AlfrescoRepoWebserviceUtils.getPredicate(userDocsPath);
    NamedValue[] parameters = new NamedValue[]{visitorParam, txParam};        
    Action newAction1 = new Action();
    newAction1.setActionName("tree-crawler");
    newAction1.setTitle("Tree Crawler");
    newAction1.setDescription("Tree Crawler.");
    newAction1.setParameters(parameters);     */

    resourceManager.createFolder(testResourcePath);
    folderPath = resourceManager.getResource(testResourcePath);
    foldersPath1 = new Resource(VeloConstants.TYPE_FOLDER, testFoldersPath1);

    foldersPath2 = new Resource(VeloConstants.TYPE_FOLDER, testFoldersPath2);

    resourceList.add(foldersPath1);
    resourceList.add(foldersPath2);

    File file1 = new File("./test_files/test.xml");
    filesToServerPath = new HashMap<File, CmsPath>();
    filesToServerPath.put(file1, testJUnitFolder.append("junitTestUpload.xml"));
    file1Content = new Scanner(file1).useDelimiter("\\A").next();

    File file2 = new File("./test_files/test.csv");
    filesToServerPath1 = new HashMap<File, CmsPath>();
    filesToServerPath1.put(file2, testJUnitFolder.append("junitTest.csv"));
    file2Content = new Scanner(file2).useDelimiter("\\A").next();
    prop = new Properties();
    prop1 = new Properties();
    propMap = new HashMap<String, Properties>();
    prop.setProperty(VeloConstants.PROP_AUTHOR, "testPropName");
    prop1.setProperty(VeloConstants.PROP_DESCRIPTION, "testPropDescription");
    propMap.put("junitTest.csv", prop1);





  }



  @Before
  public void setUp() throws IOException
  {
    file = new File("testFile.txt");
    content = "this is some sample content";
    FileUtils.writeStringToFile(file, content);
    fileResource = resourceManager.getResource(testJUnitFolder);

  }



  @AfterClass
  public static void destroy() throws Exception {
    resourceManager.deleteResource(testFolder);
    resourceManager.deleteResource(testFolderRecursive);
    resourceManager.deleteResource(testFolderMimetype);
    resourceManager.deleteResource(testProjectMimetype);
    resourceManager.deleteResource(testResourcePath);
    resourceManager.deleteResource(childFolder.getPath());
    for (IFolder folder : folders)
    {
      resourceManager.deleteResource(folder.getPath());
    }

    resourceManager.deleteResource(testJUnitFolder);
    resourceManager.deleteResource(testJUnitFolder.append("junitTestUpload.xml"));
    resourceManager.deleteResource(testJUnitFolder.append("junitTest.csv"));
    resourceManager.deleteResource(destFolder);
    resourceManager.deleteResource(destFolderOverwrite);
    resourceManager.deleteResource(sourceFolder);
    resourceManager.deleteResource(moveNewPath);
    resourceManager.deleteResource(moveOldPath);
    resourceManager.deleteResource(testProperty);
    resourceManager.deleteResource(testPropertyStringPath);
    resourceManager.deleteResource(testProperties);
    resourceManager.deleteResource(testMultiProperties);
    resourceManager.deleteResource(testUpdateLink);
    resourceManager.deleteResource(testRemoveProperties);
  }


  @After  
  public void tearDown()
  {
    if(fileCreated != null)
    {
      resourceManager.deleteResource(fileCreated.getPath());
    }
  }

  @Test  
  public void testCreateFolderCmsPath()
  {

    resourceManager.createFolder(testFolder);

    assertTrue(resourceManager.resourceExists(testFolder));
  }

  @Test  
  @Ignore  
  //gets access denied error when recursive is set to True
  public void testCreateFolderCmsPathRecursive()
  {

    resourceManager.createFolder(testFolderRecursive, true);

    assertTrue(resourceManager.resourceExists(testFolderRecursive));
  }

  @Test 
  public void testCreateFolderCmsPathMimetype()
  {

    resourceManager.createFolder(testFolderMimetype, "cmsfile/folder");

    assertTrue(resourceManager.resourceExists(testFolderMimetype));
  }

  @Test 
  public void testCreateFolderResource()
  {

    childFolder= resourceManager.createFolder(folderPath, "childFolder");

    assertTrue(resourceManager.resourceExists(childFolder.getPath()));
  }

  @Test 
  public void testCreateFolders()
  {

    folders = resourceManager.createFolders(resourceList);

    for (IFolder folder : folders)
    {
      assertTrue(resourceManager.resourceExists(folder.getPath()));
    }

  }

  @Test
  public void testCreateFileCmsPath()
  {
    fileCreated = resourceManager.createFile(testJUnitFolder.append(file.getName()), file);

    assertTrue(file.getName().equals(fileCreated.getName()));
    assertTrue(content.equals(new Scanner(fileCreated.getContent()).useDelimiter("\\A").next()));

  }

  @Test
  public void testCreateFileResource()
  {
    fileCreated = resourceManager.createFile(fileResource, file);

    assertTrue(file.getName().equals(fileCreated.getName()));
    assertTrue(content.equals(new Scanner(fileCreated.getContent()).useDelimiter("\\A").next()));

  }

  @Test
  public void testBulkUpload()
  {
    resourceManager.bulkUpload(filesToServerPath, monitor);
    assertTrue(resourceManager.resourceExists(testJUnitFolder.append("junitTestUpload.xml")));
    assertTrue(file1Content.equals(new Scanner(resourceManager.getContentProperty(testJUnitFolder.append("junitTestUpload.xml"), VeloConstants.PROP_CONTENT)).useDelimiter("\\A").next()));

  }

  @Test
  public void testBulkUploadProperties()
  {
    resourceManager.bulkUpload(filesToServerPath1, prop, propMap, null, true);
    assertTrue(resourceManager.resourceExists(testJUnitFolder.append("junitTest.csv")));
    assertTrue(file2Content.equals(new Scanner(resourceManager.getContentProperty(testJUnitFolder.append("junitTest.csv"), VeloConstants.PROP_CONTENT)).useDelimiter("\\A").next()));
    assertTrue("testPropName".equals(resourceManager.getProperty(testJUnitFolder.append("junitTest.csv"), VeloConstants.PROP_AUTHOR)));
    assertTrue("testPropDescription".equals(resourceManager.getProperty(testJUnitFolder.append("junitTest.csv"), VeloConstants.PROP_DESCRIPTION)));
  }

  @Test
  public void testCreateProject()
  {
    resourceManager.createProject(testProjectMimetype, VeloConstants.MIMETYPE_PROJECT);
    assertTrue(resourceManager.resourceExists(testProjectMimetype));

  }


  @Test 
  public void testCreateTaxonomy()
  {
    resourceManager.createTaxonomy(testResourcePath);    
    for (IFolder taxonomy : resourceManager.getTaxonomies()) {
      assertTrue(taxonomy.isType(IResource.TAXONOMY_FOLDER));
    }   
  }

  @Test 
  public void testCreateLink()
  {
    newLink = resourceManager.createLink(testJUnitFolder.append("newLink"), fileResource);  
    assertTrue(newLink.isType(IResource.LINK));

  }


  @Test 
  public void testDeleteResourceCmsPath()
  {
    resourceManager.deleteResource(testDelete1);  
    assertFalse(resourceManager.resourceExists(testDelete1));       
  }

  @Test 
  public void testDeleteResourceString()
  {
    resourceManager.deleteResource(resourceManager.getProperty(testDelete2, VeloConstants.PROP_UUID));  
    assertFalse(resourceManager.resourceExists(testDelete2));       
  }

  @Test 
  public void testDeleteResourcesList()
  {
    resourceManager.deleteResources(deleteList1);
    for (CmsPath path:deleteList1)
      assertFalse(resourceManager.resourceExists(path));       
  }

  @Test 
  public void testDeleteResourcesOption()
  {
    resourceManager.deleteResources(deleteList2, "force");
    for (CmsPath path:deleteList2)
      assertFalse(resourceManager.resourceExists(path));       
  }

  /*  @Test
  public void testExecuteActions()
  {
    resourceManager.executeActions(predicate, actions);
  } 


  @Test 
  public void testCreateSubscription()
  {

    Store store = new Store(scheme, address);
    Reference node = new Reference(store, resourceManager.getUUID(testJUnitFolder), resourceManager.getUUID(testJUnitFolder).toString());
    SubscriptionOwner owner;
    String title;
    String name;
    String type;
    NamedValue[] properties;
    String[] deliveryChannel;
    String frequency;
    Calendar lastAlertSent;
    Calendar created;
    Subscription subscription = new Subscription(node, owner, title, name, type, properties, deliveryChannel, frequency, lastAlertSent, created);

    resourceManager.createSubscription(subscription);
    for (CmsPath path:deleteList2)
      assertFalse(resourceManager.resourceExists(path));       
  }*/

  @Test  
  public void testAddComment()
  {
    String comment = "this is a comment";
    resourceManager.addComment(testJUnitFolder, comment);
    Comment[] comments = resourceManager.getComments(testJUnitFolder);
    for (Comment com : comments)
    {
      assertTrue(com.getContent().equals(comment));
    }

  }

  @Test
  public void testCopy()
  {
    resourceManager.copy(sourceFolder, destFolder);
    assertTrue(resourceManager.resourceExists(destFolder));
  }

  @Test

  // Bug reported in JIRA AKUNA-355 - Copy method fails when overwrite boolean parameter is set to false
  public void testCopyOverwrite()
  {

    fileCreated = resourceManager.createFile(destSubFolderOverwrite.append(file.getName()), file);
    resourceManager.createFile(sourceSubFolder.append("sourceFile.txt"), file);
    resourceManager.copy(sourceFolder, destFolderOverwrite, true);

    assertTrue(resourceManager.resourceExists(destFolderOverwrite.append("SourceSubFolder")));

  }

  @Test
  public void testMove()
  {
    resourceManager.move(moveOldPath, moveNewPath);
    assertTrue(resourceManager.resourceExists(moveNewPath.append("MoveOldSubFolder")));
  }


  @Test 
  @Ignore
  // Fails, marked in JIRA
  public void testSetStringPropertyUpdateCache() {
    String propName = VeloConstants.PROP_DESCRIPTION;
    String propValue = "Bucket is a cute cat.";
    resourceManager.setProperty(testProperty, propName, propValue);

    // confirm that the value is updated correctly in the cache:
    String newValue = resourceManager.getProperty(testProperty, propName);
    assertTrue(newValue.equals(propValue));
  }

  @Test
  public void testSetStringPropertyUpdateServer() {
    String propName = VeloConstants.PROP_DESCRIPTION;
    String propValue = "Bucket is a cute cat.";
    resourceManager.setProperty(testProperty, propName, propValue);

    // confirm that the value is correct on the server
   
    String newValue = resourceManager.getProperty(testProperty, propName);
    assertTrue(newValue.equals(propValue));

  }

  @Test
  @Ignore
  //Not sure what is IResource value
  public void testSetResourcePropertyUpdateServer() {
    String propName = VeloConstants.PROP_DESCRIPTION;
    //String propValue = "Bucket is a cute cat.";
    IResource res = resourceManager.getResource(testProperty);
    resourceManager.setProperty(testProperty, propName, res);

    // confirm that the value is correct on the server
    
    String newValue = resourceManager.getProperty(testProperty, propName);
    System.out.println(newValue);
    // assertTrue(newValue.equals(res));

  }

  @Test
  @Ignore
  //Created bug in Jira - AKUNA-358
  public void testSetStringPathPropertyUpdateCache() {
    String propName = VeloConstants.PROP_DESCRIPTION;
    String propValue = "Bucket is a cute cat.";

    resourceManager.setProperty("/User Documents/Junit/TestJunit/TestPropertyStringPath", propName, propValue);
    // confirm that the value is correct on the cache 
    String newValue = resourceManager.getProperty("/User Documents/Junit/TestJunit/TestPropertyStringPath", propName);

    assertTrue(newValue.equals(propValue));

  }

  @Test
  public void testSetStringPathPropertyUpdateServer() {
    String propName = VeloConstants.PROP_DESCRIPTION;
    String propValue = "Bucket is a cute cat.";

    resourceManager.setProperty("/User Documents/Junit/TestJunit/TestPropertyStringPath", propName, propValue);
    // confirm that the value is correct on the server
    
    String newValue = resourceManager.getProperty("/User Documents/Junit/TestJunit/TestPropertyStringPath", propName);

    assertTrue(newValue.equals(propValue));

  }
  @Test
  @Ignore
  //Created bug in Jira - AKUNA-358
  public void testSetPropertiesUpdateCache()
  {
    String propDesc = VeloConstants.PROP_DESCRIPTION;
    String propAuthor = VeloConstants.PROP_AUTHOR;

    Map<String, String> properties = new HashMap<String, String>();
    properties.put(propDesc, "setProperties description");
    properties.put(propAuthor , "Cleopatra");

    resourceManager.setProperties(testProperties, properties);
    String desc = resourceManager.getProperty(testProperties, propDesc);
    String author = resourceManager.getProperty(testProperties, propAuthor);
    assertTrue(desc.equals("setProperties description"));
    assertTrue(author.equals("Cleopatra"));
  }

  @Test
  public void testSetPropertiesUpdateServer()
  {
    String propDesc = VeloConstants.PROP_DESCRIPTION;
    String propAuthor = VeloConstants.PROP_AUTHOR;

    Map<String, String> properties = new HashMap<String, String>();
    properties.put(propDesc, "setProperties description");
    properties.put(propAuthor , "Cleopatra");

    resourceManager.setProperties(testProperties, properties);
   
    String desc = resourceManager.getProperty(testProperties, propDesc);
    String author = resourceManager.getProperty(testProperties, propAuthor);
    assertTrue(desc.equals("setProperties description"));
    assertTrue(author.equals("Cleopatra"));
  }


  @Test
  @Ignore
  //Created bug in Jira - AKUNA-358
  //Fails- NullPointerException as get method returns null
  public void testSetMultiValuedPropertiesUpdateCache()
  {
    List<String> valuesList1 = new ArrayList<String>();
    valuesList1.add("desciption1");
    valuesList1.add("description2");
    String propDesc = VeloConstants.PROP_DESCRIPTION;
    Map<String, List<String>> multiProperties = new HashMap<String, List<String>>();
    multiProperties.put(propDesc, valuesList1);
    resourceManager.setMultiValuedProperties(testMultiProperties, multiProperties );

    String[] values = resourceManager.getMultiValuedProperty(testMultiProperties, propDesc);
    assertTrue(values.equals(valuesList1));

  }
  @Test
  @Ignore
  //Created bug in Jira - AKUNA-358
  public void testSetMultiValuedPropertiesUpdateServer()
  {
    List<String> valuesList1 = new ArrayList<String>();
    valuesList1.add("desciption1");
    valuesList1.add("description2");
    String propDesc = VeloConstants.PROP_DESCRIPTION;
    Map<String, List<String>> multiProperties = new HashMap<String, List<String>>();
    multiProperties.put(propDesc, valuesList1);
    resourceManager.setMultiValuedProperties(testMultiProperties, multiProperties );
   
    String[] values = resourceManager.getMultiValuedProperty(testMultiProperties, propDesc);
    assertTrue(values.equals(valuesList1));

  }

  @Test 
  @Ignore
  //not sure how it works
  public void testUpdateLinkTarget()
  {
    fileCreated = resourceManager.createFile(testJUnitFolder.append(file.getName()), file);
    IFile fileCreated1 = resourceManager.createFile(testJUnitFolder.append("linkfile.txt"), file);
    ILinkedResource updateLink = resourceManager.createLink(testJUnitFolder.append("UpdateLink"), fileCreated);  
    IResource linkTarget = resourceManager.getResource(testUpdateLink);
    resourceManager.updateLinkTarget(testJUnitFolder.append("UpdateLink"), fileCreated1);
    assertTrue(linkTarget.equals(updateLink.getTarget()));

  }

  @Test
  public void testAddAspect()
  {
    String aspect = VeloConstants.ASPECT_AUTHOR;

    resourceManager.addAspect(testJUnitFolder, aspect);
    List<String> aspects = resourceManager.getAspects(testJUnitFolder);

    assertTrue(aspects.contains(aspect));

  }

  @Test
  public void testRemoveAspect()
  {
    String aspect = VeloConstants.ASPECT_PROFILE;

    resourceManager.addAspect(testJUnitFolder, aspect);
    resourceManager.removeAspect(testJUnitFolder, aspect);
    List<String> aspects = resourceManager.getAspects(testJUnitFolder);

    assertFalse(aspects.contains(aspect));

  }

  @Test
  public void testRemoveProperties()
  {
    String propDesc = VeloConstants.PROP_DESCRIPTION;
    String propAuthor = VeloConstants.PROP_AUTHOR;
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(propDesc, "setProperties description");
    properties.put(propAuthor , "Cleopatra");

    resourceManager.setProperties(testRemoveProperties, properties);
    List<String> props = new ArrayList<String>();
    props.add(propDesc);
    props.add(propAuthor);
    resourceManager.removeProperties(testRemoveProperties, props );
    
    String desc = resourceManager.getProperty(testRemoveProperties, propDesc);
    String author = resourceManager.getProperty(testRemoveProperties, propAuthor);
    assertTrue(desc == null);
    assertTrue(author == null);

  }
  
  @Test
  public void testGetRoot()
  {
  IFolder root = resourceManager.getRoot(); 
  String name = root.getName();
  assertTrue(name.equals("company_home"));
  }
  
  @Test
  public void testGetResource()
  {
     IResource res = resourceManager.getResource(testJUnitFolder);
     assertTrue(res.toString().equals("/User Documents/Junit/TestJunit"));
          
  }
  
  @Test
  public void testGetResources()
  {
     List<CmsPath> path = new ArrayList<CmsPath>();
     path.add(testJUnitFolder);
     path.add(junitHomeFolder);
    
    List<IResource> res = resourceManager.getResources(path);
  
   
     assertTrue(res.get(0).toString().equals("/User Documents/Junit/TestJunit"));
     assertTrue(res.get(1).toString().equals("/User Documents/Junit"));
   
     
  }
  
  @Test
  public void testGetResourceString()
  {
	 String uuid = resourceManager.getProperty(testJUnitFolder, VeloConstants.PROP_UUID);
	IResource res = resourceManager.getResource(uuid);
	assertTrue(res.toString().equals("/User Documents/Junit/TestJunit"));
  }
  
  @Test
  public void testResourceExists()
  {
	  assertTrue(resourceManager.resourceExists(testJUnitFolder));
  }
  
  @Test
  public void testResourceExistsString()
  {
	  assertTrue(resourceManager.resourceExists("/User Documents/Junit/TestJunit"));
  }
  
  @Test
  public void testResourceCached()
  {
	  assertTrue(resourceManager.resourceCached(testJUnitFolder));
  }
  
  @Test
  public void testGetTarget()
  {
	   
	 resourceManager.createLink(testJUnitFolder.append("testGetTarget"), fileResource);  
	 IResource target = resourceManager.getTarget(testJUnitFolder.append("testGetTarget"));
	 assertTrue(target.equals(fileResource));
  }
  
  @Test
  public void testGetPropertyAsResource()
  {
	  resourceManager.setProperty(testJUnitFolder, VeloConstants.PROP_AUTHOR, "testGetProperty Author");
	  IResource prop = resourceManager.getPropertyAsResource(testJUnitFolder, "testGetProperty Author");
	  assertTrue(prop.equals(resourceManager.getResource(testJUnitFolder)));
  }
  
}
