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
// testing starteam

import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.core.resources.security.CatSecurityException;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.logging.CatLogger;
import junit.framework.TestCase;

import org.apache.log4j.Logger;



/**
 * This class is meant to be extended by the various JUnit tests that need access to the IResourceService.
 * It provides a setUp() method that logs into the repository using a username and password that can be
 * shared across all of these tests.
 * This eliminates the need for each individual test to provide its own setUp method that does the same thing.
 * 
 * @author Eric Marshall
 * @version $Revision: 1.0 $
 */
public abstract class CatTest extends TestCase {
  /**
   * An instance of IResourceService that can be used by subclasses to test the repository.
   */
  protected IResourceManager mgr;
  protected ISearchManager searchMgr;
  private Logger logger = CatLogger.getLogger(CatTest.class);
  /**
   * An instance of ISecurityManager that can be used by subclasses to test the repository.
   */
  protected ISecurityManager securityMgr;

  // TODO when we add authentication we will need a real username/password.
  protected final static String LOGIN_NAME = "admin";
  protected final static String LOGIN_PWD  = "admin";

  /**
   * Logs into the resources plugin and retrieves a ResourceService
   */
  public void setUp() {
    try {
      ResourcesPlugin.getSecurityManager().login(LOGIN_NAME, LOGIN_PWD);

    } catch (NullPointerException e) {
      fail("setUp failed. Make sure you are running this as a JUnit Plug-in Test");
    } catch (ServerException e) {
      // TODO Auto-generated catch block
      logger.error(e);
      fail("setUp failed. Make sure you have set up the server..??");
    } catch (CatSecurityException e) {
      // TODO Auto-generated catch block
      logger.error(e);
      fail("setUp failed. Make sure you are using good credentials");
    }

    try {
      this.mgr = ResourcesPlugin.getResourceManager();
      this.securityMgr = ResourcesPlugin.getSecurityManager();
      this.searchMgr = ResourcesPlugin.getSearchManager();
    } catch (Exception e) {
      logger.error(e);
      fail("Could not get the ResourceService. Make sure you are running this as a JUnit Plug-in Test");
    }
  }
  

  public void tearDown() {
    ResourcesPlugin.getSecurityManager().logout();
  }
  
}
