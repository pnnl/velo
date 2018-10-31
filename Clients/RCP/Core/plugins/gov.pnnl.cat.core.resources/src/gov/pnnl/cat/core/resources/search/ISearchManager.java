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

import gov.pnnl.cat.core.resources.AccessDeniedException;
import gov.pnnl.cat.core.resources.ResourceException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 */
public interface ISearchManager {
  
  // Default Alfresco query syntax
  public static final String LANGUAGE_LUCENE = "lucene";
  
  // Note that XPATH queries are currently supported via Alfreso web services
  public static final String LANGUAGE_XPATH  = "xpath";

  
  public ICatQueryResult query(String query) throws ResourceException, AccessDeniedException;

  /**
   * Query repository.
   * @param query
   * @param includeThumbnails - should thumbnails be included for results?
   * @param sortByProp - what property should we sort results by.  Set null for no sorting
   * @param order - ascending or descending (null if not sorting)
   * @param maxItems - limit results batch to this size
   * @param pageNumber - will return next page of maxItems where
   *  results set includes nodes: (maxItems * pageNumber-1) through (maxItems * pageNumber)
   * @return ICatQueryResult
   */
  public ICatQueryResult query(String query, boolean includeThumbnails, String sortByProp, String order, Integer maxItems, Integer pageNumber);
  public ICatQueryResult query(String query, boolean includeThumbnails, String sortByProp, String order, Integer maxItems, Integer pageNumber, ArrayList<String> facetProperties);
  
  public HashMap<String, HashMap<String, Integer>> getFacetItems(String query, ArrayList<String> fieldsQNames);
}
