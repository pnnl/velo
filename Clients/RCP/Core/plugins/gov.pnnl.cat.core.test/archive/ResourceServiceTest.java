/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.core.resources.tests;


/**
 */
public class ResourceServiceTest extends CatTest {
//  protected static Logger logger = CatLogger.getLogger(ResourceServiceTest.class);
//  private static final CmsPath JUNIT_FOLDER_PATH = new CmsPath("/JUnit Test Data");
//  private static final CmsPath JUNIT_FILE_PATH = new CmsPath("/JUnit Test Data/test.txt");
////  private static final String JUNIT_CHILD_FOLDER_NAME = "childFolder";
//  private static final CmsPath JUNIT_TAXONOMY_PATH = JUNIT_FOLDER_PATH.append("myTaxonomyFolder");
//  private File testFile;
//  private static final int TOTAL_FOLDERS_CREATED_BY_SETUP = 1;
//
//  /**
//   * A simple test to make sure we can retrieve the root folder from the
//   * manager.
//   * 
//   * @throws ResourceException
//   */
//  public void testGetCatRoot() throws ResourceException {
//    
//    CmsPath rootPath = new CmsPath("/");
//    IFolder catRoot = this.mgr.getCatRoot();
//    assertNotNull(catRoot);
//    CmsPath path = catRoot.getPath();
//    assertNotNull(path);
//    assertEquals(rootPath, path);
//
//    // the root folder should have no parent
//    assertNull(catRoot.getParent());
//
//    // retrieve the cat root another way just to make sure we are consistent.
//    IFolder catRoot2 = (IFolder) this.mgr.getResource(rootPath);
//    assertEquals(catRoot, catRoot2);
//  }
//
//  public void testGetProjects() throws ResourceException {
//    IFolder[] projects = this.mgr.getProjects();
//    assertNotNull(projects);
//    //System.out.println("Projects found: " + projects.length);
//    logger.debug("Projects found: " + projects.length);
//    for (IFolder folder : projects) {
//      //System.out.println(folder.getName());
//      logger.debug(folder.getName());
//    }
//  }
//
//  /**
//   * This test gets the available resource trees and requests the root folder
//   * for each resource tree.
//   * 
//   * @throws ResourceException
//   */
//  public void testGetRootResources() throws ResourceException {
//    IFolder resourceTrees = this.mgr.getCatRoot();
//    assertNotNull(resourceTrees);
//    //System.out.println("tree: " + resourceTrees);
//    logger.debug("tree: " + resourceTrees);
//    CmsPath path = resourceTrees.getPath();
//    //System.out.println("path: " + path);
//    logger.debug("path: " + path);
//    assertNotNull(path);
//    IResource resource = this.mgr.getResource(path);
//    assertNotNull(resource);
//
//    // try to get the resource's parent and make sure we get null
//    assertNull(resource.getParent());
//  }
//
//
//  public void testResourceExists() throws ResourceException {
//    CmsPath existingPath = new CmsPath("/");
//    CmsPath nonExistantPath = new CmsPath("/this path will never exist - it just can't");
//
//    assertTrue(this.mgr.resourceExists(existingPath));
//    assertFalse(this.mgr.resourceExists(nonExistantPath));
//  }
//
//  
//  public void testAddTaxonomy() throws ResourceException{
//    // add a new folder to the repository
//    CmsPath path = JUNIT_FOLDER_PATH.append("JUnit Test Taxonomy " + new Date());
//    IFolder addedFolder = this.mgr.addTaxonomy(path, null);
//
//    assertNotNull(addedFolder);
//    assertEquals(path, addedFolder.getPath());
//    assertTrue(this.mgr.resourceExists(path));
//    assertEquals(LOGIN_NAME, addedFolder.getPropertyAsString(VeloConstants.PROP_CREATOR));
//    assertTrue(addedFolder.isType(IResource.TAXONOMY_ROOT));
//    assertFalse(addedFolder.isType(IResource.PHYSICAL));
//
//    // try to add the file again to make sure that fails
//    try {
//      this.mgr.addTaxonomy(path, null);
//      fail("Adding a taxonomy twice should throw an exception");
//    } catch (ResourceException e) {
//      // expected behavior
//    }
//  }
//
//
//  public void testGetTaxonomies() throws ResourceException {
//    IFolder[] taxonomies = this.mgr.getTaxonomies();
//
//    assertNotNull(taxonomies);
//    assertTrue(taxonomies.length > 0);
//
//    for (IFolder taxonomy : taxonomies) {
//      assertTrue(taxonomy.isType(IResource.TAXONOMY_ROOT));
//      //4System.out.println(taxonomy.getPath());
//      logger.debug(taxonomy.getPath());
//    }
//  }
//
//
//  public void testAddFolder() throws ResourceException, ParseException {
//    sleep(500);
//    CmsPath newFolderPath = JUNIT_FOLDER_PATH.append("/a/b/c");
//    // the new folder shouldn't already exist
//    assertFalse(this.mgr.resourceExists(newFolderPath));
//    // the new folder's parent shouldn't already exist
//    assertFalse(this.mgr.resourceExists(newFolderPath.removeLastSegments(1)));
//
//    // try to add the folder without specifying recurse = true.
//    try {
//      this.mgr.addFolder(newFolderPath, false, null);
//      fail("Should have thrown an exception");
//    } catch (ResourceException e) {
//      // this is expected
//    }
//    // the new folder should still not exist
//    assertFalse(this.mgr.resourceExists(newFolderPath));
//
//    IFolder newFolder = this.mgr.addFolder(newFolderPath, true, null);
//    assertNotNull(newFolder);
//    sleep(1000);
//    assertTrue(this.mgr.resourceExists(newFolderPath));
//
//    // make sure that the properties are correct
//    Vector children = newFolder.getChildren();
//    assertNotNull(children);
//    assertEquals(0, children.size());
//
//    assertEquals(LOGIN_NAME, newFolder.getPropertyAsString(VeloConstants.PROP_CREATOR));
//    assertEquals(LOGIN_NAME, newFolder.getPropertyAsString(VeloConstants.PROP_MODIFIER));
//
//    // make sure the new folder's creation date = its last modified date which 
//    // should also equal its parent's last modified date
//    Calendar creationDate = newFolder.getPropertyAsDate(VeloConstants.PROP_CREATED);
//    Calendar lastModifiedDate = newFolder.getPropertyAsDate(VeloConstants.PROP_MODIFIED);
//    long difference = creationDate.getTimeInMillis() - lastModifiedDate.getTimeInMillis();
//    assertTrue("last modified time and creation time are too far apart.", difference < 250);
//
//    Calendar parentLastModifiedDate = this.mgr.getPropertyAsDate(JUNIT_FOLDER_PATH, VeloConstants.PROP_MODIFIED);
//    difference = creationDate.getTimeInMillis() - parentLastModifiedDate.getTimeInMillis();
//    assertTrue("creation time and parent's last modified time are too far apart.", difference < 250);
//
//    // make sure we can look it back up
//    IResource resource = this.mgr.getResource(newFolderPath);
//    assertNotNull(resource);
//    assertTrue(resource instanceof IFolder);
//
//    // try to add the folder again to confirm that we get an error
//    try {
//      this.mgr.addFolder(newFolderPath);
//      fail("Should have thrown an exception");
//    } catch (ResourceException expected) { }
//  }
//
//
//  public void testDeleteFile() throws ResourceException, IOException {
//    CmsPath path = JUNIT_FOLDER_PATH.append("file to be deleted");
//    InputStream content = new ByteArrayInputStream(
//        "the quick brown fox jumped over the lazy dog".getBytes());
//
//    IFile file = this.mgr.addFile(path, content, null);
//
//    //make sure it was really added
//    assertNotNull(file);
//
//    // delete the file
//    this.mgr.deleteResource(file.getPath(), null);
//
//    // now try to retrieve it again
//    // this should fail.
//    try {
//      file = (IFile) this.mgr.getResource(path);
//      fail("Retrieving the deleted resource should have thrown an exception");
//    } catch (ResourceException e) {
//      // do nothing, this is expected
//    }
//  }
//
//  public void testDeleteFolder() throws ResourceException {
//    // add a folder
//    CmsPath path = JUNIT_FOLDER_PATH.append("Test Delete Folder");
//    CmsPath child1 = path.append("child1");
//    CmsPath child2 = path.append("child2");
//    InputStream content1 = new ByteArrayInputStream("content for child 1".getBytes());
//    InputStream content2 = new ByteArrayInputStream("content for child 2".getBytes());
//    IFolder addedFolder = this.mgr.addFolder(path);
//
//    assertNotNull(addedFolder);
//    assertEquals(path, addedFolder.getPath());
//
//    // make sure it's there and doesn't have any children
//    IResource resource = this.mgr.getResource(path);
//    assertNotNull(resource);
//    assertTrue(resource instanceof IFolder);
//    IFolder folder = (IFolder) resource;
//    assertNotNull(folder.getChildren());
//    assertEquals(0, folder.getChildren().size());
//
//    // add a couple children
//    assertNotNull(this.mgr.addFile(child1, content1, null));
//    assertNotNull(this.mgr.addFile(child2, content2, null));
//
//    this.mgr.deleteResource(path, null);
//
//    assertFalse(this.mgr.resourceExists(path));
//  }
//
//
//  public void testDeleteTargetFile() throws ResourceException {
//    // the path to an ordinary folder we will add
//    CmsPath path = JUNIT_FOLDER_PATH.append("JUnit Test Delete Target File");
//
//    // the path to a link to our ordinary folder
//    CmsPath linkPath = new CmsPath(path.toString() + " LINK");
//
//    InputStream content = new ByteArrayInputStream(
//        "the quick brown fox jumped over the lazy dog".getBytes());
//
//    IFile addedFile = this.mgr.addFile(path, content, null);
//
//    assertNotNull(addedFile);
//    assertEquals(path, addedFile.getPath());
//
//    // create a link to this file
//    this.mgr.addLink(linkPath, addedFile, null);
//
//    //now delete the target file
//    this.mgr.deleteResource(path, null);
//
//    // make sure the link no longer exists
//    assertFalse(this.mgr.resourceExists(linkPath));
//  }
//
//
//  public void testDeleteLink() throws ResourceException {
//    // the path to an ordinary folder we will add
//    CmsPath path = JUNIT_FOLDER_PATH.append("JUnit Test Delete Link");
//
//    // the path to a link to our ordinary folder
//    CmsPath linkPath = new CmsPath(path.toString() + " LINK");
//
//    InputStream content = new ByteArrayInputStream(
//        "the quick brown fox jumped over the lazy dog".getBytes());
//
//    IFile addedFile = this.mgr.addFile(path, content, null);
//    assertNotNull(addedFile);
//
//    // create a link to this folder
//    assertNotNull(this.mgr.addLink(linkPath, addedFile, null));
//
//    // make sure both folders exist like they should
//    assertTrue(this.mgr.resourceExists(path));
//    assertTrue(this.mgr.resourceExists(linkPath));
//
//    // now delete the linked file
//    this.mgr.deleteResource(linkPath, null);
//
//    // make sure it is gone
//    assertFalse(this.mgr.resourceExists(linkPath));
//    // and that the original is still there
//    assertTrue(this.mgr.resourceExists(path));
//  }
//
//
//  public void testAddFile() throws ResourceException, FileNotFoundException {
//    // locate a real file on our local drive
//    File file = new File("notice.html");
//    assertTrue(file.exists());
//
//    // add a new file to the repository
//    CmsPath path = JUNIT_FOLDER_PATH.append("notice.html");
//    IFile addedFile;
//
//    addedFile = this.mgr.addFile(path, file, null, true);
//
//    assertNotNull(addedFile);
//    assertEquals(path, addedFile.getPath());
//    assertTrue(this.mgr.resourceExists(path));
//    assertEquals(LOGIN_NAME, addedFile.getPropertyAsString(VeloConstants.PROP_CREATOR));
//    assertEquals(6506, addedFile.getSize());
//    assertEquals("text/html", addedFile.getPropertyAsString(VeloConstants.PROP_CONTENT_TYPE));
//
//    // try to add the file again to make sure that fails
//    try {
//      this.mgr.addFile(path, file, null, true);
//      fail("Adding a file twice should throw an exception");
//    } catch (ResourceException e) {
//      // expected behavior
//    }
//
//    
//    // TODO: update the content and make sure it changed
//  }
//
//
//  public void testAddEmptyFile() throws ResourceException {
//    CmsPath path = JUNIT_FOLDER_PATH.append("file to be deleted");
//
//    InputStream content = new ByteArrayInputStream(
//        "".getBytes());
//
//    IFile addedFile = this.mgr.addFile(path, content, null);
//    assertNotNull(addedFile);
//  }
//
//
//  public void testAddLockedFile() throws ResourceException, IOException {
//    File transferFile = File.createTempFile("CAT JUnit Test", "txt");
//    transferFile.deleteOnExit();
//
//    FileChannel channel = new RandomAccessFile(transferFile, "rw").getChannel();
//
//    // Try getting a lock
//    FileLock lock = channel.tryLock();
//    assertNotNull(lock);
//
//    InputStream content = new FileInputStream(transferFile);
//
//    // add a new file to the repository
//    CmsPath path = JUNIT_FOLDER_PATH.append("JUnit Test Locked File");
//
//    try {
//      this.mgr.addFile(path, content, null);
//      fail("Should have failed");
//    } catch (ResourceException e) {
//      CmsPath alternatePath = JUNIT_FOLDER_PATH.append("JUnit Test unlocked File");
//      content = new ByteArrayInputStream("test contenet".getBytes());
//
//      // now try to add another file that should work to make sure we are recovering from errors
//      IFile file = this.mgr.addFile(alternatePath, content, null);
//
//      assertNotNull(file);
//      assertEquals(alternatePath, file.getPath());
//    }
//
//    // Release the lock we created since we now know that the file can be accesses successfully
//    lock.release();
//  }
//
//
//  /**
//   * This test attempts to insert a file into the repository with special characters.
//   * This test currently fails.
//   * @throws ResourceException
//   */
//  public void testAddFileWithSpecialChars() throws ResourceException {
//    // TODO: handle special characters correctly so that this test passes.
//
//    fail("this test needs to be updated");
////    // create an empty InputStream
////    InputStream content = new ByteArrayInputStream("this is sample content".getBytes());
////
////    // add a new file to the repository
////    String filename = "Eric's Test File";
//////    String filename = "JUnit Test File With Special Chars `~!@#$%^&*()-_=+\\|[]{}''\"\";;::/?.>,]<>,.†";
//////    System.out.println("(\\.)|"
//////                + "(\\.\\.)|"
//////                + "(([^ /:\\[\\]*'\"|](?:[^/:\\[\\]*'\"|]*[^ /:\\[\\]*'\"|])?):)?"
//////                + "([^ /:\\[\\]*'\"|](?:[^/:\\[\\]*'\"|]*[^ /:\\[\\]*'\"|])?)"
//////                + "(\\[([1-9]\\d*)\\])?");
////    /*filename = */Text.encodeIllegalXMLCharacters(filename);
////    //System.out.println(filename);
////    logger.debug(filename);
//////    Matcher matcher = PATH_ELEMENT_PATTERN.matcher(filename);
//////    assertTrue("Illegal Characters!", matcher.matches());
////
////    CmsPath path = JUNIT_FOLDER_PATH.append(filename);
////    IFile addedFile = this.mgr.addFile(path, content, null);
////
////    assertNotNull(addedFile);
////    assertEquals(path, addedFile.getPath());
//  }
//
//
//  /**
//   * This test retrieves the children of a known hard-coded directory and
//   * compares the children returned by IResourceService.getChildren(CmsPath)
//   * with what it expects.
//   * 
//   * @throws ResourceException
//   */
//  public void testGetChildren() throws ResourceException {
//    CmsPath path = JUNIT_FOLDER_PATH.append("parent folder with lots of children");
//    String[] expectedChildNames = { "Basic", "Blast", "contents.html",
//        "Deflagration", "DetailTOC.html", "Detonation", "Detonators",
//        "Disclaimer.html", "Field", "GIFs", "GlblFile", "home.jsp",
//        "indexfrm.html", "Mechanical", "NonShock", "Nuclear", "README.doc",
//        "Reference_Guide_Start.html", "Safety", "Shock", "Simulants",
//        "StyleSheet.css", "SubjectTOC.html", "SummarySheets", "Synthesis",
//        "Thermal", "Thermal_Expl" };
//
//    this.mgr.addFolder(path);
//
//    // add a bunch of child folders
//    for (String childName : expectedChildNames) {
//      this.mgr.addFolder(path.append(childName));
//    }
//
//
//    boolean[] childrenFound = new boolean[expectedChildNames.length];
//    boolean found;
//    IFolder resource = (IFolder) this.mgr.getResource(path);
//
//    assertNotNull(resource);
//
//    Vector<IResource> children = this.mgr.getChildren(path, null);
//    assertNotNull(children);
//    assertTrue(children.size() > 0);
//    assertEquals(27, children.size());
//
//    assertEquals(children, ((IFolder) resource).getChildren());
//
//    // look at the children we got back and compare them to the child names that
//    // we were expecting. Mark each child name that we were expecting that
//    // either we found it or we didn't.
//    for (IResource child : children) {
//      found = false;
//
//      for (int i = 0; i < expectedChildNames.length && !found; i++) {
//        if (child.getName().equals(expectedChildNames[i])) {
//          childrenFound[i] = true;
//        }
//      }
//    }
//
//    // iterate over the booleans indicating whether we found each child to make
//    // sure we found everything we were expecting.
//    for (int i = 0; i < childrenFound.length; i++) {
//      assertTrue("The child '" + expectedChildNames[i] + "' was not found.", childrenFound[i]);
//    }
//  }
//
//
//  public void testGetFolderContents() throws ResourceException {
//    int totalFoldersToAdd = 10;
//
//    for (int i = 0; i < totalFoldersToAdd; i++) {
//      this.mgr.addFolder(JUNIT_FOLDER_PATH.append("folder " + i));
//    }
//
//    IFolder folder = (IFolder) this.mgr.getResource(JUNIT_FOLDER_PATH);
//
//    long[] contents = folder.getContents(null);
//    //System.out.println("FOLDER SIZE: " + contents[0] + ", FILES: " + contents[1] + ", FOLDERS: " + contents[2]);
//    logger.debug("FOLDER SIZE: " + contents[0] + ", FILES: " + contents[1] + ", FOLDERS: " + contents[2]);
//
//    long size         = contents[0];
//    long totalFiles   = contents[1];
//    long totalFolders = contents[2];
//
//    assertEquals(this.testFile.length(), size);
//    assertEquals(1, totalFiles);
//    assertEquals(totalFoldersToAdd + TOTAL_FOLDERS_CREATED_BY_SETUP, totalFolders);
//  }
//
//
//  /**
//   * This test requests a Vector of properties from the ResourceService and
//   * compares the values returned with what is expected. It also makes sure that
//   * the IResource returns the same values for this particular list of
//   * properties as the ResourceService does.
//   * 
//   * @throws ResourceException
//   */
//  public void testGetProperties() throws ResourceException {
//    QualifiedName[] qNames = {
//        VeloConstants.PROP_CREATED,
//        VeloConstants.PROP_MODIFIER,
//        VeloConstants.PROP_CREATOR
//    };
//    Vector<QualifiedName> qNamesVector = new Vector<QualifiedName>(Arrays.asList(qNames));
//    Vector<String> results = this.mgr.getPropertiesAsString(JUNIT_FOLDER_PATH, qNamesVector);
//
//    assertNotNull(results);
//    assertEquals(qNames.length, results.size());
//    assertEquals(LOGIN_NAME, results.get(1));
//    assertEquals(LOGIN_NAME, results.get(2));
//    assertEquals(results, this.mgr.getResource(JUNIT_FOLDER_PATH).getPropertiesAsString(qNamesVector));
//  }
//
//
//  /**
//   * This test requests a fake property to make sure that it throws an
//   * exception.
//   * 
//   * @throws ResourceException
//   */
//  public void testGetFakeProperty() throws ResourceException {
//    QualifiedName qname = new QualifiedName("jcr", "thisPropertyDoesNotExist");
//    assertNull(this.mgr.getProperty(JUNIT_FOLDER_PATH, qname));
//    assertNull(this.mgr.getResource(JUNIT_FOLDER_PATH).getPropertyAsString(qname));
//  }
//
//
//  public void testSetProperty() throws ResourceException {
//    String value = "test junit title";
//    QualifiedName property = VeloConstants.PROP_TITLE;
//
//    assertTrue(this.mgr.resourceExists(JUNIT_FOLDER_PATH));
//    IFolder folder = (IFolder) this.mgr.getResource(JUNIT_FOLDER_PATH);
//
//    // TODO: uncomment the following line once we get notification working.
//    // the problem is, we cache the value of the property as null. then later
//    // when we want to look it back up, the cache hasn't been updated, so we
//    // still get null back, instead of the new value.
////    assertNull(folder.getPropertyAsString(property));
//    folder.setProperty(property, value);
//    assertEquals(value, folder.getPropertyAsString(property));
//  }
//
//
//  public static void sleep(long ms) {
//    try {
//      Thread.sleep(ms);
//    } catch (InterruptedException ignored) {}
//  }
//
//
//  public void testSetFileContents() throws ResourceException, IOException {
//    String newData = "this is some different text that we will write to the junit test file";
//    InputStream inputStream = new ByteArrayInputStream(newData.getBytes());
//
//    // update the file with our new data
//    this.mgr.setFileContents(JUNIT_FILE_PATH, inputStream);
//
//    assertEquals(new ByteArrayInputStream(newData.getBytes()), this.mgr.getContentProperty(JUNIT_FILE_PATH, VeloConstants.PROP_CONTENT));
//  }
//
//
//  public void testGetFileContents() throws ResourceException, IOException {
//    assertTrue(this.testFile.exists());
//    assertTrue(this.testFile.length() > 0);
//
//    InputStream input1 = this.mgr.getContentProperty(JUNIT_FILE_PATH, VeloConstants.PROP_CONTENT);
//    InputStream input2 = new FileInputStream(this.testFile);
//
//    assertNotNull(input1);
//    assertNotNull(input2);
//
//    assertEquals(input1, input2);
//  }
//
//
//  // a custom assertEquals for comparing each byte in an InputStream
//  private void assertEquals(InputStream stream1, InputStream stream2) throws IOException {
//    int data;
//    while ((data = stream1.read()) != -1) {
//      assertEquals(data, stream2.read());
//    }
//
//    stream1.close();
//    stream2.close();
//  }
//
//
//  public void testGetURL() throws ResourceException {
//    final String CORRECT_URL = "http://bkcl1.pnl.gov:8080/alfresco/webdav" +  JUNIT_FILE_PATH.toString();
//
//    CmsPath path = JUNIT_FILE_PATH;
//    URL url = this.mgr.getWebdavUrl(path);
//    assertEquals(CORRECT_URL, url.toString());
//
//    IResource resource = this.mgr.getResource(path);
//    assertTrue(resource instanceof IFile);
//    assertNotNull(((IFile) resource).getWebdavUrl());
//    assertEquals(((IFile) resource).getWebdavUrl(), url);
//    
//    //System.out.println(((IFile) resource).getWebdavUrl());
//    logger.debug(((IFile) resource).getWebdavUrl());
//  }
//
//
//  public void testGetURLContentType() throws ResourceException {
//    CmsPath path = JUNIT_FILE_PATH;
//    IFile file = (IFile)mgr.getResource(path);
//    
//    URL url = file.getTextHttpUrl();
//    
//    //System.out.println(url);
//    logger.debug(url);
//  }
//
//
//  /**
//   * This test inserts a new file into the repository, moves that resource.
//   * 
//   * @throws ResourceException
//   */
//  public void testMove() throws ResourceException {
//    // TODO: update this test when we get the server bug fixes
//    InputStream content = new ByteArrayInputStream(new String("this is some sample content to be stored in the new file").getBytes());
//    CmsPath srcPath  = JUNIT_FOLDER_PATH.append("JUnit Test File - testMove");
//    CmsPath destPath = JUNIT_FOLDER_PATH.append("3JUnit Test File - testMove - NEW FILENAME");
//
//    this.mgr.addFile(srcPath, content, null);
//
//    assertNotNull(this.mgr.getResource(srcPath));
//
//    // make sure we don't have a file at the destination
//    assertFalse(this.mgr.resourceExists(destPath));
//
//    // now move our file into the destination path
//    this.mgr.move(srcPath, destPath, null);
//
//    // make sure we can retrieve our file
//    assertNotNull(this.mgr.getResource(destPath));
//
//    this.mgr.getResource(srcPath);
//  }
//
//
//  public void testCopy() throws ResourceException {
//    // TODO: verify this test passes when we get the server bug fixes
//    CmsPath srcPath  = JUNIT_FILE_PATH;
//    CmsPath destPath = JUNIT_FILE_PATH.removeLastSegments(1).append("copy of test.txt");
//
//    assertNotNull(this.mgr.getResource(srcPath));
//
//    // make sure we don't have a file at the destination
//    assertFalse(this.mgr.resourceExists(destPath));
//
//    // now move our file into the destination path
//    this.mgr.copy(srcPath, destPath, null);
//
//    // make sure we can retrieve our file from both locations
//    assertNotNull(this.mgr.getResource(srcPath));
//    assertNotNull(this.mgr.getResource(destPath));
//  }
//
//
//  public void testAddLinkedFile() throws ResourceException {
//    CmsPath linkSrc = JUNIT_FOLDER_PATH.append("linked-file.txt");
//    CmsPath target  = JUNIT_FILE_PATH;
//
//    IFile targetResource = (IFile) this.mgr.getResource(target);
//
//    ILinkedResource linkedResource = this.mgr.addLink(linkSrc, targetResource, null);
//
//    assertNotNull(linkedResource);
//    assertTrue(linkedResource instanceof IFile);
//
//    IFile linkedFile = (IFile) linkedResource;
//    IFile srcFile    = (IFile) this.mgr.getResource(target);
//
//    assertNotNull(linkedResource.getTarget());
//
//    // make sure they have different names,
//    assertNotSame(linkedFile.getName(), srcFile.getName());
//    // different paths
//    assertNotSame(linkedFile.getPath(), srcFile.getPath());
//
//    // but that they have the same URL,
//    assertEquals(linkedFile.getWebdavUrl(), srcFile.getWebdavUrl());
//    // and size
//    assertEquals(linkedFile.getSize(), srcFile.getSize());
//  }
//
//
//  public void testUpdateLinkLocation() throws ResourceException {
////    CmsPath linkSrc = JUNIT_FOLDER_PATH.append("linked-file.txt");
////    CmsPath target  = JUNIT_FILE_PATH;
////
////    IFile targetResource = (IFile) this.mgr.getResource(target);
////
////    ILinkedResource linkedResource = this.mgr.addLink(linkSrc, targetResource);
//    
//       
//    CmsPath link = JUNIT_FOLDER_PATH.append("linked-file.txt");
//    CmsPath target = JUNIT_FILE_PATH;
//    CmsPath newTarget = JUNIT_FOLDER_PATH.append("JUnit Test Delete Target File");
//    InputStream content = new ByteArrayInputStream("the quick brown fox jumped over the lazy dog".getBytes());
//
//    IFile addedFile = this.mgr.addFile(newTarget, content, null);
//
//    assertNotNull(addedFile);
//    assertEquals(newTarget, addedFile.getPath());
//    
//    ILinkedResource linkRes;
//    IFile targetRes;
//
//    assertFalse("Link already exists", this.mgr.resourceExists(link));
//    assertTrue("Target not found", this.mgr.resourceExists(target));
//
//    targetRes = (IFile)this.mgr.getResource(target);
//    assertNotNull(targetRes);
//
////      linkRes = this.mgr.addLink(linkSrc, targetResource);
//		  linkRes = this.mgr.addLink(link, targetRes, null);
//		  assertNotNull(linkRes);
//		  assertTrue(this.mgr.resourceExists(link));
//		
////		  assertEquals(linkRes.getTarget(), targetRes);
//		
//		  this.mgr.updateLinkTarget(link, addedFile, null);
//
//		  // go get our link again, since the object we currently have a reference to is now out-of-date.
//		  // TODO update notification to do this automatically
//		  linkRes = (ILinkedResource) this.mgr.getResource(link);
////		  assertEquals(linkRes.getTarget().getPath(), newTarget);
////		  System.out.println("new target: " + linkRes.getTarget().getPath());
//    
//  }
//
//
//  public void testAddLinkToLink() throws ResourceException {
//    CmsPath link1Path = JUNIT_FOLDER_PATH.append("Link 1");
//    CmsPath link2Path = JUNIT_FOLDER_PATH.append("Link 2");
//
//    assertFalse("Resource exists!", this.mgr.resourceExists(link1Path));
//    assertFalse("Resource exists!", this.mgr.resourceExists(link2Path));
//
//    IFile target = (IFile) this.mgr.getResource(JUNIT_FILE_PATH);
//
//    ILinkedResource link1 = this.mgr.addLink(link1Path, target, null);
//
//    // try to add a link to the link
//    try {
//      this.mgr.addLink(link2Path, (IFile) link1, null);
//      fail("Linking to a link should fail!");
//    } catch (ResourceException e) {}
//  }
//
//
//  public void testGetResourcesByAspect() throws AccessDeniedException, ResourceException, ParseException {
//    List<IResource> resources = mgr.getResourcesByAspect(VeloConstants.PROP_parseQNameString(VeloConstants.ASPECT_TAXONOMY_ROOT), null);
//    assertTrue(resources.size() > 0);
//
//    for (IResource res : resources) {
//      System.out.println(res);
//    }
//  }
//
//
//  public void createTestData() throws ResourceException, IOException {
//
//    // create a folder
//
//    // delete the folder if it already exists
//    if (this.mgr.resourceExists(JUNIT_FOLDER_PATH)) {
//      this.mgr.deleteResource(JUNIT_FOLDER_PATH, null);
//    }
//
//    // now add the new folder
//    this.mgr.addFolder(JUNIT_FOLDER_PATH);
//
//    //create a file
//
//    //delete the file if it already exists
//    if (this.mgr.resourceExists(JUNIT_FILE_PATH)) {
//      this.mgr.deleteResource(JUNIT_FILE_PATH, null);
//    }
//
//    // now add the new file
//    this.testFile = createTempFile(JUNIT_FILE_PATH.last().getName(), true);
//    this.mgr.addFile(JUNIT_FILE_PATH, this.testFile, null, true);
//
//    // create a taxonomy
//
//    // delete the taxonomy if it already exists
//    if (this.mgr.resourceExists(JUNIT_TAXONOMY_PATH)) {
//      this.mgr.deleteResource(JUNIT_TAXONOMY_PATH, null);
//    }
//    this.mgr.addTaxonomy(JUNIT_TAXONOMY_PATH, null);
//  }
//  
//  
//
//  public void deleteTestData() throws ResourceException {
//    this.mgr.deleteResource(JUNIT_TAXONOMY_PATH, null);//hack for children tax's not getting deleted
//    this.mgr.deleteResource(JUNIT_FOLDER_PATH, null);
//  }
//
//
//  public void setUp() {
//    super.setUp();
//
//    // clear the cache before each test
//    this.mgr.clearCache();
//
//    try {
//      createTestData();
//    } catch (ResourceException e) {
//      logger.error(e);
//      fail();
//    } catch (IOException e) {
//      logger.error(e);
//      fail();
//    }
//  }
//
//  public void tearDown() {
//    try {
//      deleteTestData();
//    } catch (ResourceException e) {
//      logger.error(e);
//      fail();
//    }
//
//    super.tearDown();
//  }
//  
//  
//  //utility methods:
//  private File createTempFile(String filename, boolean writeContent) throws IOException{
//    String prefix = filename.substring(0, filename.lastIndexOf("."));
//    String suffix = filename.substring(filename.lastIndexOf(".") + 1);
//    File file = File.createTempFile(prefix, suffix);
//    file.deleteOnExit();
//    if(writeContent){
//      FileOutputStream out = new FileOutputStream(file);
//      for(int i = 0; i < 1024 * 1025; i++){
//        out.write("z".getBytes());
//      }
//      out.close();
//    }
//    return file;
//  }
}
