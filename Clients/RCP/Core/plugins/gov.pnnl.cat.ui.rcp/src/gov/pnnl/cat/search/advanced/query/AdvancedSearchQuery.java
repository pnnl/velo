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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author d3m602
 * 
 * @version $Revision: 1.0 $
 */
public class AdvancedSearchQuery extends BasicSearchQuery {

  private String allwords;
  private String exactphrase;
  private String oneofwords;
  private String withoutwords;

  boolean createdDateChecked = false;

  boolean modifiedDateChecked;

  private String strCreatedDate;
  private String strCreatedDateTo;

  private String strModifiedDate;
  private String strModifiedDateTo;

  private String author;
  private String title;
  private String description;
  private String mimeType;

  private Collection<CmsPath> locations = new ArrayList<CmsPath>();
  
  private String mode; // search mode
  private static final String MODE_ALL = "all";
  private static final String MODE_FILE_NAME = "filename";
  private static final String MODE_CONTENT = "content";
  private static final String MODE_FOLDER = "folder";
  private static Logger logger = CatLogger.getLogger(AdvancedSearchQuery.class);
  
  private ISearchManager searchMgr;
  private AdvancedSearchExtensions ext = null;

  public AdvancedSearchQuery() {
    this(null);
  }
  
  /**
   * Constructor for AdvancedSearchQuery.
   * @param ext AdvancedSearchExtensions
   */
  public AdvancedSearchQuery(AdvancedSearchExtensions ext) {
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

  public void unSetCreatedDate() {
    createdDateChecked = false;
  }

  /**
   * Method setCreatedDate.
   * @param from String
   * @param to String
   */
  public void setCreatedDate(String from, String to) {

    createdDateChecked = true;
    strCreatedDate = from;
    strCreatedDateTo = to;
    logger.debug("setCreatedDate:" + strCreatedDate + "-" + strCreatedDateTo);
  }

  /**
   * Method setModifiedDate.
   * @param from String
   * @param to String
   */
  public void setModifiedDate(String from, String to) {
    modifiedDateChecked = true;
    strModifiedDate = from;
    strModifiedDateTo = to;
    logger.debug("setModifiedDate:" + strModifiedDate + "-" + strModifiedDateTo);
  }

  public void unSetModifiedDate() {
    modifiedDateChecked = false;
  }

  /**
   * Method setAuthor.
   * @param a String
   */
  public void setAuthor(String a) {
    author = a;
  }

  public void unSetAuthor() {
    author = null;
  }

  /**
   * Method setDescription.
   * @param d String
   */
  public void setDescription(String d) {
    description = d;
  }

  public void unSettDiscription() {
    description = null;
  }

  /**
   * Method setTitle.
   * @param t String
   */
  public void setTitle(String t) {
    title = t;
  }

  public void unSetTitle() {
    title = null;
  }

  /**
   * Method setMode.
   * @param m String
   */
  public void setMode(String m) {
    mode = m;
  }
  
  /**
   * Method setMimeType.
   * @param t String
   */
  public void setMimeType(String t)
  {
    mimeType = null;
    if(t != null && t.length() > 0 && !(t.equals("All Formats")))
    {
      mimeType = t;
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
    String searchString = getSearchString();
    logger.debug("AdvancedSearchQuery searhString:" + searchString);

    SearchContext searchContext = new SearchContext();
    
    searchContext.setText(searchString);

    if (createdDateChecked == true) {
      logger.debug("createdDateChecked:" + strCreatedDate + "-" + strCreatedDateTo);

      final String DATE_PATTERN = "MM/dd/yyyy"; // "yyyy-MM-dd";
      final SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);

      SimpleDateFormat ddf = CachingDateFormat.getDateFormat();
      // SimpleDateFormat df = CachingDateFormat.getDateFormat();
      strCreatedDate = ddf.format(df.parse(strCreatedDate));
      // strCreatedDateTo = df.format(new Date());
      strCreatedDateTo = ddf.format(df.parse(strCreatedDateTo));
      logger.debug("Date=" + strCreatedDate + " TO " + strCreatedDateTo);
      searchContext.addRangeQuery(VeloConstants.PROP_CREATED, strCreatedDate, strCreatedDateTo, true);
      logger.debug("ContentModel.PROP_CREATED:" + VeloConstants.PROP_CREATED);
    }

    if (modifiedDateChecked == true) {
      logger.debug("modifiedDateChecked:" + strModifiedDate + "-" + strModifiedDateTo);

      // TODO: use only one DATE_PATTERN
      final String DATE_PATTERN = "MM/dd/yyyy"; // "yyyy-MM-dd";
      final SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);
      SimpleDateFormat ddf = CachingDateFormat.getDateFormat();

      strModifiedDate = ddf.format(df.parse(strModifiedDate));
      strModifiedDateTo = ddf.format(df.parse(strModifiedDateTo));
      logger.debug("Date=" + strModifiedDate + " TO " + strModifiedDateTo);
      searchContext.addRangeQuery(VeloConstants.PROP_MODIFIED, strModifiedDate, strModifiedDateTo, true);
      logger.debug("ContentModel.PROP_MODIFIED:" + VeloConstants.PROP_MODIFIED);
    }

    if (author != null && author.length() != 0) {
      searchContext.addAttributeQuery(VeloConstants.PROP_AUTHOR, trimDoubleQuotes(author));
    }

    if (title != null && title.length() != 0) {
      searchContext.addAttributeQuery(VeloConstants.PROP_TITLE, trimDoubleQuotes(title));
    }

    if (description != null && description.length() != 0) {
      searchContext.addAttributeQuery(VeloConstants.PROP_DESCRIPTION, trimDoubleQuotes(description));
    }

    //set Mime type if available
    if(mimeType != null && mimeType.length() > 0)
    {
      searchContext.setMimeType(mimeType);
    }
    
    if(!locations.isEmpty())
    {
      searchContext.setLocations(locations);

      // We are taking this out because we are goign to get rid of taxonomies as
      // they cause performance problems
//    // if the user has specified locations to search in, we need to take
//    // additional steps to handle taxonomies located under the directories
//    // the user has chosen.
//      IResourceManager mgr = ResourcesPlugin.getResourceManager();
//
//      // a list that will hold all of the taxonomies that have been selected, or
//      // that are located under the non-taxonomy folders that have been selected
//      List <CmsPath> taxonomyFolders = new ArrayList<CmsPath>();
//
//      // a collection of the non-taxonomy folders that were selected
//      Collection<CmsPath> regularFolders = new ArrayList<CmsPath>();
//
//      // separate the folders that are in a taxonomy from the folders that are not
//      for (CmsPath path : locations) {
//        IResource resource = mgr.getResource(path);
//
//        if (resource.isType(IResource.TAXONOMY_ROOT) ||
//            resource.isType(IResource.TAXONOMY_FOLDER)) {
//          taxonomyFolders.add(path);
//        } else {
//          regularFolders.add(path);
//        }
//      }
//
//      // execute a search for all of the taxonomyRoots under the selected locations.
//      if (!regularFolders.isEmpty()) {
//        // search resource underneath that is a
//        SearchContext context = new SearchContext();
//        context.setLocations(regularFolders);
//        //context.setMode(SearchContext.SEARCH_SPACE_NAMES);
//        context.setAspectToInclude(VeloConstants.ASPECT_TAXONOMY_ROOT);
//        context.setAspectToExclude(VeloConstants.ASPECT_FAVORITES_ROOT);
//        String taxonomyQuery = context.buildQuery(3);
//        
//        logger.debug("taxonomy query:" + taxonomyQuery);
//         
//        ICatQueryResult queryResults = searchMgr.query(taxonomyQuery);
//        List<IResource> taxonomyResults = queryResults.getHandles();
//        
//        logger.debug("taxonomy search result size:" + taxonomyResults.size());
//
//        // add each taxonomy into the list of taxonomies to search
//        for(IResource res : taxonomyResults)
//        {
//          logger.debug("taxonomy search result:" + res.getPath().toString());
//          taxonomyFolders.add(res.getPath());
//        }
//      }
//      
//      String [] categories = new String[taxonomyFolders.size()];
//
//      // determine that category for each taxonomy
//      for(int i=0; i<taxonomyFolders.size(); i++)
//      {
//        CmsPath path = taxonomyFolders.get(i);
//        IResource taxonomy = mgr.getResource(path);
//        String categoryName = taxonomy.getName() + "_" + taxonomy.getPropertyAsString(VeloConstants.PROP_UUID);
//        logger.debug("Path: " + path);
//        logger.debug("\t" + categoryName);
//        categories[i] = categoryName;
//      }
//
//      // add the categories to our search criteria
//      searchContext.setCategories(categories);
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

  /**
   * Advanced Search can be valid either:
   *  1) searchString is no empty (the Look For part:
   *   all of the words, the exact phrase, at least one of the word)
   *  2) metaData specified
   * @return boolean
   */
  protected boolean searchValid()
  {
    String searchString = getSearchString();
    boolean valid = (searchString != null) && (searchString.length() > 0);
    valid = valid || metaDataDefined();
    
    //now check with extensions:
    boolean noTextRequired = false;
    if(ext != null) {
      List<AdvancedSearchOptions> extensions = ext.getAdvOptionsExtensions();
      for (AdvancedSearchOptions advancedSearchOptions : extensions) {
        if(!advancedSearchOptions.searchTextRequired()){
          noTextRequired = true;
        }
      }
    }
    return valid || noTextRequired;
  }

  //TODO: print out the searchString and metaData
  /**
   * Method toString.
   * @return String
   * @see gov.pnnl.cat.search.basic.query.IBasicSearchQuery#toString()
   */
  public String toString() {
    return "searchString: " + mSearchString;
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

  
  /** True if any of the following metadata is defined:
   * File format
   * Title
   * Author
   * Description
   * Modified Date
   * Created Date 
  
   * @return boolean
   */

  public boolean metaDataDefined()
  {
    if( (mimeType != null && mimeType.length() > 0)
        || (title != null && title.length() > 0)
        || (author != null && author.length() > 0)
        || (description != null && description.length() > 0)
        || modifiedDateChecked
        || createdDateChecked)
    {
      return true;
    }
    else
      return false;
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

