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

import gov.pnnl.cat.core.resources.search.ICluster;
import gov.pnnl.cat.logging.CatLogger;

import java.util.List;

import org.apache.log4j.Logger;

/**
 */
public class SearchManagerTest extends CatTest {
  protected static Logger logger = CatLogger.getLogger(SearchManagerTest.class);
  private int level;
  
//  /**
//   * Method testClusteredSearch.
//   * @throws ServerException
//   * @throws ResourceException
//   */
//  public void testClusteredSearch() throws ServerException, ResourceException{
//
//    StringBuilder query = new StringBuilder();
//    query.append("(PATH:\"/app:company_home//*\" AND -PATH:\"/app:company_home/app:*//*\") AND ");
//    query.append("( "); //beginning of (Text OR Author OR ...) 
//    query.append("TEXT:"+"exact");
//    query.append(") AND (TYPE:\""); //end of (Text OR Title OR ...)
//    query.append(Constants.TYPE_FOLDER);
//    query.append("\" OR TYPE:\"");
//    query.append(Constants.TYPE_CONTENT);
//    query.append("\" OR TYPE:\"");
//    query.append(VeloConstants.TYPE_LINKED_FILE);
//    query.append("\")");
//
//    ICluster rootCluster = this.searchMgr.clusteredQuery(ISearchManager.LANGUAGE_LUCENE, query.toString());
//    recurseTree(rootCluster);
//    List<IResource> results = rootCluster.getWholeTreeResourceMatches();
//    for(IResource result : results)
//    {
//      System.out.println("clustered search result:" + result.getPath());
//    }
//    this.level = 0;
//  }

  /**
   * Method recurseTree.
   * @param rootcluster ICluster
   */
  private void recurseTree(ICluster rootcluster){
    List<ICluster> subs = rootcluster.getSubclusters();
    for (ICluster cluster : subs) {
      printClusterInfo(cluster);
      level++;
      recurseTree(cluster);
      level--;
    }
  }
  
  /**
   * Method printClusterInfo.
   * @param cluster ICluster
   */
  private void printClusterInfo(ICluster cluster){
    String pad = "";
    for(int i = 0; i < this.level; i++)
      pad += "   ";
    System.out.println(pad + "cluster: " + cluster);
    System.out.println(pad + "matches: " + cluster.getReourceMatches().size());
    System.out.println(pad + "parent: " + cluster.getParent());
  }
  
}
