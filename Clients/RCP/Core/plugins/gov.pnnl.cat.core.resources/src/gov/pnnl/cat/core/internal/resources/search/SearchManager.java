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
package gov.pnnl.cat.core.internal.resources.search;

import gov.pnnl.cat.core.internal.resources.IResourceService;
import gov.pnnl.cat.core.internal.resources.ResourceManager;
import gov.pnnl.cat.core.internal.resources.cache.Cache;
import gov.pnnl.cat.core.resources.AccessDeniedException;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * TODO: this class needs to go away since it just calls resource service
 */
public class SearchManager implements ISearchManager {
  private ResourceManager resourceManager;
  private IResourceService resourceService;

  private static Logger logger = CatLogger.getLogger(SearchManager.class);

  public static final int BATCH_SIZE = 50;

  private int matchCount = 0;

  public SearchManager() {
  }

  public void init() {
  }

  /**
   * Method setResourceManager.
   * @param mgr ResourceManager
   */
  public void setResourceManager(ResourceManager mgr) {
    this.resourceManager = mgr;
  }

  /**
   * @param resourceService the resourceService to set
   */
  public void setResourceService(IResourceService resourceService) {
    this.resourceService = resourceService;
  }
  
  @Override
  public HashMap<String, HashMap<String, Integer>> getFacetItems(String query, ArrayList<String> fieldsQNames){
    return resourceService.getFacetItems(query, fieldsQNames);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.search.ISearchManager#query(java.lang.String)
   */
  @Override
  public ICatQueryResult query(String query) throws ResourceException, AccessDeniedException {
    return query(query, false, null, null, null, null);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.search.ISearchManager#query(java.lang.String, boolean, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
   */
  @Override
  public ICatQueryResult query(String query, boolean includeThumbnails, String sortByProp, 
      String order, Integer maxItems, Integer pageNumber) {
    return query(query, includeThumbnails, sortByProp, order, maxItems, pageNumber, null);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.search.ISearchManager#query(java.lang.String, boolean, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
   */
  @Override
  public ICatQueryResult query(String query, boolean includeThumbnails, String sortByProp, 
      String order, Integer maxItems, Integer pageNumber, ArrayList<String> facetProperties) {
    ICatQueryResult searchResult = resourceService.search(query, includeThumbnails, sortByProp, order, maxItems, pageNumber, includeThumbnails, facetProperties);
    
    List<IResource> handles = searchResult.getHandles();
    Cache cache = resourceManager.getCache(); // TODO: don't force query results to be in the cache
    //need to keep track of how many resources we ignore (like type forum and files from the dictionary) so our search result view's label won't say something like
    // showing 7 (out of 10) matches.  Instead it should say showing 7 (out of 7) matches
    int resourcesNotAddedToCache = 0; 
    for(Resource r : searchResult.getResources()) {
      IResource handle = cache.addResource(r);
      if(handle != null) {
        handles.add(handle);
      }else {
        resourcesNotAddedToCache++;
      }
    }
//  when using lucene instead of solr, some fields can be null like: {"batchSize":null,"pageNumber":null,"numHits":null,"results":[],"propertyFacets":{}}
    if(searchResult.getTotalHits() != null){
      ((CatQueryResult)searchResult).setTotalHits(searchResult.getTotalHits() - resourcesNotAddedToCache);
    }
    return searchResult;
  }

}
