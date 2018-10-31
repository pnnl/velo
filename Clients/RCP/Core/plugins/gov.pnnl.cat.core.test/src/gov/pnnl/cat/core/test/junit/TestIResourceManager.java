package gov.pnnl.cat.core.test.junit;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;
import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.SpringContainerInitializer;
import gov.pnnl.velo.util.VeloConstants;

import org.junit.*;

public class TestIResourceManager {
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
	public void folderCreated_pathOnly() {
		CmsPath path = new CmsPath(basePath).append("junitTestFolderCreated1"); // setup
		
		assertFalse(resourceManager.resourceExists(path)); // pre-check
		
		IResource resource = resourceManager.createFolder(path); // create
		
    assertTrue(resourceManager.resourceExists(path)); // verify
    assertTrue(resourceManager.resourceExists(resource.getPath())); // verify again
    
    resourceManager.deleteResource(path); // clean up
    assertFalse(resourceManager.resourceExists(path));
	}
	
	@Test
	@Ignore
	// Fails, ticked in jira AKUNA-317
	public void folderCreated_recursive() {
		CmsPath path1 = new CmsPath(basePath).append("junitTestFolderCreated2"); // setup
		path1.append("anotherFolder");
		
		assertFalse(resourceManager.resourceExists(path1)); // pre-check
		
		IResource resource = resourceManager.createFolder(path1, true); // create
		
    assertTrue(resourceManager.resourceExists(path1)); // verify
    assertTrue(resourceManager.resourceExists(resource.getPath())); // verify again
    
    resourceManager.deleteResource(resource.getPath()); // clean up
    assertFalse(resourceManager.resourceExists(path1));
	}
	
	@Test
	public void folderCreated_resourceAndNameOnly() {
		CmsPath path = new CmsPath(basePath).append("junitTestFolderCreated3"); // setup
		assertFalse(resourceManager.resourceExists(path)); // pre-check
		IResource parent = resourceManager.createFolder(path);
		
		IResource r = resourceManager.createFolder(parent, "ChildFolder"); // create
		
    assertTrue(resourceManager.resourceExists(parent.getPath())); // verify
    assertTrue(resourceManager.resourceExists(r.getPath()));
    
    resourceManager.deleteResource(parent.getPath()); // clean up
    assertFalse(resourceManager.resourceExists(path));
	}
	
	@Test
	public void folderCreated_pathAndMimetype() {
		CmsPath path = new CmsPath(basePath).append("junitTestFolderCreated4"); // setup
		
		assertFalse(resourceManager.resourceExists(path)); // pre-check
		
		IResource resource = resourceManager.createFolder(path, "cmsfile/model"); // create
		
    assertTrue(resourceManager.resourceExists(resource.getPath())); // verify
    
    resourceManager.deleteResource(resource.getPath()); // clean up
    assertFalse(resourceManager.resourceExists(path));
	}
	
	@Test
	@Ignore
	// Needs to be fixed
	public void foldersCreated() {
		CmsPath path1 = new CmsPath(basePath).append("junitTestFolderCreated5"); // setup
		CmsPath path2 = new CmsPath(basePath).append("junitTestFolderCreated6");
		
		assertFalse(resourceManager.resourceExists(path1)); // pre-check
		assertFalse(resourceManager.resourceExists(path2));
		Resource r1 = new Resource(VeloConstants.TYPE_FOLDER, path1);
//		Resource r2 = new Resource();
//		r2.setPath(path2.toAssociationNamePath());
//		r2.setName("junitTestFolderCreated6");
		// could set a whole bunch of stuff
		
		List<Resource> foldersToCreate = new ArrayList<Resource>();
		foldersToCreate.add(r1);
//		foldersToCreate.add(r2);
		
		List<IFolder> folders = resourceManager.createFolders(foldersToCreate);
		
    assertTrue(resourceManager.resourceExists(folders.get(0).getPath())); // verify
//    assertTrue(resourceManager.resourceExists(folders.get(1).getPath()));
    
//    resourceManager.deleteResource(new ArrayList<Resou>)esource(folders); // clean up
//    assertFalse(resourceManager.resourceExists(path));
	}
	
	@Test
	public void fileCreated_usingPath() throws FileNotFoundException {
		CmsPath path = new CmsPath(basePath).append("junitTestFileCreated.xml"); // setup
		File file = new File("./test_files/test.xml");
		String preFileContent = new Scanner(file).useDelimiter("\\A").next();
		
		assertFalse(resourceManager.resourceExists(path)); // pre-check
		
		IFile createdFile = resourceManager.createFile(path, file); // create
		String postFileContent = new Scanner(createdFile.getContent()).useDelimiter("\\A").next();
		
		assertTrue(resourceManager.resourceExists((createdFile).getPath())); // verify
		assertTrue(preFileContent.hashCode() == postFileContent.hashCode());
		
		resourceManager.deleteResource(createdFile.getPath()); // clean up
		assertFalse(resourceManager.resourceExists(path));
	}
	
	@Test
	public void fileCreated_usingResource() throws FileNotFoundException {
		CmsPath path = new CmsPath(basePath); // setup
		File file = new File("./test_files/test.xml");
		String preFileContent = new Scanner(file).useDelimiter("\\A").next();
		IResource parentResource = resourceManager.getResource(path);
		assertFalse(resourceManager.resourceExists(path.append("test.xml"))); // pre-check
		
		IFile createdFile = resourceManager.createFile(parentResource, file); // create
		String postFileContent = new Scanner(createdFile.getContent()).useDelimiter("\\A").next();
		
		assertTrue(resourceManager.resourceExists(createdFile.getPath())); // verify
		assertTrue(preFileContent.hashCode() == postFileContent.hashCode());
		
		// DO NOT DELETE `path` as the whole Junit user would be deleted, rather get createdFile's path 
		resourceManager.deleteResource(createdFile.getPath()); // clean up
		assertFalse(resourceManager.resourceExists(createdFile.getPath()));
	}
	
	@Test
	public void uploadFiles() throws FileNotFoundException {
		CmsPath path1 = new CmsPath(basePath).append("junitTestUpload.xml"); // setup
		File file1 = new File("./test_files/test.xml");
		String preFileContent1 = new Scanner(file1).useDelimiter("\\A").next();
		
		CmsPath path2 = new CmsPath(basePath).append("junitTestUpload.csv");
		File file2 = new File("./test_files/test.csv");
		String preFileContent2 = new Scanner(file2).useDelimiter("\\A").next();
		
		HashMap<File, CmsPath> filesToServerPath = new HashMap<File, CmsPath>();
		filesToServerPath.put(file1, path1);
		filesToServerPath.put(file2, path2);

		assertFalse(resourceManager.resourceExists(path1)); // pre-check
		assertFalse(resourceManager.resourceExists(path2));
		
		resourceManager.bulkUpload(filesToServerPath, null); // create
		String postFileContent1 = new Scanner(resourceManager.getContentProperty(path1, VeloConstants.PROP_CONTENT)).useDelimiter("\\A").next();
		String postFileContent2 = new Scanner(resourceManager.getContentProperty(path2, VeloConstants.PROP_CONTENT)).useDelimiter("\\A").next();

		assertTrue(preFileContent1.hashCode() == postFileContent1.hashCode());
		assertTrue(preFileContent2.hashCode() == postFileContent2.hashCode());
		
		resourceManager.deleteResource(path1); // clean up
		assertFalse(resourceManager.resourceExists(path1));
		resourceManager.deleteResource(path2);
		assertFalse(resourceManager.resourceExists(path2));
	}
	
	@Test
	public void projectCreated(){
		CmsPath path = new CmsPath(basePath).append("junitTestProject"); // setup
		assertFalse(resourceManager.resourceExists(path)); // pre-check
		IFolder createdProject = resourceManager.createProject(path, VeloConstants.MIMETYPE_PROJECT); // create
		assertTrue(resourceManager.resourceExists((createdProject).getPath())); // verify
		resourceManager.deleteResource(createdProject.getPath()); // clean up
		assertFalse(resourceManager.resourceExists(createdProject.getPath()));
	}
	
	@Test
	@Ignore
	public void linkCreated(){
		CmsPath path1 = new CmsPath(basePath).append("junitTest_LinkResourceOrigin/"); // setup the path where the link will go
		CmsPath path2 = new CmsPath(basePath).append("junitTest_LinkResourceDest"); // setup the path where the link will point to
		assertFalse(resourceManager.resourceExists(path1)); // pre-check
		assertFalse(resourceManager.resourceExists(path2));
		IResource resource = resourceManager.createFolder(path2); // setup resource at path2
		
		ILinkedResource link = resourceManager.createLink(path1, resource);  // create
	}
	
	@Test
	@Ignore
	// Where do comments go?
	public void commentAdded(){
		CmsPath path = new CmsPath(basePath).append("junitTestProjectForComment"); // setup
		assertFalse(resourceManager.resourceExists(path)); // pre-check
		IFolder createdProject = resourceManager.createProject(path, VeloConstants.MIMETYPE_PROJECT);
		
		resourceManager.addComment(path, "This is comment text.");// create
		
//		assertTrue(resourceManager.resourceExists((createdProject).getPath())); // verify
//		resourceManager.deleteResource(createdProject.getPath()); // clean up
//		assertFalse(resourceManager.resourceExists(createdProject.getPath()));
	}

}
