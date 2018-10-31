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
/**
 * Notice: This computer software was prepared by Battelle Memorial Institute,
 * hereinafter the Contractor for the Department of Homeland Security under the
 * terms and conditions of the U.S. Department of Energy's Operating Contract
 * DE-AC06-76RLO with Battelle Memorial Institute, Pacific Northwest Division.
 * All rights in the computer software are reserved by DOE on behalf of the
 * United States Government and the Contractor as provided in the Contract. You
 * are authorized to use this computer software for Governmental purposes but it
 * is not to be released or distributed to the public. NEITHER THE GOVERNMENT
 * NOR THE CONTRACTOR MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
 * LIABILITY FOR THE USE OF THIS SOFTWARE. This notice including this sentence
 * must appear on any copies of this computer software.
 */
package gov.pnnl.cat.tests;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.Serializable;
import java.util.Collection;

import javax.transaction.UserTransaction;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Tests for taxonomy behavior.
 */
public class TaxonomyBehaviorTests extends TestCase
{
  private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
  protected NodeService nodeService;
  protected ContentService contentService;
  protected TransactionService transactionService;
  protected AuthenticationComponent authenticationComponent;
  protected SearchService searchService;
  protected CategoryService categoryService;
  protected NodeUtils nodeUtils;
  protected CopyService copyService;
  protected StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
  
  
  @Override
  public void setUp()
  {
      nodeService = (NodeService)applicationContext.getBean("nodeService");
      contentService = (ContentService)applicationContext.getBean("contentService");
      authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
      transactionService = (TransactionService)applicationContext.getBean("transactionComponent");
      searchService = (SearchService)applicationContext.getBean("searchService");
      categoryService = (CategoryService)applicationContext.getBean("categoryService");
      nodeUtils = (NodeUtils)applicationContext.getBean("nodeUtils");
      copyService =(CopyService)applicationContext.getBean("copyService");
      // Authenticate as the system user
      AuthenticationUtil.setRunAsUserSystem();

      // TODO: create some test taxonomies to start with
  }

  @Override
  public void tearDown()
  {
      authenticationComponent.clearCurrentSecurityContext();
  }
  
  /**
   * Test the behavior of cm:content objects as they are added to
   * taxonomies.
   * This test will copy a file into a taxonomy root
   * @throws Exception
   */
  public void testCopyContentBehaviorIntoTaxonomyRoot() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    ResultSet resultSet;
    NodeRef testFile = null;
    NodeRef taxonomyRootFolder = null;
    
    try
    {
      try
      {
      userTransaction1.begin();
      NodeRef companyHome = nodeUtils.getCompanyHome();
      
      //create a folder
      NodeRef parentFolder = nodeUtils.getCompanyHome();
      String taxonomyRootFolderName = "TaxonomyRoot";
      taxonomyRootFolder = nodeUtils.createFolder(parentFolder, taxonomyRootFolderName);

      //Apply the taxonomy_root aspect
      nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
      
      //create test file to be copied
      testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");
      
      //copy test file to taxonomy folder
      copyService.copy(testFile, taxonomyRootFolder, 
          ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, taxonomyRootFolderName));
      
      userTransaction1.commit();
      }
      catch(Exception e)
      {
          try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      } 
      
      try
      {
        userTransaction2.begin();
        
        //search for copied file
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/app:company_home/cm:TaxonomyRoot/cm:test.txt" + "\"");
    
        //there should only be 1 copied file
        assertEquals(1, resultSet.length());
        
        NodeRef checkFileLink = resultSet.getNodeRef(0);
        QName type = nodeService.getType(checkFileLink);
        
        //check to make sure that the file was deleted and a link was created to original file
        assertEquals("filelink", type.getLocalName());
        userTransaction2.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
          try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
     
      try
      {
        userTransaction3.begin();
        Serializable taxonomyRootProps = nodeService.getProperty(taxonomyRootFolder, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> taxonomyCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, taxonomyRootProps);
    
        //make sure taxonomy root only contains one category
        assertEquals(1,taxonomyCategories.size());
        NodeRef taxonomyCategory = taxonomyCategories.iterator().next();
        
        Serializable testFileProps = nodeService.getProperty(testFile, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> testFileCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, testFileProps);
    
        //make sure testFile contains the taxonomy root category
        assertTrue(testFileCategories.contains(taxonomyCategory));
    
        //deleteNodes
        nodeService.deleteNode(testFile);
        nodeService.deleteNode(taxonomyRootFolder);
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
          try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }
      
      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      userTransactionFinally.commit();
    }
}
  
  /**
   * Test the behavior of cm:content objects as they are added to
   * taxonomies.
   * This test will copy a file into a folder in the taxonomy root
   * @throws Exception
   */
  public void testCopyContentBehaviorIntoFolder() throws Exception {

    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction(); 
    ResultSet resultSet;
    NodeRef testFile = null;
    NodeRef taxonomyChildFolder;
    NodeRef taxonomyRootFolder = null;
    
    try
    {

      try
      {
        userTransaction1.begin();
        NodeRef companyHome = nodeUtils.getCompanyHome();
        //create a folder
        NodeRef parentFolder = nodeUtils.getCompanyHome();
        String taxonomyRootFolderName = "TaxonomyRoot";
        taxonomyRootFolder = nodeUtils.createFolder(parentFolder, taxonomyRootFolderName);
  
        //Apply the taxonomy_root aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a folder in the taxonomy root
        String taxonomyChildFolderName = "TaxonomyChildFolder";
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        //create test file to be copied
        testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");
        
        //copy test file to childFolder
        copyService.copy(testFile, taxonomyChildFolder, 
            ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, taxonomyChildFolderName));
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
          try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //search for copied file
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/app:company_home/cm:TaxonomyRoot/cm:TaxonomyChildFolder/cm:test.txt" + "\"");
  
        //there should only be 1 copied file
        assertEquals(1, resultSet.length());
  
        NodeRef checkFileLink = resultSet.getNodeRef(0);
        QName type = nodeService.getType(checkFileLink);
        
        //check to make sure that the file was deleted and a link was created to original file
        assertEquals("filelink", type.getLocalName());
        userTransaction2.commit();
      }
      catch(AssertionFailedError e)
      {
          try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
      catch(Exception e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction3.begin();
        
        Serializable childFolderProps = nodeService.getProperty(taxonomyChildFolder, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> childFolderCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, childFolderProps);
  
        //make sure childFolder only contains one category
        assertEquals(1,childFolderCategories.size());
        NodeRef childCategory = childFolderCategories.iterator().next();
        
        Serializable testFileProps = nodeService.getProperty(testFile, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> testFileCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, testFileProps);
  
        //make sure testFile contains the childFolder category
        assertTrue(testFileCategories.contains(childCategory));
  
        //deleteNodes
        nodeService.deleteNode(testFile);
        nodeService.deleteNode(taxonomyRootFolder);
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
          try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }
      
      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      userTransactionFinally.commit();
   }
}

  /**
   * Test the behavior of cm:content objects as they are added to
   * taxonomies.
   * This test will move a file into the taxonomy root
   * @throws Exception
   */  
  public void testMoveContentBehaviorIntoTaxonomyRoot() throws Exception {
  UserTransaction userTransaction1 = transactionService.getUserTransaction();
  UserTransaction userTransaction2 = transactionService.getUserTransaction();
  UserTransaction userTransaction3 = transactionService.getUserTransaction();
  NodeRef taxonomyRootFolder = null;
  NodeRef testFile = null;
  String taxonomyRootFolderName = "TaxonomyRoot";
  
  try
  {
    try
    {
      userTransaction1.begin();
      NodeRef companyHome = nodeUtils.getCompanyHome();
      
      //create a folder
      NodeRef parentFolder = nodeUtils.getCompanyHome();
      
      taxonomyRootFolder = nodeUtils.createFolder(parentFolder, taxonomyRootFolderName);
  
      //Apply the taxonomy_root aspect
      nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
      
      //create test file to be moved
      testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");
      
      userTransaction1.commit();
    }
      catch(Exception e)
      {
        try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //try to move node into taxonomy root folder
        nodeService.moveNode(testFile, taxonomyRootFolder, ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, taxonomyRootFolderName));
        System.out.println("MOVED NODE");
        
        userTransaction2.commit();

        //An exception should be thrown so the moveNode should fail
        fail();
        
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
      }
      
      try
      {
        userTransaction3.begin();
        
        //delete nodes
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(testFile);
        userTransaction3.commit();
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }
      
      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      userTransactionFinally.commit();
    }
 }

  
  /**
   * Test the behavior of cm:content objects as they are added to
   * taxonomies.
   * This test will move a file into a folder in the taxonomy root
   * @throws Exception
   */
  public void testMoveContentBehaviorIntoFolder() throws Exception {
  UserTransaction userTransaction1 = transactionService.getUserTransaction();
  UserTransaction userTransaction2 = transactionService.getUserTransaction();
  UserTransaction userTransaction3 = transactionService.getUserTransaction();
  NodeRef taxonomyRootFolder = null;
  NodeRef testFile = null;
  String taxonomyRootFolderName = "TaxonomyRoot";
  NodeRef taxonomyChildFolder;
  String taxonomyChildFolderName;
  
  try
  {
    try
    {
      userTransaction1.begin();
      NodeRef companyHome = nodeUtils.getCompanyHome();
      
      //create a folder
      NodeRef parentFolder = nodeUtils.getCompanyHome();
      
      taxonomyRootFolder = nodeUtils.createFolder(parentFolder, taxonomyRootFolderName);

      //Apply the taxonomy_root aspect
      nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
      
      //create a folder in the taxonomy root
      taxonomyChildFolderName = "ChildFolder";
      taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
      
      //create test file to be moved into ChildFolder folder
      testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");
      
      userTransaction1.commit();
    }
    catch(Exception e)
    {
      try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
      throw e;
    }

    try
    {
      userTransaction2.begin();
      
      //try to move node into taxonomy root folder
      nodeService.moveNode(testFile, taxonomyChildFolder, ContentModel.ASSOC_CONTAINS,
          QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, taxonomyChildFolderName));
      userTransaction2.commit();
      
      //An exception should be thrown so the moveNode should fail
      fail();
    }
    catch(AssertionFailedError e)
    {
      try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
      throw e;
    }
    catch(Exception  e)
    {
      try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
    }
    try
    {
      userTransaction3.begin();
      
      //delete nodes
      nodeService.deleteNode(taxonomyRootFolder);
      nodeService.deleteNode(testFile);
      userTransaction3.commit();
    }
    catch(Exception e)
    {
      try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
      throw e;
    }
  }
  catch(Exception e)
  {
    throw e;
  }
  finally
  {
    UserTransaction userTransactionFinally = transactionService.getUserTransaction();
    userTransactionFinally.begin();
    
    if(nodeService.exists(taxonomyRootFolder) == true)
    {
      nodeService.deleteNode(taxonomyRootFolder);
    }
    
    if(nodeService.exists(testFile) == true)
    {
      nodeService.deleteNode(testFile);
    }
    userTransactionFinally.commit();
  }
}

  
  /**
   * Test the behavior of app:filelink objects as they are added to
   * taxonomies
   * This test will create a new link inside the taxonomy root
   * @throws Exception
   */
  public void testCreateLinkBehaviorInsideTaxonomyRoot() throws Exception {

    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    UserTransaction userTransaction5 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    NodeRef testFile = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef companyHome;
    NodeRef newLink;

    try
    {
      try
      {
        userTransaction1.begin();
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        NodeRef parentFolder = nodeUtils.getCompanyHome();
        
        taxonomyRootFolder = nodeUtils.createFolder(parentFolder, taxonomyRootFolderName);

        //Apply the taxonomy_root aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create test file
        testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");

        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        // create link to original file      
        newLink = nodeUtils.createLinkedFile(testFile, taxonomyRootFolder);

        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction3.begin();
        Serializable taxonomyRootProps = nodeService.getProperty(taxonomyRootFolder, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> taxonomyCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, taxonomyRootProps);
    
        //make sure taxonomy root only contains one category
        assertEquals(1,taxonomyCategories.size());
        NodeRef taxonomyCategory = taxonomyCategories.iterator().next();
        
        Serializable newLinkProps = nodeService.getProperty(newLink, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> newLinkCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, newLinkProps);
    
        //make sure the new link contains the taxonomy root category
        assertTrue(newLinkCategories.contains(taxonomyCategory));
        
        Serializable testFileProps = nodeService.getProperty(newLink, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> testFileCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, testFileProps);
        
        //make sure the test file has the taxonomy root category
        assertTrue(testFileCategories.contains(taxonomyCategory));
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        
        //check to make sure the new link has the taxonomyLink aspect
        assertTrue(nodeService.hasAspect(newLink, CatConstants.ASPECT_TAXONOMY_LINK));
        
        userTransaction4.commit();
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction5.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(testFile);
        userTransaction5.commit();
        
      }
      catch(Exception e)
      {
        try{userTransaction5.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }
      
      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      userTransactionFinally.commit();
    }
  }
  
  
  /**
   * Test the behavior of app:filelink objects as they are added to
   * taxonomies
   * This test will create a new link inside a folder in the taxonomy root
   * @throws Exception
   */
  public void testCreateLinkBehaviorInsideFolder() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    UserTransaction userTransaction5 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    NodeRef testFile = null;
    NodeRef taxonomyChildFolder;
    String taxonomyRootFolderName = "TaxonomyRoot";
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    NodeRef companyHome;
    NodeRef newLink;

    try
    {
      try
      {
        userTransaction1.begin();
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        NodeRef parentFolder = nodeUtils.getCompanyHome();
        
        taxonomyRootFolder = nodeUtils.createFolder(parentFolder, taxonomyRootFolderName);

        //Apply the taxonomy_root aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a folder in the taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        //create test file
        testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");

        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        // create link to original file      
        newLink = nodeUtils.createLinkedFile(testFile, taxonomyChildFolder);

        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction3.begin();
        Serializable childFolderProps = nodeService.getProperty(taxonomyChildFolder, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> childFolderCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, childFolderProps);
    
        //make sure childFolder only contains one category
        assertEquals(1,childFolderCategories.size());
        NodeRef childFolderCategory = childFolderCategories.iterator().next();
        
        Serializable newLinkProps = nodeService.getProperty(newLink, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> newLinkCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, newLinkProps);
    
        //make sure the new link contains the childFolder category
        assertTrue(newLinkCategories.contains(childFolderCategory));
        
        Serializable testFileProps = nodeService.getProperty(newLink, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> testFileCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, testFileProps);
        
        //make sure the test file has the childFolder category
        assertTrue(testFileCategories.contains(childFolderCategory));
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        
        //check to make sure the new link has the taxonomyLink aspect
        assertTrue(nodeService.hasAspect(newLink, CatConstants.ASPECT_TAXONOMY_LINK));
        
        userTransaction4.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction4.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction5.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(testFile);
        userTransaction5.commit();
        
      }
      catch(Exception e)
      {
        try{userTransaction5.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }
      
      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      userTransactionFinally.commit();
    }
  }

  
  /**
   * Test the behavior of app:filelink objects as they are added to
   * taxonomies
   * This test will copy a link to the taxonomy root
   * @throws Exception
   */
  public void testCopyLinkBehaviorInsideTaxonomyRoot() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    UserTransaction userTransaction5 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    NodeRef testFile = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef companyHome;
    NodeRef newLink;
    NodeRef originalLink;
    NodeRef regularFolder = null;
    String regularFolderName = "RegularFolder";
    
    try
    {
      try
      {
        userTransaction1.begin();
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        NodeRef parentFolder = nodeUtils.getCompanyHome();
        taxonomyRootFolder = nodeUtils.createFolder(parentFolder, taxonomyRootFolderName);

        //Apply the taxonomy_root aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a folder
        regularFolder = nodeUtils.createFolder(companyHome, regularFolderName);
        
        //create test file
        testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");

        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        // create link to original file to testFolder    
        originalLink = nodeUtils.createLinkedFile(testFile, regularFolder);
        
        // copy link to taxonomy root
        newLink = copyService.copy(originalLink, taxonomyRootFolder, ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, taxonomyRootFolderName),true);
        
        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      try
      {
        userTransaction3.begin();
        Serializable taxonomyRootProps = nodeService.getProperty(taxonomyRootFolder, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> taxonomyRootCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, taxonomyRootProps);
    
        //make sure taxonomy root only contains one category
        assertEquals(1,taxonomyRootCategories.size());
        NodeRef taxonomyRootCategory = taxonomyRootCategories.iterator().next();
        
        Serializable newLinkProps = nodeService.getProperty(newLink, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> newLinkCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, newLinkProps);
    
        //make sure the new link contains the taxonomy root category
        assertTrue(newLinkCategories.contains(taxonomyRootCategory));
        
        Serializable testFileProps = nodeService.getProperty(newLink, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> testFileCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, testFileProps);
        
        //make sure the test file has the taxonomy root category
        assertTrue(testFileCategories.contains(taxonomyRootCategory));
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        
        //check to make sure the new link has the taxonomyLink aspect
        assertTrue(nodeService.hasAspect(newLink, CatConstants.ASPECT_TAXONOMY_LINK));
        
        userTransaction4.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction4.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction5.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(testFile);
        nodeService.deleteNode(regularFolder);
        userTransaction5.commit();
        
      }
      catch(Exception e)
      {
        try{userTransaction5.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }
      
      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      
      if(nodeService.exists(regularFolder) == true)
      {
        nodeService.deleteNode(regularFolder);
      }
      userTransactionFinally.commit();
    }

}


  /**
 * Test the behavior of app:filelink objects as they are added to
 * taxonomies
 * This test will copy a link to a folder in a taxonomy root
 * @throws Exception
 */
  public void testCopyLinkBehaviorInsideFolder() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    UserTransaction userTransaction5 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    NodeRef testFile = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef companyHome;
    NodeRef newLink;
    NodeRef originalLink;
    NodeRef regularFolder = null;
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    String regularFolderName = "RegularFolder";
    
    try
    {
      try
      {
        userTransaction1.begin();
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        NodeRef parentFolder = nodeUtils.getCompanyHome();
        taxonomyRootFolder = nodeUtils.createFolder(parentFolder, taxonomyRootFolderName);
    
        //Apply the taxonomy_root aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a folder inside of the taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        //create a folder
        regularFolder = nodeUtils.createFolder(companyHome, regularFolderName);
        
        //create test file
        testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");
    
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        // create link to original file to testFolder    
        originalLink = nodeUtils.createLinkedFile(testFile, regularFolder);
        
        // copy link to childFolder inside the taxonomy root
        newLink = copyService.copy(originalLink, taxonomyChildFolder, ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, taxonomyChildFolderName),true);
        
        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction3.begin();
        Serializable childFolderProps = nodeService.getProperty(taxonomyChildFolder, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> childFolderCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, childFolderProps);
    
        //make sure childFolder only contains one category
        assertEquals(1,childFolderCategories.size());
        NodeRef childFolderCategory = childFolderCategories.iterator().next();
        
        Serializable newLinkProps = nodeService.getProperty(newLink, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> newLinkCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, newLinkProps);
    
        //make sure the new link contains the childFolder category
        assertTrue(newLinkCategories.contains(childFolderCategory));
        
        Serializable testFileProps = nodeService.getProperty(newLink, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> testFileCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, testFileProps);
        
        //make sure the test file has the childFolder category
        assertTrue(testFileCategories.contains(childFolderCategory));
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        
        //check to make sure the new link has the taxonomyLink aspect
        assertTrue(nodeService.hasAspect(newLink, CatConstants.ASPECT_TAXONOMY_LINK));
        
        userTransaction4.commit();
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction5.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(testFile);
        nodeService.deleteNode(regularFolder);
        userTransaction5.commit();
        
      }
      catch(Exception e)
      {
        try{userTransaction5.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }
      
      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      
      if(nodeService.exists(regularFolder) == true)
      {
        nodeService.deleteNode(regularFolder);
      }
      userTransactionFinally.commit();
    }
}


  /**
 * Test the behavior of app:filelink objects as they are added to
 * taxonomies
 * This test will move a link to a folder in a taxonomy root
 * @throws Exception
 */
/*public void testMoveLinkBehvaiorInsideTaxonomyRoot() throws Exception {
  UserTransaction userTransaction1 = transactionService.getUserTransaction();
  
  NodeRef taxonomyRootFolder;
  NodeRef testFile;
  String folderName = "TaxonomyRoot";
  NodeRef companyHome;
  NodeRef newLink;
  NodeRef originalLink;
  NodeRef testFolder;
  String testFolderName = "TestFolder";
  try
  {
    userTransaction1.begin();
    companyHome = nodeUtils.getCompanyHome();
    
    //create a folder
    NodeRef parentFolder = nodeUtils.getCompanyHome();
    taxonomyRootFolder = nodeUtils.createFolder(parentFolder, folderName);

    //Apply the taxonomy_root aspect
    nodeUtils.createTaxonomyRoot(taxonomyRootFolder);
    
    //create a folder
    testFolder = nodeUtils.createFolder(companyHome, testFolderName);
    
    //create test file
    testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");

    userTransaction1.commit();
  }
  catch(Exception e)
  {
    try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
    throw e;
  }
  
  UserTransaction userTransaction2 = transactionService.getUserTransaction();
  try
  {
    userTransaction2.begin();
    
    // create link to original file to testFolder    
    originalLink = nodeUtils.createLinkedFile(testFile, testFolder);
    
    newLink = nodeService.moveNode(originalLink, taxonomyRootFolder, ContentModel.ASSOC_CONTAINS,
        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folderName)).getChildRef();
    
    userTransaction2.commit();
  }
  catch(Exception e)
  {
    try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
    throw e;
  }
}
*/

 
 /**
 * Test the behavior of app:filelink objects as they are added to
 * taxonomies
 * This test will move a link to a taxonomy root
 * @throws Exception
 */
  public void testMoveLinkBehvaiorInsideFolder() throws Exception {
  
}

  
  /**
   * Test the behavior of cm:folder objects as they are added to
   * taxonomies
   * This test will create a new folder in a taxonomy root
   * @throws Exception
   */
  public void testCreateFolderBehaviorInsideTaxonomyRoot() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef companyHome;
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    ResultSet resultSet;
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create folder inside taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //make sure childFolder has the taxonomyFolder aspect
        assertTrue(nodeService.hasAspect(taxonomyChildFolder, CatConstants.ASPECT_TAXONOMY_FOLDER));
        
        userTransaction2.commit();
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction3.begin();
        
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/cm:generalclassifiable/cm:TaxonomyRoot/cm:" + taxonomyChildFolderName + "\"");
        
        //make sure the subcategory childFolder is present and also make sure it has the same name as the folder
        assertEquals(1, resultSet.length());
        userTransaction3.commit();
        
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        userTransaction4.commit();
        
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      userTransactionFinally.commit();
    }
  }
  
  
  /**
   * Test the behavior of cm:folder objects as they are added to
   * taxonomies
   * This test will create a new folder in a taxonomy root folder
   * @throws Exception
   */
  public void testCreateFolderBehaviorInsideTaxonomyRootChildFolder() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef companyHome;
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    NodeRef childFolderOfTaxonomyChildFolder;
    String childFolderNameOfTaxonomyChildFolder = "ChildFolderOfTaxonomyChildFolder";
    ResultSet resultSet;
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create folder inside taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        //create folder inside child taxonomy root folder
        childFolderOfTaxonomyChildFolder = nodeUtils.createFolder(taxonomyChildFolder, 
            childFolderNameOfTaxonomyChildFolder);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //make sure childFolder has the taxonomyFolder aspect
        assertTrue(nodeService.hasAspect(childFolderOfTaxonomyChildFolder, CatConstants.ASPECT_TAXONOMY_FOLDER));
        
        userTransaction2.commit();
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction3.begin();
        
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/cm:generalclassifiable/cm:TaxonomyRoot/cm:TaxonomyChildFolder/cm:" + childFolderNameOfTaxonomyChildFolder + "\"");
        
        //make sure the subcategory childFolder is present and also make sure it has the same name as the folder
        assertEquals(1, resultSet.length());
        userTransaction3.commit();
        
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        userTransaction4.commit();
        
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      userTransactionFinally.commit();
    }
}

  
  /**
   * Test the behavior of cm:folder objects as they are added to
   * taxonomies
   * This test will copy a regular folder into a taxonomy root
   * @throws Exception
   */
  public void testCopyRegularFolderBehaviorIntoTaxonomyRoot() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef companyHome;
    NodeRef regularFolder = null;
    String regularFolderName = "RegularFolder";
    ResultSet resultSet;
    NodeRef copiedRegularFolder;
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a regular folder in company_home
        regularFolder = nodeUtils.createFolder(companyHome, regularFolderName);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //copy regular folder into taxonomy root
        copiedRegularFolder = copyService.copy(regularFolder, taxonomyRootFolder, ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, taxonomyRootFolderName),true);
        
        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction3.begin();
        
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/cm:generalclassifiable/cm:TaxonomyRoot/cm:" + regularFolderName + "\"");
        
        //make sure a subcategory is present in the copied folder and also make sure it has the same name as the folder
        assertEquals(1, resultSet.length());
        
        //make sure the copied folder inside the taxonomy root has the taxonomyFolder aspect
        assertTrue(nodeService.hasAspect(copiedRegularFolder, CatConstants.ASPECT_TAXONOMY_FOLDER));
        
        userTransaction3.commit();
        
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(regularFolder);
        userTransaction4.commit();
        
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      if(nodeService.exists(regularFolder) == true)
      {
        nodeService.deleteNode(regularFolder);
      }
      userTransactionFinally.commit();
    }

}

  
  /**
   * Test the behavior of cm:folder objects as they are added to
   * taxonomies
   * This test will move a regular folder into a taxonomy root
   * @throws Exception
   */
  public void testMoveRegularFolderBehvaiorIntoTaxononmyRoot() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef companyHome;
    NodeRef regularFolder = null;
    String regularFolderName = "RegularFolder";

    try
    {
      try
      {
        userTransaction1.begin();
        
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a regular folder in company_home
        regularFolder = nodeUtils.createFolder(companyHome, regularFolderName);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //try to move the regular folder into taxonomy root folder
        nodeService.moveNode(regularFolder, taxonomyRootFolder, ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, taxonomyRootFolderName));
        userTransaction2.commit();
        
        //An exception should be thrown so the moveNode should fail
        fail();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception  e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
      }
      
      try
      {
        userTransaction3.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(regularFolder);
        userTransaction3.commit();
      }
      catch(Exception  e)
      {
        try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      if(nodeService.exists(regularFolder) == true)
      {
        nodeService.deleteNode(regularFolder);
      }
      userTransactionFinally.commit();
    }
  }

  
  /**
   * Test the taxonomyFolder aspect behavior
   * This test will copy a taxonomy folder to a regular folder
   * @throws Exception
   */

//rename a taxonomy folder
//delete a taxonomy folder
//
  public void testCopyTaxonomyFolderBehaviorIntoRegularFolder() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef companyHome;
    NodeRef regularFolder = null;
    String regularFolderName = "RegularFolder";
    NodeRef copiedTaxonomyFolder;
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a folder inside taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        //create a regular folder in company_home
        regularFolder = nodeUtils.createFolder(companyHome, regularFolderName);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //copy taxonomy folder into regular folder
        copiedTaxonomyFolder = copyService.copy(taxonomyChildFolder, regularFolder, 
            ContentModel.ASSOC_CONTAINS,  
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, regularFolderName),true);
        
        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }    
      
      try
      {
        userTransaction3.begin();
        
        //make sure copied folder does not have the taxonomyFolder aspect
        assertFalse(nodeService.hasAspect(copiedTaxonomyFolder, CatConstants.ASPECT_TAXONOMY_FOLDER));
        
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(regularFolder);
        userTransaction4.commit();
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      if(nodeService.exists(regularFolder) == true)
      {
        nodeService.deleteNode(regularFolder);
      }
      userTransactionFinally.commit();
    }
}    
  
  /**
   * Test the taxonomyFolder aspect behavior
   * This test will copy a taxonomy folder to another taxonomy
   * @throws Exception
   */
  public void testCopyTaxonomyFolderBehaviorIntoAnotherTaxonomy() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    NodeRef anotherTaxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    String anotherTaxonomyRootFolderName = "AnotherTaxonomyRoot";
    NodeRef companyHome;
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    ResultSet resultSet;
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create folder inside taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        //create another folder to copy to
        anotherTaxonomyRootFolder = nodeUtils.createFolder(companyHome, anotherTaxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(anotherTaxonomyRootFolder);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        copyService.copy(taxonomyChildFolder, anotherTaxonomyRootFolder,
            ContentModel.ASSOC_CONTAINS, 
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, anotherTaxonomyRootFolderName),true);
        
        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        
        userTransaction3.begin();
        
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/cm:generalclassifiable/cm:AnotherTaxonomyRoot/cm:" + taxonomyChildFolderName + "\"");
        
        //make sure the subcategory and correct name is found for the copied folder
        assertEquals(1, resultSet.length());
        
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        nodeService.deleteNode(anotherTaxonomyRootFolder);
        nodeService.deleteNode(taxonomyRootFolder);
        userTransaction4.commit();
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      if(nodeService.exists(anotherTaxonomyRootFolder) == true)
      {
        nodeService.deleteNode(anotherTaxonomyRootFolder);
      }
      userTransactionFinally.commit();
    }
  }

  
  /**
   * Test the taxonomyFolder aspect behavior
   * This test will move a taxonomy folder to another taxonomy
   * @throws Exception
   */
  public void testMoveTaxonomyFolderBehaviorIntoAnotherTaxonomy() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    NodeRef anotherTaxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    String anotherTaxonomyRootFolderName = "AnotherTaxonomyRoot";
    NodeRef companyHome;
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    ResultSet resultSet;
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create folder inside taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        //create another folder to copy to
        anotherTaxonomyRootFolder = nodeUtils.createFolder(companyHome, anotherTaxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(anotherTaxonomyRootFolder);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        nodeService.moveNode(taxonomyChildFolder, anotherTaxonomyRootFolder,
             ContentModel.ASSOC_CONTAINS, 
             QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, anotherTaxonomyRootFolderName)).getChildRef();
        
        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try{userTransaction2.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        
        userTransaction3.begin();
        
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/cm:generalclassifiable/cm:AnotherTaxonomyRoot/cm:" + taxonomyChildFolderName + "\"");
        
        //make sure the subcategory and correct name is found for the copied folder
        assertEquals(1, resultSet.length());
        
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        nodeService.deleteNode(anotherTaxonomyRootFolder);
        nodeService.deleteNode(taxonomyRootFolder);
        userTransaction4.commit();
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      if(nodeService.exists(anotherTaxonomyRootFolder) == true)
      {
        nodeService.deleteNode(anotherTaxonomyRootFolder);
      }
      userTransactionFinally.commit();
    }
  }
  

  /**
   * Test the taxonomyFolder aspect behavior
   * This test will move a taxonomy folder to a regular folder
   * @throws Exception
   */
  public void testMoveTaxonomyFolderBehaviorIntoRegularFolder() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    NodeRef companyHome;
    NodeRef regularFolder = null;
    String regularFolderName = "RegualrFolder";

    try
    {
      try
      {
        userTransaction1.begin();
        
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a folder in the taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        //create a regular folder in company_home
        regularFolder = nodeUtils.createFolder(companyHome, regularFolderName);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //try to move the taxonomy folder into a regular folder
        nodeService.moveNode(taxonomyChildFolder, regularFolder, ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, regularFolderName));
        userTransaction2.commit();
        
        //An exception should be thrown so the moveNode should fail
        fail();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception  e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
      }
      
      try
      {
        userTransaction3.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(regularFolder);
        userTransaction3.commit();
      }
      catch(Exception  e)
      {
        try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      if(nodeService.exists(regularFolder) == true)
      {
        nodeService.deleteNode(regularFolder);
      }
      userTransactionFinally.commit();
    }
}


 
  /**
   * Test the taxonomyFolder aspect behavior
   * This test will copy a taxonomy root folder into another taxonomy folder
   * @throws Exception
   */
  public void testCopyTaxonomyRootFolderBehaviorIntoAnotherTaxonomy() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    NodeRef companyHome;
    NodeRef anotherTaxonomyRootFolder = null;
    String anotherTaxonomyRootFolderName = "AnotherTaxonomyRoot";

    try
    {
      try
      {
        userTransaction1.begin();
        
        companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy root aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a folder in the taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        //create another taxonomy root folder in company_home
        anotherTaxonomyRootFolder = nodeUtils.createFolder(companyHome, anotherTaxonomyRootFolderName);
        
        //add taxonomy root aspect
        nodeUtils.addTaxonomyRootAspect(anotherTaxonomyRootFolder);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try{userTransaction1.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //try to copy the taxonomy root folder into another taxonomy
        copyService.copy(anotherTaxonomyRootFolder, taxonomyChildFolder,
             ContentModel.ASSOC_CONTAINS,
             QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, taxonomyChildFolderName), true);
        userTransaction2.commit();
        
        //An exception should be thrown so the moveNode should fail
        fail();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception  e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
      }
      
      try
      {
        userTransaction3.begin();
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(anotherTaxonomyRootFolder);
        userTransaction3.commit();
      }
      catch(Exception  e)
      {
        try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      if(nodeService.exists(anotherTaxonomyRootFolder) == true)
      {
        nodeService.deleteNode(anotherTaxonomyRootFolder);
      }
      userTransactionFinally.commit();
    }
}

  
  /**
   * Test the taxonomyFolder aspect behavior
   * This test will rename a taxonomy folder
   * @throws Exception
   */
  public void testRenameTaxononmyFolderBehavior() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    UserTransaction userTransaction5 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    ResultSet resultSet;
    String newFolderName;
    NodeRef category;
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        NodeRef companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy root aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create folder in taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
          try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        // Find folder
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/cm:generalclassifiable/cm:TaxonomyRoot/cm:" + taxonomyChildFolderName + "\"");

        // Make sure it only finds one folder
        assertEquals(1, resultSet.length());
        userTransaction2.commit();       
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
          try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
      
      try
      {
        userTransaction3.begin();
        
        category = resultSet.getNodeRef(0);
        
        // Rename the folder
        newFolderName = taxonomyChildFolderName + "2";
        nodeService.moveNode(taxonomyChildFolder, taxonomyRootFolder, ContentModel.ASSOC_CONTAINS, 
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newFolderName));
        nodeService.setProperty(taxonomyChildFolder, ContentModel.PROP_NAME, newFolderName);
        userTransaction3.commit();        
      }
      catch(Exception e)
      {
          try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
      
      try
      {
        
        userTransaction4.begin();
        
        // Check that the rename renamed the corresponding Category too
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/cm:generalclassifiable/cm:TaxonomyRoot/cm:" + taxonomyChildFolderName + "\"");
        assertEquals(0, resultSet.length());
        
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/cm:generalclassifiable/cm:TaxonomyRoot/cm:" + newFolderName + "\"");
        assertEquals(1, resultSet.length());
        
        assertEquals(newFolderName, (String)nodeService.getProperty(category, ContentModel.PROP_NAME));
        userTransaction4.commit();        
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction4.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
          try { userTransaction4.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
      
      try
      {
        
        userTransaction5.begin();
        // Delete the folder
        nodeService.deleteNode(taxonomyRootFolder);
        
        // Check the category got deleted
        assertEquals(false, nodeService.exists(category));
        userTransaction5.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction5.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
          try { userTransaction5.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }
      userTransactionFinally.commit();
    }
}

  /**
   * Test the taxonomyFolder aspect behavior
   * This test will delete a taxonomy folder
   * @throws Exception
   */
  public void testDeleteTaxonomyFolderBehavior() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    ResultSet resultSet;
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        NodeRef companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy root aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create folder in taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
          try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
          throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        // Find folder
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/cm:generalclassifiable/cm:TaxonomyRoot/cm:" + taxonomyChildFolderName + "\"");

        // Make sure it only finds one folder
        assertEquals(1, resultSet.length());
        
        //Delete taxonomyChildFolder
        nodeService.deleteNode(taxonomyChildFolder);
        
        userTransaction2.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction3.begin();
        
        // Try to find deleted folder
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/cm:generalclassifiable/cm:TaxonomyRoot/cm:" + taxonomyChildFolderName + "\"");

        // Make sure it only finds one folder
        assertEquals(0, resultSet.length());
        
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }
      userTransactionFinally.commit();
    }
}

  /**
   * Test the taxonomyLink aspect behavior
   * This test will copy a link from one taxonomy to another
   * @throws Exception
   */
  public void testCopyTaxonomyLinkBehaviorIntoAnotherTaxonomy() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef taxonomyChildFolder;
    String taxonomyChildFolderName = "TaxonomyChildFolder";
    NodeRef testFile = null;
    NodeRef anotherTaxonomyRootFolder = null;
    String anotherTaxonomyRootFolderName = "AnotherTaxonomyRoot";
    NodeRef originalLink;
    NodeRef newLink;
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        NodeRef companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy root aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create folder in taxonomy root
        taxonomyChildFolder = nodeUtils.createFolder(taxonomyRootFolder, taxonomyChildFolderName);
        
        //create a folder
        anotherTaxonomyRootFolder = nodeUtils.createFolder(companyHome, anotherTaxonomyRootFolderName);
        
        //add taxonomy root aspect
        nodeUtils.addTaxonomyRootAspect(anotherTaxonomyRootFolder);
        
        //create test file
        testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //create original link between testFile and taxonomyChildFolder
        originalLink = nodeUtils.createLinkedFile(testFile, taxonomyChildFolder);
        
        // copy link to another taxonomy root
        newLink = copyService.copy(originalLink, anotherTaxonomyRootFolder, ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, anotherTaxonomyRootFolderName),true);
        
        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction3.begin();
        Serializable childFolderProps = nodeService.getProperty(taxonomyChildFolder, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> childFolderCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, childFolderProps);
    
        //make sure taxonomyChildFolder only contains one category
        assertEquals(1,childFolderCategories.size());
        NodeRef childFolderCategory = childFolderCategories.iterator().next();
        
        Serializable originalLinkProps = nodeService.getProperty(originalLink, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> originalLinkCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, originalLinkProps);
    
        //make sure the original link contains the taxonomyChildFolder category
        assertTrue(originalLinkCategories.contains(childFolderCategory));
        
        Serializable testFileProps = nodeService.getProperty(testFile, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> testFileCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, testFileProps);
        
        //make sure the test file has the taxonomyChildFolder category
        assertTrue(testFileCategories.contains(childFolderCategory));
        
        Serializable anotherTaxonomyRootFolderProps = nodeService.getProperty(anotherTaxonomyRootFolder,CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> anotherTaxonomyRootFolderCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class,anotherTaxonomyRootFolderProps);
        
        //make sure anotherTaxonomyRootFolder only contains one category
        assertEquals(1, anotherTaxonomyRootFolderCategories.size());
        NodeRef anotherTaxonomyRootFolderCategory = anotherTaxonomyRootFolderCategories.iterator().next();
        
        //make sure the test file has the anotherTaxonomyRootFolder category
        assertTrue(testFileCategories.contains(anotherTaxonomyRootFolderCategory));
        
        Serializable newLinkProps = nodeService.getProperty(newLink,CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> newLinkCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class,newLinkProps);
        
        //make sure the new link contains the anotherTaxonomyRootFolderCategory
        assertTrue(newLinkCategories.contains(anotherTaxonomyRootFolderCategory));
        
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try{userTransaction3.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(anotherTaxonomyRootFolder);
        nodeService.deleteNode(testFile);
        
        userTransaction4.commit();
      }
      catch(Exception e)
      {
        try{userTransaction4.rollback();} catch (IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }
      if(nodeService.exists(anotherTaxonomyRootFolder) == true)
      {
        nodeService.deleteNode(anotherTaxonomyRootFolder);
      }
      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      userTransactionFinally.commit();
    }
}

  /**
   * Test the taxonomyLink aspect behavior
   * This test will delete a taxonomy link
   * @throws Exception
   */
  public void testDeleteTaxonomyLinkBehavior() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    UserTransaction userTransaction5 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef testFile = null;
    NodeRef originalLink;
    ResultSet resultSet;
    
    try
    {
      try
      {
       userTransaction1.begin();
       
       NodeRef companyHome = nodeUtils.getCompanyHome();
       
       //create a folder
       taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
       
       //add taxonomy root aspect
       nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
       
       //create test file
       testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");
       
       userTransaction1.commit();
      }
      catch(Exception e)
      {
        try {userTransaction1.rollback();} catch(IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();
        
        //create linked file
        originalLink = nodeUtils.createLinkedFile(testFile, taxonomyRootFolder);
        
        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try {userTransaction2.rollback();} catch(IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction3.begin();
        
        //make sure new link exists
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/app:company_home/cm:TaxonomyRoot/cm:test.txt" +  "\"");
        
        assertEquals(1, resultSet.length());
        
        Serializable taxonomyRootFolderProps = nodeService.getProperty(taxonomyRootFolder,CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> taxonomyRootFolderCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class,taxonomyRootFolderProps);
        
        //make sure anotherTaxonomyRootFolder only contains one category
        assertEquals(1, taxonomyRootFolderCategories.size());
        NodeRef taxonomyRootFolderCategory = taxonomyRootFolderCategories.iterator().next();
        
        Serializable testFileProps = nodeService.getProperty(testFile, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> testFileCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, testFileProps);
        
        //make sure testFile contains taxonomyRootFolder category
        assertTrue(testFileCategories.contains(taxonomyRootFolderCategory));
        
        userTransaction3.commit();
      }
      catch(AssertionFailedError e)
      {
        try {userTransaction3.rollback();} catch(IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try {userTransaction3.rollback();} catch(IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        
        //delete link
        nodeService.deleteNode(originalLink);
        
        Serializable taxonomyRootFolderProps = nodeService.getProperty(taxonomyRootFolder,CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> taxonomyRootFolderCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class,taxonomyRootFolderProps);
        
        //make sure taxonomyRootFolder only contains one category
        assertEquals(1, taxonomyRootFolderCategories.size());
        NodeRef taxonomyRootFolderCategory = taxonomyRootFolderCategories.iterator().next();
        
        Serializable testFileProps = nodeService.getProperty(testFile, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> testFileCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, testFileProps);
        
        //make sure testFile does NOT contain taxonomyRootFolder category
        assertFalse(testFileCategories.contains(taxonomyRootFolderCategory));
        userTransaction4.commit();
      }
      catch(AssertionFailedError e)
      {
        try {userTransaction4.rollback();} catch(IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try {userTransaction4.rollback();} catch(IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction5.begin();
        
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(testFile);
        
        userTransaction5.commit();
      }
      catch(Exception e)
      {
        try {userTransaction5.rollback();} catch(IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      userTransactionFinally.commit();
    }
}

  /**
   * Test the taxonomyLink aspect behavior
   * This test will copy a taxonomy link into a regular folder
   * @throws Exception
   */
  public void testCopyTaxonomyLinkBehaviorIntoRegularFolder() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    UserTransaction userTransaction5 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef testFile = null;
    NodeRef regularFolder = null;
    String regularFolderName = "RegularFolder";
    NodeRef newLink;
    ResultSet resultSet;
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        NodeRef companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a regular folder
        regularFolder = nodeUtils.createFolder(companyHome, regularFolderName);
        
        //create test file
        testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try { userTransaction1.rollback(); } catch(IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();

        //create linked file to taxonomy root folder
        nodeUtils.createLinkedFile(testFile, taxonomyRootFolder);
        
        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try { userTransaction2.rollback(); } catch(IllegalStateException ee) {}
        throw e;
      }
      try
      {
        userTransaction3.begin();
        
        //find link to original link
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/app:company_home/cm:TaxonomyRoot/cm:test.txt" +  "\"");
        
        // copy link to a regular folder
        newLink = copyService.copy(resultSet.getNodeRef(0), regularFolder, ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, regularFolderName),true);
        
        userTransaction3.commit();
      }
      catch(Exception e)
      {
        try { userTransaction3.rollback(); } catch(IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction4.begin();
        
        //copied link should be deleted
        assertFalse(nodeService.exists(newLink));
        
        //find test file in regular folder
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/app:company_home/cm:RegularFolder/cm:test.txt" +  "\"");
        
        //make sure an original copy exists instead of a link
        assertEquals(1, resultSet.length());
        
        Serializable taxonomyRootFolderProps = nodeService.getProperty(taxonomyRootFolder,CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> taxonomyRootFolderCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class,taxonomyRootFolderProps);
        
        //make sure taxonomyRootFolder only contains one category
        assertEquals(1, taxonomyRootFolderCategories.size());
        NodeRef taxonomyRootFolderCategory = taxonomyRootFolderCategories.iterator().next();
        
        Serializable regularFolderProps = nodeService.getProperty(regularFolder, CatConstants.PROP_CATEGORIES);
        Collection<NodeRef> regularFolderCategories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, regularFolderProps);
        
        //make sure regular folder does not contain the taxonomyRootFolder category
        assertFalse(regularFolderCategories.contains(taxonomyRootFolderCategory));
        userTransaction4.commit();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction4.rollback(); } catch(IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try { userTransaction4.rollback(); } catch(IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction5.begin();
        
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(testFile);
        nodeService.deleteNode(regularFolder);
        
        userTransaction5.commit();
      }
      catch(Exception e)
      {
        try { userTransaction5.rollback(); } catch(IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      
      if(nodeService.exists(regularFolder) == true)
      {
        nodeService.deleteNode(regularFolder);
      }
      userTransactionFinally.commit();
    }
}
  
  /**
   * Test the taxonomyLink aspect behavior
   * This test will move a taxonomy link into a regular folder
   * @throws Exception
   */
  public void testMoveTaxonomyLinkBehaviorIntoRegularFolder() throws Exception {
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    UserTransaction userTransaction2 = transactionService.getUserTransaction();
    UserTransaction userTransaction3 = transactionService.getUserTransaction();
    UserTransaction userTransaction4 = transactionService.getUserTransaction();
    NodeRef taxonomyRootFolder = null;
    String taxonomyRootFolderName = "TaxonomyRoot";
    NodeRef testFile = null;
    NodeRef regularFolder = null;
    String regularFolderName = "RegularFolder";
    ResultSet resultSet;
    
    try
    {
      try
      {
        userTransaction1.begin();
        
        NodeRef companyHome = nodeUtils.getCompanyHome();
        
        //create a folder
        taxonomyRootFolder = nodeUtils.createFolder(companyHome, taxonomyRootFolderName);
        
        //add taxonomy aspect
        nodeUtils.addTaxonomyRootAspect(taxonomyRootFolder);
        
        //create a regular folder
        regularFolder = nodeUtils.createFolder(companyHome, regularFolderName);
        
        //create test file
        testFile = nodeUtils.createTextFile(companyHome, "test.txt", "this is a test");
        
        userTransaction1.commit();
      }
      catch(Exception e)
      {
        try { userTransaction1.rollback(); } catch(IllegalStateException ee) {}
        throw e;
      }
      
      try
      {
        userTransaction2.begin();

        //create linked file to taxonomy root folder
        nodeUtils.createLinkedFile(testFile, taxonomyRootFolder);
        
        userTransaction2.commit();
      }
      catch(Exception e)
      {
        try { userTransaction2.rollback(); } catch(IllegalStateException ee) {}
        throw e;
      }
      try
      {
        userTransaction3.begin();
        
        //find link to original link
        resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
            "PATH:\"/app:company_home/cm:TaxonomyRoot/cm:test.txt" +  "\"");
        
        // move link to a regular folder
        nodeService.moveNode(resultSet.getNodeRef(0), regularFolder, ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, regularFolderName)).getChildRef();
        
        userTransaction3.commit();
        
        fail();
      }
      catch(AssertionFailedError e)
      {
        try { userTransaction3.rollback(); } catch(IllegalStateException ee) {}
        throw e;
      }
      catch(Exception e)
      {
        try { userTransaction3.rollback(); } catch(IllegalStateException ee) {}
      }
      try
      {
        userTransaction4.begin();
        
        nodeService.deleteNode(taxonomyRootFolder);
        nodeService.deleteNode(regularFolder);
        nodeService.deleteNode(testFile);
        
        userTransaction4.commit();
      }
      catch(Exception e)
      {
        try { userTransaction4.rollback(); } catch(IllegalStateException ee) {}
        throw e;
      }
    }
    catch(Exception e)
    {
      throw e;
    }
    finally
    {
      UserTransaction userTransactionFinally = transactionService.getUserTransaction();
      userTransactionFinally.begin();
      
      if(nodeService.exists(taxonomyRootFolder) == true)
      {
        nodeService.deleteNode(taxonomyRootFolder);
      }

      if(nodeService.exists(testFile) == true)
      {
        nodeService.deleteNode(testFile);
      }
      
      if(nodeService.exists(regularFolder) == true)
      {
        nodeService.deleteNode(regularFolder);
      }
      userTransactionFinally.commit();
    }

  }    
  
  
  /**
   * Test the taxonomyRoot aspect behaviour
   */
  public void testTaxonomyRootBehavior()
      throws Exception
  {       
      UserTransaction userTransaction1 = transactionService.getUserTransaction();
      UserTransaction userTransaction2 = transactionService.getUserTransaction();
      UserTransaction userTransaction3 = transactionService.getUserTransaction();
      UserTransaction userTransaction4 = transactionService.getUserTransaction();
      UserTransaction userTransaction5 = transactionService.getUserTransaction();
      ResultSet resultSet;
      String folderName = "BUCKET";
      NodeRef folder = null;
      String newName;
      NodeRef parentFolder;
      NodeRef category;
      
      try
      {
        try
        {
            userTransaction1.begin();
        
            // Create a folder
            parentFolder = nodeUtils.getCompanyHome();

            folder = nodeUtils.createFolder(parentFolder, folderName);
            
            // Apply the taxonomy_root aspect
            nodeUtils.addTaxonomyRootAspect(folder);
            userTransaction1.commit();
        }
        catch(Exception e)
        {
            try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        } 
        
        try
        {
          userTransaction2.begin();
          
          // Find folder
          resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
              "PATH:\"/cm:generalclassifiable/cm:" + folderName + "\"");

          // Make sure it only finds one folder
          assertEquals(1, resultSet.length());
          userTransaction2.commit();       
        }
        catch(AssertionFailedError e)
        {
          try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
          throw e;
        }
        catch(Exception e)
        {
            try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }
          
        try
        {
          userTransaction3.begin();
          
          category = resultSet.getNodeRef(0);
          
          // Rename the folder
          newName = folderName + "2";
          nodeService.moveNode(folder, parentFolder, ContentModel.ASSOC_CONTAINS, 
              QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName));
          nodeService.setProperty(folder, ContentModel.PROP_NAME, newName);
          userTransaction3.commit();        
        }
        catch(Exception e)
        {
            try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }
          
        try
        {
          
          userTransaction4.begin();
          
          // Check that the rename renamed the corresponding Category too
          resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
              "PATH:\"/cm:generalclassifiable/cm:" + folderName + "\"");
          assertEquals(0, resultSet.length());
          
          resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE,
              "PATH:\"/cm:generalclassifiable/cm:" + newName + "\"");
          assertEquals(1, resultSet.length());
          
          assertEquals(newName, (String)nodeService.getProperty(category, ContentModel.PROP_NAME));
          userTransaction4.commit();        
        }
        catch(AssertionFailedError e)
        {
          try { userTransaction4.rollback(); } catch (IllegalStateException ee) {}
          throw e;
        }
        catch(Exception e)
        {
            try { userTransaction4.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }     

        try
        {
          
          userTransaction5.begin();
          // Delete the folder
          nodeService.deleteNode(folder);
          
          // Check the category got deleted
          assertEquals(false, nodeService.exists(category));
          userTransaction5.commit();
        }
        catch(AssertionFailedError e)
        {
          try { userTransaction5.rollback(); } catch (IllegalStateException ee) {}
          throw e;
        }
        catch(Exception e)
        {
            try { userTransaction5.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }
      }
      catch(Exception e)
      {
        throw e;
      }
      finally
      {
        UserTransaction userTransactionFinally = transactionService.getUserTransaction();
        userTransactionFinally.begin();
        
        if(nodeService.exists(folder) == true)
        {
          nodeService.deleteNode(folder);
        }
        userTransactionFinally.commit();
      }
  }
}
