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
package gov.pnnl.cat.core.resources.search;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.velo.model.Resource;

import java.util.HashMap;
import java.util.List;

/**
 */
public interface ICatQueryResult {

  /**
   * Method getResources.
   * @return List<Resource>
   */
  public List<Resource> getResources();
  
  /**
   * The hits, as IResource objects.
  
   * @return List<IResource>
   */
  public List<IResource> getHandles();

  /**
   * The Alfresco query session ID.  If this is not null, then that means more
   * query results are available on the server.  Call SearchManager.fetchMore() to
   * retrieve them.
  
   * @return String
   */
  // TODO: not sure if this applies as we are now doing the paging via SOLR - could be deprecated
  public String getQuerySessionID();
  
  public Long getTotalHits();
  
  public HashMap<String, HashMap<String, Integer>> getPropertyFacets();

}
