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
package gov.pnnl.velo.ui.views.facetedsearch;

import gov.pnnl.cat.core.internal.resources.search.SearchManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;
import gov.pnnl.cat.core.resources.search.SearchContext;
import gov.pnnl.cat.search.CachingDateFormat;
import gov.pnnl.cat.search.advanced.query.AdvancedSearchQuery;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.alfresco.repo.search.impl.lucene.QueryParser;

/**
 */
public abstract class AbstractFacetedSearchQuery extends AdvancedSearchQuery{

  private FacetedSearchResultListener callback;

  private String querySessionID;

  private CmsPath location;
  

  private Date createdFromDate;
  private Date createdToDate;


  /**
   * Method setFacetedSearchResultListener.
   * @param callback FacetedSearchResultListener
   */
  public void setFacetedSearchResultListener(FacetedSearchResultListener callback){
    this.callback = callback;
  }

  public AbstractFacetedSearchQuery() {
  }

  // override these methods so that we can keep a handle on the search results - they need to be used again
  // in the other facets views:
  /**
   * Method executeSearch.
   * @return List<IResource>
   * @throws Exception
   */
  @Override
  public ICatQueryResult executeSearch() throws Exception {
    // impl copied from AbstractQuery
    String searchQuery = buildSearchQuery();
    long begin = System.currentTimeMillis();
    this.querySessionID = null;

    // For now, only bring back first 100 results
    // TODO: add paging
    ICatQueryResult results = ((SearchManager)ResourcesPlugin.getDefault().getSearchManager()).query(searchQuery, 
        false, /*VeloConstants.PROP_NAME*/ null, "ascending", 100, null, getFacetPropertiesRequested());
      
    long end = System.currentTimeMillis();
    System.out.println("Took " + (end - begin) + " ms to execute the query");

    // FACET SEARCH CHANGE BEGIN
    if(callback != null){
      callback.searchExecuted(results);
    }
    // FACET SEARCH CHANGE END
    
    return results;
  }
  
  
 
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#getLabel()
   */
  public String getLabel() {
    return "Faceted Search Query";
  }

  /**
   * Method searchValid.
   * @return boolean
   */
  protected boolean searchValid()
  {
//    String searchString = getSearchString();
//    boolean valid = (searchString != null) && (searchString.length() > 0);
//    valid = valid || metaDataDefined();
//    return valid;
    //we just need a location for this query to be valid:
    return true;//location != null && !location.toDisplayString().isEmpty();
  }
  
  /**
   * Method buildSearchQuery.
   * @return String
   * @throws Exception
   * @see gov.pnnl.cat.search.basic.query.IBasicSearchQuery#buildSearchQuery()
   */
  public String buildSearchQuery() throws Exception{
    StringBuilder query = new StringBuilder();
    query.append("-ASPECT:\"");
    query.append(VeloConstants.ASPECT_IGNORE);
    query.append("\"");
    if(location != null) {
      String loc = location.toPrefixString(true) + "//*";
      query.append(" AND PATH:\"").append(loc).append("\" ");
    }
    
    if(this.createdToDate != null && this.createdFromDate != null){
      SimpleDateFormat dateSearchFormat = CachingDateFormat.getDateFormat();
      String escapedDatePropName = SearchContext.escapeQualifiedName(VeloConstants.PROP_CREATED); //"\\{http\\://www.alfresco.org/model/content/1.0\\}created";
      query.append(" AND (");
        Date startDate = this.createdFromDate;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(createdToDate);
        cal.add(Calendar.DATE, 1);//roll the end date by 1 to make it inclusive (otherwise search results with the end date value won't be included)
        Date endDate = cal.getTime();
        query.append("@").append(escapedDatePropName)
        .append(":").append("[").append(QueryParser.escape(dateSearchFormat.format(startDate)))
        .append(" TO ").append(QueryParser.escape(dateSearchFormat.format(endDate))).append("]");
      query.append(") ");
    }
    
    return buildFacetedSearchQuery(query);
  }
  
  /**
   * Method buildFacetedSearchQuery.
   * @param query StringBuilder
   * @return String
   * @throws Exception
   */
  public abstract String buildFacetedSearchQuery(StringBuilder query) throws Exception;

  public abstract ArrayList<String> getFacetPropertiesRequested();
  
  protected void appendFacetSelectionQuery(List<String> selections, String escapedPropName, StringBuilder query){
    if(selections != null && selections.size() > 0){
      query.append(" AND (");
      for (String selection : selections) {
        //wrap in quotes?  IE for AND (@\{http\://www.pnnl.gov/cii/model/1.0\}microscopistName:Nigel Browning) 
        query.append("@").append(escapedPropName).append(":").append(selection);
        //if we're not on the last date element, add an 'OR'
        if(selections.indexOf(selection) != selections.size() -1 ){
          query.append(" OR ");
        }
      }
      query.append(") ");
      //but also return the dataset folder itself if it's got a tiltAngleMin higher than the selected min AND tiltAngleMax 
    }
  }

  private Collection<CmsPath> locations = new ArrayList<CmsPath>();
//reset the location to null for all location search
  public void resetLocation()
  {
    locations.clear();
  }
  
  //if it is not the first one,
  //add "|" and then input to the locations string
  /**
   * Method addLocation.
   * @param path CmsPath
   */
  public void addLocation(CmsPath path)
  {
    locations.add(path);
    location = path;
  }
  
  /**
   * Method setLocation.
   * @param path CmsPath
   */
  public void setLocation(CmsPath path)
  {
    location = path;
  }

 
  public Date getCreatedFromDate() {
    return createdFromDate;
  }

  public void setCreatedFromDate(Date createdFromDate) {
    this.createdFromDate = createdFromDate;
  }

  public Date getCreatedToDate() {
    return createdToDate;
  }

  public void setCreatedToDate(Date createdToDate) {
    this.createdToDate = createdToDate;
  }

}
