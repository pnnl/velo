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

import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.util.Constants;


/**
 * Practice Alfresco web service queries
 * @version $Revision: 1.0 $
 */
public class SearchTest {

  /** Admin user name and password used to connect to the repository */
  protected static final String USERNAME = "admin";
  protected static final String PASSWORD = "Koolcat1";
  
  /** The store used throughout the samples */
  protected static final Store STORE = new Store(Constants.WORKSPACE_STORE, "SpacesStore");

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
  }

//  /**
//   * Method executeSearch.
//   * @param queryString String
//   * @throws Exception
//   */
//  public static void executeSearch(String queryString) throws Exception {
//    
//    // Get a reference to the respository web service
//    RepositoryServiceSoapBindingStub repositoryService = AlfrescoWebServiceFactory.getRepositoryService();         
//    
//    // Create a query object, looking for all items with alfresco in the name of text
//    Query query = new Query(Constants.QUERY_LANG_LUCENE, queryString);
//    
//    // Execute the query
//    long start = System.currentTimeMillis();
//    QueryResult queryResult = repositoryService.query(STORE, query, false);
//    long end = System.currentTimeMillis();
//    
//    System.out.println("Time to execute query = " + (end - start));
//    
//    // Display the results
//    ResultSet resultSet = queryResult.getResultSet();
//    ResultSetRow[] rows = resultSet.getRows();
//    if (rows == null)
//    {
//        System.out.println("No query results found.");
//    }
//    else
//    {
//        System.out.println("Found " + rows.length + " results");
//    }
//  }
}

