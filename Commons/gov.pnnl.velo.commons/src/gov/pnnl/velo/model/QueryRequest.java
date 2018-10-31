package gov.pnnl.velo.model;

import java.util.ArrayList;

public class QueryRequest {
  private String query;
  private boolean includeThumbnails = false;
  private String sortByProp;
  private String order;
  private Integer maxItems;
  private Integer pageNumber;
  private ArrayList<String> propertyFacets;
  
  public QueryRequest() {
    
  }
  
  public QueryRequest(String query, boolean includeThumbnails, String sortByProp, String order, Integer maxItems, Integer pageNumber, ArrayList<String> propertyFacets) {
    super();
    this.query = query;
    this.includeThumbnails = includeThumbnails;
    this.sortByProp = sortByProp;
    this.order = order;
    this.maxItems = maxItems;
    this.pageNumber = pageNumber;
    this.propertyFacets = propertyFacets;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public boolean isIncludeThumbnails() {
    return includeThumbnails;
  }

  public void setIncludeThumbnails(boolean includeThumbnails) {
    this.includeThumbnails = includeThumbnails;
  }

  public String getSortByProp() {
    return sortByProp;
  }

  public void setSortByProp(String sortByProp) {
    this.sortByProp = sortByProp;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public Integer getMaxItems() {
    return maxItems;
  }

  public void setMaxItems(Integer maxItems) {
    this.maxItems = maxItems;
  }

  public Integer getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(Integer pageNumber) {
    this.pageNumber = pageNumber;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  public ArrayList<String> getPropertyFacets() {
    return propertyFacets;
  }

  public void setPropertyFacets(ArrayList<String> propertyFacets) {
    this.propertyFacets = propertyFacets;
  }

}
