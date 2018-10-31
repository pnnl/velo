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
package gov.pnnl.cat.search.advanced.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.core.resources.search.SearchContext;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.search.CachingDateFormat;
import gov.pnnl.cat.search.advanced.AdvancedSearchExtensions;
import gov.pnnl.cat.search.advanced.AdvancedSearchOptions;
import gov.pnnl.cat.search.basic.query.BasicSearchQuery;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

/**
 * @author d3m602
 * 
 * @version $Revision: 1.0 $
 */
public class StandaloneSearchQuery extends BasicSearchQuery {

  private String allwords;
  private String exactphrase;
  private String oneofwords;
  private String withoutwords;

  private Collection<CmsPath> locations = new ArrayList<CmsPath>();
  public static final String CONTENT_TYPE_FILE = "file";
  public static final String CONTENT_TYPE_FOLDER = "folder";
  
  private static Logger logger = CatLogger.getLogger(StandaloneSearchQuery.class);
  
  private ISearchManager searchMgr;
  private AdvancedSearchExtensions ext = null;
  private SearchContext searchContext = new SearchContext();
  public StandaloneSearchQuery() {
    this(null);
  }
  
  /**
   * Constructor for AdvancedSearchQuery.
   * @param ext AdvancedSearchExtensions
   */
  public StandaloneSearchQuery(AdvancedSearchExtensions ext) {
    super();
    this.searchMgr = ResourcesPlugin.getSearchManager();
    this.ext = ext;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#getLabel()
   */
  public String getLabel() {
    return "Advanced Search Query";
  }


  /**
   * Method setMode.
   * @param m String
   */
  public void setContentType(String type) {
    if(type.equals(CONTENT_TYPE_FILE)){
      searchContext.addTypeToInclude(VeloConstants.PROP_CONTENT);
      
    }else if(type.equals(CONTENT_TYPE_FOLDER)){
      searchContext.addTypeToInclude(VeloConstants.PROP_FOLDER);
      
    }else{
      throw new IllegalArgumentException(type + "is a invalid content type. Valid values are: "+ CONTENT_TYPE_FILE + "," + CONTENT_TYPE_FOLDER );
    }
  }
  
  /**
   * Method setAllwords.
   * @param aw String
   */
  public void setAllwords(String aw) {
    allwords = aw;
  }

  public void unSetAllwords() {
    allwords = null;
  }

  /**
   * Method setExactphrase.
   * @param e String
   */
  public void setExactphrase(String e) {
    exactphrase = e;
  }

  public void unSetExactphrase() {
    exactphrase = null;
  }

  /**
   * Method setWithoutwords.
   * @param ww String
   */
  public void setWithoutwords(String ww) {
    withoutwords = ww;
  }

  public void unSetWithoutwords() {
    exactphrase = null;
  }

  /**
   * Method setOneofwords.
   * @param ow String
   */
  public void setOneofwords(String ow) {
    oneofwords = ow;
  }

  public void unSetOneofwords() {
    oneofwords = null;
  }

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
  }

  
  /**
   * This method offers specific AdvancedSearch syntax:
   *   Text and metadata
   * Note: BasicSearchQuery manages only Text part.
   * @return String
   * @throws Exception
   * @see gov.pnnl.cat.search.basic.query.IBasicSearchQuery#buildSearchQuery()
   */
  public String buildSearchQuery() throws Exception
  {    
    String query;
    
    searchContext.setText(getSearchString());
    
    if(!locations.isEmpty())
    {
      searchContext.setLocations(locations);
    }

    if (ext != null) {
      for (AdvancedSearchOptions option : ext.getAdvOptionsExtensions()) {
        option.setSearchParam(searchContext);
      }
    }
    
    // we always want to exclude CAT ignore & hidden rendition aspects
    searchContext.setAspectToExclude(VeloConstants.ASPECT_IGNORE);
    searchContext.setAspectToExclude(VeloConstants.ASPECT_HIDDEN_RENDITION);

    // we always want to exclude thumbnail types
    searchContext.addTypeToExclude(VeloConstants.TYPE_THUMBNAIL);
    
    query = searchContext.buildQuery(2);
    logger.debug("AdvancedSearchQuery:" + query);
    
    return query;
    
  }
  
  public void addAttributeQuery(String property, String value){
    if (value != null && value.length() != 0) {
      searchContext.addAttributeQuery(property, trimDoubleQuotes(value));
    }
  }

  public void addDateRange(String property, Date dateFrom, Date dateTo) throws ParseException {

    SimpleDateFormat ddf = CachingDateFormat.getDateFormat();
    // SimpleDateFormat df = CachingDateFormat.getDateFormat();
    String strFromDate = ddf.format(dateFrom);
    // strCreatedDateTo = df.format(new Date());
    String strToDate = ddf.format(dateTo);
    
    logger.debug("Date=" + strFromDate + " TO " + strToDate);
    searchContext.addRangeQuery(property, strFromDate, strToDate, true);
    logger.debug("Property:" + property);
  }
  
  public void addMimetype(String mimetype){
    if(mimetype!=null)
     searchContext.setMimeType(mimetype);
  }
  
  public void addIncludeAspect(String aspect){
    searchContext.setAspectToInclude(aspect);
  }

  
  public void addExcludeAspect(String aspect){
    searchContext.setAspectToExclude(aspect);
  }
  
  /**
   * the logic used to belong to AdvancedSearch::getSearchQuery()
   * @return String
   * @see gov.pnnl.cat.search.basic.query.IBasicSearchQuery#getSearchString()
   */
  public String getSearchString() {
    return super.getSearchString();
    
//    String searchStr = "";
//    //put exact phrase at the very beginning
//    if(exactphrase != null && exactphrase.length() > 0)
//    {
//      searchStr += "\"" + exactphrase + "\"";
//    }
//
//    String allwordsSearchStr = "";
//    if(allwords != null && allwords.length() > 0)
//    {
//      String [] words = allwords.split(" ");
//      if(words.length > 0)
//      {
//        for(int i=0; i<words.length; i++)
//        {
//          allwordsSearchStr += "+" + words[i];
//        }
//      }
//    }
//
//    String withoutwordsSearchStr = "";
//    if(withoutwords != null && withoutwords.length() > 0)
//    {
//      String [] words = withoutwords.split(" ");
//      if(words.length > 0)
//      {
//        for(int i=0; i<words.length; i++)
//        {
//          withoutwordsSearchStr += "+-" + words[i];
//        }
//      }
//    }
//
//    searchStr += allwordsSearchStr + withoutwordsSearchStr; //need to add other string
//    
//    String oneofwordsSearchStr = "";
//    if(oneofwords != null && oneofwords.length() > 0)
//    {
//      String [] words = oneofwords.split(" ");
//      if(words.length > 0)
//      {
//        oneofwordsSearchStr = "(";
//        for(int i=0; i<words.length; i++)
//        {
//          oneofwordsSearchStr += words[i] + " ";
//        }
//        oneofwordsSearchStr = oneofwordsSearchStr.trim();
//        oneofwordsSearchStr += ")";
//      }
//    }
//    if(oneofwordsSearchStr.length() > 0)
//    {
//      searchStr += "+" + oneofwordsSearchStr;
//    }
//
//    return searchStr;
  }

  
  /**
   * Method trimDoubleQuotes.
   * @param searchString String
   * @return String
   */
  private String trimDoubleQuotes(String searchString)
  {
    String modifiedSearchString = searchString;
    if(modifiedSearchString.startsWith("\""))
    {
      modifiedSearchString = modifiedSearchString.substring(1);
    }
    if(modifiedSearchString.endsWith("\""))
    {
      modifiedSearchString = modifiedSearchString.substring(0, modifiedSearchString.length()-1);
    }
    return modifiedSearchString;
  }
}

