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
package gov.pnnl.cat.tests;

import java.text.DateFormat;
import java.util.Date;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.ISO9075;
import org.springframework.context.ApplicationContext;

public class GeneralTests extends TestCase
{
  private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
  protected NodeService nodeService;
  protected ContentService contentService;
  protected TransactionService transactionService;
  protected AuthenticationComponent authenticationComponent;
  protected SearchService searchService;
  protected PersonService personService;
  protected StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");


  @Override
  public void setUp()
  {
    nodeService = (NodeService)applicationContext.getBean("nodeService");
    personService = (PersonService)applicationContext.getBean("personService");
    contentService = (ContentService)applicationContext.getBean("contentService");
    authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
    transactionService = (TransactionService)applicationContext.getBean("transactionComponent");
    searchService = (SearchService)applicationContext.getBean("searchService");

    // Authenticate as the system user
    AuthenticationUtil.setRunAsUserSystem();
  }

  @Override
  public void tearDown()
  {
    authenticationComponent.clearCurrentSecurityContext();
  }

  public void testPersonProperties() throws Exception
  {       
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    try
    {
      userTransaction1.begin();

      NodeRef person = personService.getPerson("admin");
      String fileName = (String)nodeService.getProperty(person, ContentModel.PROP_NAME);
      System.out.println("admin profile file name = " + fileName);
      
      userTransaction1.commit();
    }
    catch(Exception e)
    {
      try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
      throw e;
    }        

  }

  public void testSearch() throws Exception
  {       
    UserTransaction userTransaction1 = transactionService.getUserTransaction();
    ResultSet resultSet = null;
    ResultSet results2 = null;
    try
    {
      userTransaction1.begin();

      // Create a folder
      String path = "/app:company_home/cm:" + ISO9075.encode("Reference Library")
      + "/cm:Data/cm:CarinaTest.txt";
      System.out.println("encoded path = " + path);
      String query = "PATH:\"" + path + "\"";
      resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
      System.out.println("number of hits = " + resultSet.length());
      NodeRef node = resultSet.getNodeRef(0);
      System.out.println("node id = " + node.getId());
      System.out.println(node.toString());

      String query2 = "@cm\\:destination:\"" + node.toString() +"\"";
      results2 = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query2);
      System.out.println("number of link hits = " + results2.length());
      NodeRef link = results2.getNodeRef(0);
      System.out.println("link is " + nodeService.getPath(link).toString());

    }
    catch(Exception e)
    {
      try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
      throw e;
    }        
    finally {
      if(resultSet != null) {
        resultSet.close();
      }
      if(results2 != null) {
        results2.close();
      }
    }

  }


  public void testDateConversion()
  {
    try {
      Date date = DateFormat.getDateInstance(DateFormat.SHORT).parse("02/23/2006");
      String dateStr = ISO8601DateFormat.format(date);
      System.out.println("converted date = " + dateStr);

      String test = "2005-09-16T17:01:03.456+01:00";
      // convert to a date
      date = ISO8601DateFormat.parse(test);
      // get the string form
      String strDate = ISO8601DateFormat.format(date);
      // convert back to a date from the converted string
      Date dateAfter = ISO8601DateFormat.parse(strDate);
      // make sure the date objects match, test this instead of the
      // string as the string form will be different in different
      // locales
      assertEquals(date, dateAfter);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
