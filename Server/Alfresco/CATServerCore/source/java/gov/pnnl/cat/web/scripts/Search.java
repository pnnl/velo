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
 * 
 */
package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.model.QueryRequest;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.model.SearchResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.search.impl.lucene.LuceneResultSet;
import org.alfresco.repo.search.impl.lucene.PagingLuceneResultSet;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.Pair;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Search for up the given resources, including all properties and aspects for each 
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class Search extends AbstractCatWebScript {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    ObjectMapper mapper = new ObjectMapper();
    QueryRequest queryRequest = mapper.readValue(requestContent, QueryRequest.class);
    
    SearchResult searchResult = performSearch(queryRequest.getQuery(), 
        queryRequest.isIncludeThumbnails(), queryRequest.getSortByProp(), 
        queryRequest.getOrder(), queryRequest.getMaxItems(), queryRequest.getPageNumber(), queryRequest.getPropertyFacets());
    
    // write the results to the output stream
    // serialize children via json
    long begin = System.currentTimeMillis();
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    mapper.writeValue(res.getOutputStream(), searchResult);
    long end = System.currentTimeMillis();
    System.out.println("Time to serialize Resource objects to JSON: " + (end-begin) + " ms");
    return null;
  }

  /**
   * Method performSearch.
   * @param query String
   * @param includeThumbnails boolean
   * @param sortByProp String
   * @param order String
   * @param maxItems Integer
   * @return ResourceList
   * @throws Exception
   */
  public SearchResult performSearch(String query, boolean includeThumbnails, String sortByProp, String order, Integer maxItems, Integer pageNumber, ArrayList<String> facetRequests) throws Exception{
    SearchResultNodeRefs intermediateResults = performSearchForNodes(query, includeThumbnails, sortByProp, order, maxItems, pageNumber, facetRequests);

    ArrayList<Resource> nodes = new ArrayList<Resource>();
      
    long begin = System.currentTimeMillis();
    List<NodeRef> nodeRefs = intermediateResults.nodeRefs;
    for(NodeRef nodeRef : nodeRefs) {
      nodes.add(WebScriptUtils.getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService, includeThumbnails));
    }
    long end = System.currentTimeMillis();
    System.out.println("Time to convert results to Resource objects: " + (end-begin) + " ms");
      
    SearchResult searchResult = new SearchResult(maxItems, pageNumber, intermediateResults.getNumHits(), nodes, intermediateResults.getPropertyFacets());
    return searchResult;
  }
  
  
  public SearchResultNodeRefs performSearchForNodes(String query, boolean includeThumbnails, String sortByProp, String order, Integer maxItems, Integer pageNumber, ArrayList<String> facetRequests) throws Exception{
    System.out.println("search query  = " + query);

    HashMap<String, HashMap<String, Integer>> propertyFacets = new HashMap<String, HashMap<String, Integer>>();
    Long totalHits = null;
    List<NodeRef> nodeRefs = null;
    ResultSet results = null;
    try {
      SearchParameters sp = new SearchParameters();
      sp.addStore(CatConstants.SPACES_STORE);
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      sp.setQuery(query);

      if(maxItems != null){
        sp.setMaxItems(maxItems);
      }
      
      if(pageNumber != null) { // we are requesting a particular page num
        sp.setSkipCount(maxItems * (pageNumber - 1)); //once we add to clients a paging UI, pass in and use here which page to return
      }
      
      boolean ascending = (order == null || order.equals("ascending")) ? true : false;
      if(sortByProp != null) {
        //String sortByField = @\{http\://www.pnnl.gov/cii/model/1.0\}intendedExperimentalMethod
        sp.addSort(sortByProp, ascending);
      }
      
      //add facets to query if requested:
      if(facetRequests != null && facetRequests.size() > 0){
        for (String propertyQName : facetRequests) {
          addFieldFacet(propertyQName, sp);
        }
      }
      

      long begin = System.currentTimeMillis();
      results = searchService.query(sp); 
      long end = System.currentTimeMillis();
      System.out.println("Time to perform alfresco search: " + (end-begin) + " ms");
      
      //add facets to results returned if facets requested:
      if(facetRequests != null && facetRequests.size() > 0){
        for (String propertyQName : facetRequests) {
          HashMap<String, Integer> propertyFacet = convertFacetsToMap(results.getFieldFacet(convertPropertyToField(propertyQName)));
          propertyFacets.put(propertyQName, propertyFacet);
        }
      }
      
      // Return the total number of hits in the results:
      if(results instanceof SolrJSONResultSet) {
        totalHits = ((SolrJSONResultSet)results).getNumberFound();
      
      } else if (results instanceof PagingLuceneResultSet) {
        // TODO: will we be able to find out total hits?
      
      } else if (results instanceof LuceneResultSet) {
        // TODO: will we be able to find out total hits?
      }
      
      nodeRefs = results.getNodeRefs();
    } finally {
      if(results != null) {
        results.close();
      }
    }
    SearchResultNodeRefs searchResult = new SearchResultNodeRefs(maxItems, pageNumber, totalHits, null, propertyFacets);
    searchResult.nodeRefs = nodeRefs;
    return searchResult;
  }
  
  class SearchResultNodeRefs extends SearchResult{
    List<NodeRef> nodeRefs;
    
    public SearchResultNodeRefs(Integer batchSize, Integer pageNumber, Long numHits, ArrayList<Resource> results, HashMap<String, HashMap<String, Integer>> propertyFacets) {
      super(batchSize, pageNumber, numHits,  results,  propertyFacets);
    }
  }
  
  /**
   * Method getNode.
   * @param path String
   * @return Resource
   * @throws Exception
   */
  protected Resource getNode(String path) throws Exception {
    NodeRef nodeRef = NodeUtils.getNodeByName(path, nodeService);
    if (nodeRef == null) {
      logger.warn(path + " does not exist!");
      return null;
    }
    return WebScriptUtils.getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService);
  }
  
  private void addFieldFacet(String property, SearchParameters sp){
    //append the .__.u to the end of each field so that the facets returned are on the NOT tokenised values.  Otherwise for 
    //the microscopist name field, one record would hve the value "Zoe Guillen" but 2 facet items would be returned, "Zoe" and "Guillen"
    String field = convertPropertyToField(property);
    FieldFacet ff = new FieldFacet(field);
    sp.addFieldFacet(ff);
  }
  
  private String convertPropertyToField(String propertyQName){
    return  "@" + propertyQName + ".__.u";
  }
  
  private HashMap<String, Integer> convertFacetsToMap(List<Pair<String, Integer>> solrFacets){
    HashMap<String, Integer> uiFacets = new HashMap<String, Integer> (); 
    for (Pair<String,Integer> pair : solrFacets) {
      if(pair.getSecond().intValue() > 0){
        uiFacets.put(pair.getFirst(), pair.getSecond());
      }
    }
    return uiFacets;
  }

}
