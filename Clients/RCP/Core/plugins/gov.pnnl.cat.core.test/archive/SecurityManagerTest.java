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

import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.security.CatSecurityException;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.core.resources.security.User;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;

import java.util.Random;

import org.apache.log4j.Logger;

/**
 */
public class SecurityManagerTest extends CatTest {
  protected static Logger logger = CatLogger.getLogger(SecurityManagerTest.class);
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private CmsPath homeFolder;
  private final String JUNIT_PASSWORD = "junit";

  /**
   * Method testGetUsers.
   * @throws ServerException
   * @throws ResourceException
   */
  public void testGetUsers() throws ServerException, ResourceException{
    //Collection<IUser> users = this.securityMgr.getUsers();
    IUser[] users = this.securityMgr.getUsers();
    assertNotNull(users);

    assertTrue(users.length > 0);

    for (IUser user : users) {
      //System.out.println(user);
      logger.debug(user);
      assertNotNull(user.getUsername());
      assertTrue(user.getUsername().trim().length() > 1);
    }
  }

  /**
   * Method testGetUser.
   * @throws ServerException
   * @throws ResourceException
   */
  public void testGetUser() throws ServerException, ResourceException {
    IUser user = this.securityMgr.getUser("admin");
    assertEquals("admin", user.getUsername());
    assertEquals("Administrator", user.getFirstName());
    assertEquals("", user.getLastName());
    //System.out.println(user.getHomeFolder());
    logger.debug(user.getHomeFolder());
    assertNotNull(user.getHomeFolder());
    assertEquals(new CmsPath("/"), user.getHomeFolder());
    assertTrue(user.isAdmin());
  }

  /**
   * Method testCreateAndDeleteUser.
   * @throws ServerException
   * @throws CatSecurityException
   * @throws ResourceException
   */
  public void testCreateAndDeleteUser() throws ServerException, CatSecurityException, ResourceException {
    String strUserName = "test"+System.currentTimeMillis();
    
    User user = new User();
    user.setUsername(strUserName);
    user.setFirstName("TestFirstName");
    user.setLastName("TestLastName");
    user.setEmail("TestFirstName.TestLastName@pnl.gov");
    user.setPassword("test");
    
    IUser[] newUsers = this.securityMgr.createUser(user);
    assertEquals(1, newUsers.length);
    assertEquals(strUserName, newUsers[0].getUsername());
    assertEquals("TestFirstName", newUsers[0].getFirstName());
    assertEquals("TestLastName", newUsers[0].getLastName());
    assertEquals("TestFirstName.TestLastName@pnl.gov", newUsers[0].getEmail());
    //System.out.println("user home: " + newUsers[0].getHomeFolder());
    logger.debug("user home: " + newUsers[0].getHomeFolder());
    assertEquals(new CmsPath("/User Documents/" + newUsers[0].getUsername()), newUsers[0].getHomeFolder());
    
    this.securityMgr.deleteUser(strUserName);
    try {
      this.securityMgr.getUser(strUserName);
      fail("User should have been deleted");
    } catch (Exception ex) {    
    }
  }

  /**
   * Method testUpdateUser.
   * @throws ServerException
   * @throws ResourceException
   */
  public void testUpdateUser() throws ServerException, ResourceException {
    // TODO: make this happen for someone else!
    IUser user = this.securityMgr.getUser(username);
    assertEquals(homeFolder, user.getHomeFolder());
    IUser clonedUser = user.clone();
    String firstName = user.getFirstName();
    String newFirstName = firstName + "-UPDATED";
    String newNewPhone = user.getPhoneNumber() + "-4567";
    clonedUser.setFirstName(newFirstName);
    clonedUser.setPhoneNumber(newNewPhone);
//    clonedUser.setPicture(new CmsPath("/User Documents/eric/Personal Library/folder_new.gif"));

    IUser[] updatedUsers = this.securityMgr.updateUser(clonedUser);
    assertNotNull(updatedUsers);
    assertEquals(1, updatedUsers.length);

    IUser updatedUser = updatedUsers[0];
    assertEquals(newFirstName, updatedUser.getFirstName());
    assertNotSame(firstName, updatedUser.getFirstName());
    assertEquals(newNewPhone, updatedUser.getPhoneNumber());
    // make sure their home folder hasn't been changed
    assertEquals(homeFolder, updatedUser.getHomeFolder());
  }

  public void setUp() {
    super.setUp();

    int id = new Random().nextInt(1000);
    username = "junitTestUser" + id;
    firstName = "Fname" + id;
    lastName = "Lname" + id;
    email = "Fname.Lname" + id + "@pnl.gov";
    phone = "123";
    homeFolder = new CmsPath("/User Documents/" + username);

    User user = new User();
    user.setUsername(username);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPhoneNumber(phone);
    user.setPassword(JUNIT_PASSWORD);

    try {
      IUser[] newUsers = this.securityMgr.createUser(user);

      assertEquals(1, newUsers.length);

      assertEquals(username,   newUsers[0].getUsername());
      assertEquals(firstName,  newUsers[0].getFirstName());
      assertEquals(lastName,   newUsers[0].getLastName());
      assertEquals(email,      newUsers[0].getEmail());
      assertEquals(phone,      newUsers[0].getPhoneNumber());
      // TODO: figure out why this fails
//      assertEquals(homeFolder, newUsers[0].getHomeFolder());

    } catch (Exception e) {
      logger.error(e);
      fail("Could not create the user");
    }
  }

  public void tearDown() {
    try {
      this.securityMgr.deleteUser(username);
    } catch (Exception e) {
      logger.error(e);
      fail("Could not delete the user");
    }

    super.tearDown();
  }
}
