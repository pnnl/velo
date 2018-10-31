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
/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package gov.pnnl.cat.tests;

import gov.pnnl.cat.util.NodeUtils;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Tests for taxonomy behavior.
 */
public class LinkTests extends TestCase
{
  private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
  protected NodeService nodeService;
  protected ContentService contentService;
  protected TransactionService transactionService;
  protected AuthenticationComponent authenticationComponent;
  protected SearchService searchService;
  protected StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
  protected NodeUtils nodeUtils;
  protected PersonService personService;
  protected PermissionService permissionService;
  protected AuthenticationService authenticationService;
 
  
  @Override
  public void setUp()
  {
    nodeService = (NodeService)applicationContext.getBean("nodeService");
    contentService = (ContentService)applicationContext.getBean("contentService");
    authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
    transactionService = (TransactionService)applicationContext.getBean("transactionComponent");
    searchService = (SearchService)applicationContext.getBean("searchService");
    nodeUtils = (NodeUtils)applicationContext.getBean("nodeUtils");
    personService = (PersonService)applicationContext.getBean("personService");
    permissionService = (PermissionService)applicationContext.getBean("PermissionService" );
    authenticationService = (AuthenticationService)applicationContext.getBean("authenticationServiceImpl");
    
  }

  @Override
  public void tearDown()
  {
    authenticationComponent.clearCurrentSecurityContext();
  }

  public void testDoubleLinks() throws Exception { 
    // Authenticate as the system user
    AuthenticationUtil.setRunAsUserSystem();
    
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    NodeRef testFile, link1, link2, testFolder1, testFolder2;
    try
    {
      userTransaction1.begin();
     
      // Create a test folder
      NodeRef companyHome = nodeUtils.getCompanyHome();
      testFolder1 = nodeUtils.createFolder(companyHome, "test1", null, nodeService);
      
      testFolder2 = nodeUtils.createFolder(companyHome, "test2", null, nodeService);
      
      testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");
      link1 = nodeUtils.createLinkedFile(testFile, testFolder1);
      link2 = nodeUtils.createLinkedFile(link1, testFolder2);
      
      userTransaction1.commit();

    }
    catch(Exception e)
    {
      try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
      throw e;
    }        

    // now verify that the double link was swapped to a single link
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    try
    {
      userTransaction2.begin();
      
      // link2 should have been replaced by another link
      assertEquals(false, nodeService.exists(link2));
      
      String link2path = "/Company Home/test2/test.txt";
      NodeRef singleLink = nodeUtils.getNodeByName(link2path);
      assertNotNull(singleLink);
      
      // make sure the new target is the original file
      NodeRef target = (NodeRef)nodeService.getProperty(singleLink, ContentModel.PROP_LINK_DESTINATION);
      
      assertEquals(target, testFile);

      // clean up
      nodeService.deleteNode(testFolder1);
      nodeService.deleteNode(testFolder2);
      nodeService.deleteNode(testFile);
      
      userTransaction2.commit();
    }
    catch(Exception e)
    {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
        
    }   
  }
  
  public void testRecursiveDelete() throws Exception { 
    // Authenticate as the system user
    AuthenticationUtil.setRunAsUserSystem();
    
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    NodeRef testFolder1, testFolder2;
    try
    {
      userTransaction1.begin();
     
      // Create a test folder
      NodeRef companyHome = nodeUtils.getCompanyHome();
      testFolder1 = nodeUtils.createFolder(companyHome, "recursiveTest1", null, nodeService);
      
      testFolder2 = nodeUtils.createFolder(testFolder1, "recursiveTest2", null, nodeService);
      
      nodeUtils.createTextFile(testFolder2, "test.txt", "this is a test");
      
      userTransaction1.commit();

    }
    catch(Exception e)
    {
      try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
      throw e;
    }        

    // now try to delete the parent folder
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    try
    {
      userTransaction2.begin();
      
      nodeService.deleteNode(testFolder1);
      
      userTransaction2.commit();
    }
    catch(Exception e)
    {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
        
    }   
  }
}
