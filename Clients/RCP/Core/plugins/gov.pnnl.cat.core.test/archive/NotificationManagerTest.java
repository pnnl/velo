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
public class NotificationManagerTest extends CatTest {
//  // Note: many of these tests will fail if there is outside activity on the repository
//  private boolean[] completed;
//  protected static Logger logger = CatLogger.getLogger(NotificationManagerTest.class);
//  public void testAddFileNotification() throws ResourceException {
//    this.completed = new boolean[2];
//    final CmsPath filePath = new CmsPath("/physical/reference_library/JUnit Test Notification File");
//    InputStream content = new ByteArrayInputStream("this is some sample content".getBytes());
//
//    IResourceEventListener listener1 = new IResourceEventListener() {
//      public void onEvent(IBatchNotification events) {
//        Iterator iter = events.getNonRedundantEvents();
//        assertNotNull(iter);
//        assertTrue(iter.hasNext());
//
//        IResourceEvent event = (IResourceEvent) iter.next();
//
//        //System.out.println(event.getPath());
//        logger.debug(event.getPath());
//        assertEquals(filePath.removeLastSegments(1), event.getPath());
//        assertTrue(event.hasChange(IResourceEvent.CONTENT_CHANGED));
//        assertFalse("More events received than I expected", iter.hasNext());
//        setComplete(0);
//      }
//    };
//
//    IResourceEventListener listener2 = new IResourceEventListener() {
//      public void onEvent(IBatchNotification events) {
//        Iterator iter = events.getNonRedundantEvents();
//        assertNotNull(iter);
//        assertTrue(iter.hasNext());
//        
//        IResourceEvent event = (IResourceEvent) iter.next();
//        
//        //System.out.println(event.getPath());
//        logger.debug(event.getPath());
//        assertEquals(filePath.removeLastSegments(1), event.getPath());
//        assertTrue(event.hasChange(IResourceEvent.CONTENT_CHANGED));
//        assertFalse("More events received than I expected", iter.hasNext());
//        setComplete(1);
//      }
//    };
//
//    try {
//      this.mgr.addResourceEventListener(listener1);
//
//      this.mgr.addFile(filePath, content, null);
//
//      // use a different listener
//      this.mgr.removeResourceEventListener(listener1);
//      this.mgr.addResourceEventListener(listener2);
//    } finally {
//      this.mgr.deleteResource(filePath, null);
//    }
//
//    assertTrue(isComplete(0));
//    assertTrue(isComplete(1));
//    this.mgr.removeResourceEventListener(listener2);
//  }
//
//  private void setComplete(int index) {
//    this.completed[index] = true;
//  }
//
//  private boolean isComplete(int index) {
//    return this.completed[index];
//  }
//
//
//  public void testAddFolderNotification() throws ResourceException {
//    this.completed = new boolean[1];
//    final CmsPath folderPath = new CmsPath("/physical/reference_library/JUnit Test Notification Folder (testAddFolderNotification)");
//
//    IResourceEventListener listener = new IResourceEventListener() {
//      public void onEvent(IBatchNotification events) {
//        assertNotNull(events);
//        IResourceEvent folderEvent = events.findEvent(folderPath);
//        assertNotNull(folderEvent);
//        assertEquals(folderEvent.getPath(), folderPath);
//        assertTrue(folderEvent.hasChange(IResourceEvent.ADDED));
//
//        Iterator iter = events.getNonRedundantEvents();
//        assertNotNull(iter);
//        assertTrue(iter.hasNext());
//
//        IResourceEvent event = (IResourceEvent) iter.next();
//
//        //System.out.println(event.getPath());
//        logger.debug(event.getPath());
//        assertEquals(folderPath.removeLastSegments(1), event.getPath());
//        assertTrue(event.hasChange(IResourceEvent.CONTENT_CHANGED));
//        assertFalse("More events received than I expected", iter.hasNext());
//
////        iter = events.getNonRedundantEvents(IResourceEvent.REMOVED);
////        assertFalse(iter.hasNext());
////        try {
////          iter.next();
////          fail("Should have thrown a NoSuchElementException");
////        } catch (NoSuchElementException expected) {}
//        setComplete(0);
//      }
//    };
//
//    try {          
//      this.mgr.addResourceEventListener(listener);
//
//      this.mgr.addFolder(folderPath);
//
//      this.mgr.removeResourceEventListener(listener);
//    } finally {
//      this.mgr.deleteResource(folderPath, null);
//    }
//
//    assertTrue(isComplete(0));
//  }
//
//
//  /**
//   * The purpose of this test is to add a file before we subscribe to notifications.
//   * Then, after we have added our listener, update the content of the file and make
//   * sure the notification we get looks correct.
//   * @throws ResourceException 
//   *
//   */
//  public void testUpdateContentNotification() throws ResourceException {
//    this.completed = new boolean[1];
//    final CmsPath filePath = new CmsPath("/physical/reference_library/JUnit Test Notification File testUpdateContentNotification");
//    InputStream content = new ByteArrayInputStream("this is some sample content".getBytes());
//    String newContent = "this is some ***UPDATED*** sample content";
//
//    IResourceEventListener listener = new IResourceEventListener() {
//      public void onEvent(IBatchNotification events) {
//        Iterator iter = events.getNonRedundantEvents();
//        assertNotNull(iter);
//        assertTrue(iter.hasNext());
//
//        IResourceEvent event = (IResourceEvent) iter.next();
//
//        //System.out.println(event.getPath());
//        logger.debug(event.getPath());
//        assertEquals(filePath, event.getPath());
//        //System.out.println(event.getChangeFlags());
//        logger.debug(event.getChangeFlags());
//        assertTrue(event.hasChange(IResourceEvent.CONTENT_CHANGED));
//        assertFalse("More events received than I expected", iter.hasNext());
//        setComplete(0);
//      }
//    };
//
//    try {
//      this.mgr.addFile(filePath, content, null);
//
//      this.mgr.addResourceEventListener(listener);
//      IFile file = (IFile) this.mgr.getResource(filePath);
//      file.setContent(newContent);
//
//      this.mgr.removeResourceEventListener(listener);
//    } finally {
//      this.mgr.deleteResource(filePath, null);
//    }
//
//    assertTrue(isComplete(0));
//  }
//
//
//  /**
//   * The idea here is to create the following tree:
//   * 
//   * - a
//   *   - b
//   *     - b1
//   *     - (b2)
//   *   - c ( -> b)
//   *
//   * So we have a root folder, a, where we will store all of the resources used in this test.
//   * In there, we create a folder, b, and add a file to it, b1.  Then we create a link to b, c.
//   * 
//   * After we've done all this, we add a notification listener.  Then we add a second file to b, b2.
//   * 
//   * Our notification listener should be notified of this in two places: b and c.
//   * This tests makes sure that happens.
//   * 
//   * The last thing we do is delete a, our root folder.
//   * 
//   * *** UPDATE ***
//   * This test now fails, until we solve the problem of notifying links. (2006-07-05)
//   * 
//   * @throws ResourceException 
//   */
//  public void testUpdateLinkedFolder() throws ResourceException {
//    this.completed = new boolean[1];
//    CmsPath aPath = new CmsPath("/physical/reference_library/a (testUpdateLinkedFolder)");
//    final CmsPath bPath  = aPath.append("b");
//    CmsPath b1Path = bPath.append("b1");
//    CmsPath b2Path = bPath.append("b2");
//    final CmsPath cPath  = aPath.append("c");
//
//    InputStream b1Content = new ByteArrayInputStream("some sample content for file b1".getBytes());
//    InputStream b2Content = new ByteArrayInputStream("some sample content for file b2".getBytes());
//
//    IFolder bFolder;
//
//    IResourceEventListener listener = new IResourceEventListener() {
//      public void onEvent(IBatchNotification events) {
//        Iterator iter = events.getNonRedundantEvents();
//
//        assertNotNull(iter);
//        assertTrue(iter.hasNext());
//
//        IResourceEvent event = (IResourceEvent) iter.next();
//        assertNotNull(event);
//        assertEquals(event.getPath(), bPath);
//        assertTrue(event.hasChange(IResourceEvent.CONTENT_CHANGED));
//
//        assertTrue(iter.hasNext());
//
//        event = (IResourceEvent) iter.next();
//        assertNotNull(event);
//        assertEquals(event.getPath(), cPath);
//        assertTrue(event.hasChange(IResourceEvent.CONTENT_CHANGED));
//
//        assertFalse(iter.hasNext());
//
//        setComplete(0);
//      }
//    };
//
//    try {
//      // add a, b, b1
//      assertNotNull(this.mgr.addFolder(aPath));
//      bFolder = this.mgr.addFolder(bPath);
//      assertNotNull(bFolder);
//      assertNotNull(this.mgr.addFile(b1Path, b1Content, null));
//
//      // create a link from c to b
//      // TODO: update this test - we can't link to a folder anymore
////      assertNotNull(this.mgr.addLink(cPath, bFolder));
//
//      // register our notification listener
//      this.mgr.addResourceEventListener(listener);
//      this.mgr.addFile(b2Path, b2Content, null);
//
//      this.mgr.removeResourceEventListener(listener);
//      // make sure our listener received what he was expecting
//      assertTrue("Didn't get the notification!", isComplete(0));
//    } finally {
//      this.mgr.deleteResource(aPath, null);
//    }
//  }
}
