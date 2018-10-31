package gov.pnnl.velo.model;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchResult {
  
  private Integer batchSize;
  private Integer pageNumber;
  private Long numHits;
  private ArrayList<Resource> results;
  //map of propertyName To map of PropertyValue To Counts
  private HashMap<String, HashMap<String, Integer>> propertyFacets = new HashMap<String, HashMap<String, Integer>>();
  
  public SearchResult() {
    
  }
  
  public SearchResult(Integer batchSize, Integer pageNumber, Long numHits, ArrayList<Resource> results, HashMap<String, HashMap<String, Integer>> propertyFacets) {
    super();
    this.batchSize = batchSize;
    this.pageNumber = pageNumber;
    this.numHits = numHits;
    this.results = results;
    this.propertyFacets = propertyFacets;
  }

  public Integer getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(Integer batchSize) {
    this.batchSize = batchSize;
  }

  public Integer getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(Integer pageNumber) {
    this.pageNumber = pageNumber;
  }

  public Long getNumHits() {
    return numHits;
  }

  public void setNumHits(Long numHits) {
    this.numHits = numHits;
  }

  public ArrayList<Resource> getResults() {
    return results;
  }

  public void setResults(ArrayList<Resource> results) {
    this.results = results;
  }

  public HashMap<String, HashMap<String, Integer>> getPropertyFacets() {
    return propertyFacets;
  }

  public void setPropertyFacets(HashMap<String, HashMap<String, Integer>> propertyFacets) {
    this.propertyFacets = propertyFacets;
  }

  
}
