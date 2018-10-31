package gov.pnnl.velo.webscripts;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.velo.webscripts.AbstractVeloWebScript;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.Pair;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FacetItemsSearch extends AbstractVeloWebScript {

  public static final String PARAM_QUERY = "query";  
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    String query = req.getParameter(PARAM_QUERY);
    
    System.out.println("search query  = " + query);
    
    ObjectMapper mapper = new ObjectMapper();
    ArrayList<String> fieldsQNames = null;

    fieldsQNames = mapper.readValue(requestContent, new TypeReference<ArrayList<String>>() {});
    
    HashMap<String, HashMap<String, Integer>> facets = new HashMap<String, HashMap<String, Integer>>();
    ResultSet results = null;
    try {
      SearchParameters sp = new SearchParameters();
      sp.addStore(CatConstants.SPACES_STORE);
      sp.setQuery(query);
      sp.setMaxItems(0);
      sp.setLanguage(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO);

      ArrayList<String> fieldsSolrName = new ArrayList<String>();
      
      for (String fieldQName : fieldsQNames) {
        fieldsSolrName.add(addFieldFacet(fieldQName, sp));
      }

      results = searchService.query(sp);
      
      int index = 0;
      for (String fieldSolrName : fieldsSolrName) {
        HashMap<String, Integer> facetCounts = new HashMap<String, Integer>();
        updateUiFacets(results.getFieldFacet(fieldSolrName), facetCounts);
        facets.put(fieldsQNames.get(index), facetCounts);
        index++;
      }
      
    } finally {
      if(results != null) {
        results.close();
      }
    }
    
    //return a map of maps
    HashMap<String, HashMap<String, Integer>> propToPropNameAndCounts = new HashMap<String, HashMap<String, Integer>>();
    
    
    for (String fieldQName : facets.keySet()) {
      propToPropNameAndCounts.put(fieldQName, facets.get(fieldQName));
    }      
    
    // write the results to the output stream
    // serialize children via json
    
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    mapper.writeValue(res.getOutputStream(), propToPropNameAndCounts);
    
    return null;
  }
  
  private String addFieldFacet(String property, SearchParameters sp){
    //append the .__.u to the end of each field so that the facets returned are on the NOT tokenised values.  Otherwise for 
    //the microscopist name field, one record would hve the value "Zoe Guillen" but 2 facet items would be returned, "Zoe" and "Guillen"
    String field = convertPropertyToFieldFacet(property);
    FieldFacet ff = new FieldFacet(field);
    sp.addFieldFacet(ff);
    return field;
  }
  
  private String convertPropertyToFieldFacet(String propertyQName){
    return  "@" + propertyQName + ".__.u";
  }
  
  private void updateUiFacets(List<Pair<String, Integer>> solrFacets, HashMap<String, Integer> uiFacets){
    for (Pair<String,Integer> pair : solrFacets) {
      if(pair.getSecond().intValue() > 0){
        uiFacets.put(pair.getFirst(), pair.getSecond());
      }
    }
  }
  
  
}
