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
package gov.pnnl.cat.search.basic.query;

import gov.pnnl.cat.core.resources.search.SearchContext;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.search.basic.results.BasicSearchResult;
import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchMessages;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.alfresco.webservice.util.Constants;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * @author d3l028
 * 
 * @version $Revision: 1.0 $
 */
public class BasicSearchQuery extends AbstractQuery implements IBasicSearchQuery {

  //protected AbstractTextSearchResult mResult;

  protected StringBuffer mSearchString = new StringBuffer();
  private static Logger logger = CatLogger.getLogger(BasicSearchQuery.class);
 
  /**
   * giving empty constructor for web services
   * 
   */
  public BasicSearchQuery() {
  }

  /**
  
   * @param searchString
   */
  public BasicSearchQuery(String searchString) {
    
    mSearchString = new StringBuffer(searchString);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
   */
  /**
   * Method createSearchResult.
   * @return ISearchResult
   */
  public ISearchResult createSearchResult() {
      return new BasicSearchResult(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus run(IProgressMonitor monitor)throws OperationCanceledException {
    
    // TODO: Maybe update monitor as search progresses.  I don't think we can do this because
    // we don't get progress info from the JCR repository search
    // TODO: try to figure out a way to cancel the jcr search if user presses cancel
    try {      
        
      performSearch(getSearchString(), monitor);
      String message = SearchMessages.TextSearchEngine_statusMessage;
      return new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, message, null);
      
    } catch (Exception e) {
      CatSearchPlugin.log( new Status(IStatus.ERROR, CatSearchPlugin.getID(), 1, "Error during search.", e));   
      ToolErrorHandler.handleError("Error During Search", e, true);
      return Status.CANCEL_STATUS;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#getLabel()
   */
  public String getLabel() {
    return "Basic Search Query";// ???is this right???
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#canRerun()
   */
  public boolean canRerun() {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
   */
  public boolean canRunInBackground() {
    // TODO Auto-generated method stub
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cue.search.text.query.ITextSearchQuery#getSearchString()
   */
  public String getSearchString() {
    return mSearchString.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cue.search.text.query.ITextSearchQuery#setSearchString(java.lang.String)
   */
  public void setSearchString(String searchString) {
    mSearchString = new StringBuffer(searchString);

  }

  /*
   * added only for debugging purposes (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "searchString: " + mSearchString
        + ", result: {" + this.searchResult.toString() + "}.";
  }


  


  /**
   * Method searchValid.
   * @return boolean
   */
  protected boolean searchValid()
  {
    String searchString = getSearchString();
    boolean valid = (searchString != null) && (searchString.length() > 0);
    return valid;
  }
  
  /**
   * Method parseSearchString2.
   * @param searchStr String
   * @param andStrings ArrayList
   * @param notStrings ArrayList
   * @param orStrings ArrayList
   */
  private void parseSearchString2(String searchStr, ArrayList andStrings, ArrayList notStrings, ArrayList orStrings)
  {
    String theSearchStr = searchStr.trim();

    //first find if there is any + in the search string
    theSearchStr = getStrings(theSearchStr, andStrings, "+");
    logger.debug("searchString after calling getStrings(+):" + theSearchStr);
    logger.debug("andStrings size:" + andStrings.size());
    for(Object dummy : andStrings)
    {
      String dummyStr = (String)dummy;
      logger.debug("and string:" + dummy);
    }

    if(theSearchStr.length() > 0)
    {
      theSearchStr = getStrings(theSearchStr, notStrings, "-");
      logger.debug("searchString after calling getStrings(-):" + theSearchStr);
      logger.debug("notStrings size:" + notStrings.size());
      for(Object dummy : notStrings)
      {
        String dummyStr = (String)dummy;
        logger.debug("not string:" + dummy);
      }
    }

    if(theSearchStr.length() > 0)
    {
      String [] OrStrings = parseSearchString(theSearchStr);
      for (String orString : OrStrings)
      {
        orStrings.add(orString);
      }
    }
    for(Object dummy : orStrings)
    {
      String dummyStr = (String)dummy;
      logger.debug("or string:" + dummy);
    }
    
  }
  
  /**
   * throw away single quote?
   * pair the first and second double quote, 3 and 4, ..., throw away the last unmatched double quote?
   * 
   * @param searchStr
  
   * @return String[]
   */
  private String [] parseSearchString(String searchStr)
  {
    ArrayList<String> strs = new ArrayList<String>();
    searchStr = searchStr.trim();
    
    StringTokenizer t = new StringTokenizer(searchStr, "\"", true);
    StringTokenizer ts; //for space
    int tokenCount = t.countTokens();
    
    String term = "";
    
    //if there is no double quote
    if(tokenCount == 1)
    {
      ts = new StringTokenizer(searchStr, " ");
      int tokenCountSpace = ts.countTokens();
      for (int i=0; i<tokenCountSpace; i++)
      {
        term = ts.nextToken().trim();
        if(term.length() > 0)
        {
          strs.add(term);
        }
      }
      return strs.toArray(new String[strs.size()]);
    }

    int quoteCount = 0;
    for (int i=0; i<tokenCount; i++)
    {
      term = t.nextToken().trim();
      if(term.equals("\""))
      {
        quoteCount++;
        continue;
      }
      
      if(quoteCount%2 == 1) //inside the "", take the whole string, and wrap it with ""
      {
        if(term.length() > 0)
        {
          term = "\"" + term + "\"";
          strs.add(term);
        }
      }
      else //outside of double quote, would be space separated words
      {
        term = term.trim();
        if(term.length() > 0)
        {
          if(term.indexOf(" ") > 0)
          {
            ts = new StringTokenizer(term, " ");
            int tokenCountSpace = ts.countTokens();
            for (int j=0; j<tokenCountSpace; j++)
            {
              String term2 = ts.nextToken().trim();
              strs.add(term2);
            }
          }
          else
          {
            strs.add(term);
          }
        }
      }
    }
    String [] result = strs.toArray(new String[strs.size()]);
    return result;
  }

//  /**
//   * 
//   * @param parsedStrings
//   * @param prop
//  
//   * @return String
//   */
//  private String buildPropertyQueryOld(String [] parsedStrings, String prop) {
//    StringBuilder query = new StringBuilder();
//    query.append("( ");
//    int elementIndex = 0;
//    for(String element : parsedStrings)
//    {
//      if(elementIndex > 0)
//      {
//        query.append(" OR ");
//      }
//      query.append(prop);
//      query.append(":");
//      query.append(element);
//      elementIndex++;
//    }
//    query.append(" )");
//
//    return query.toString();
//    
//  }

  /**
   * This function takes care of + and - signs
   * @param parsedStrings
   * @param prop
  
   * @return String
   */
  private String buildPropertyQueryOld2(String [] parsedStrings, String prop) {
    StringBuilder query = new StringBuilder();
    query.append("( ");
    int elementIndex = 0;

    StringBuilder andQuery = new StringBuilder();
    int andIndex = 0;
    StringBuilder notQuery = new StringBuilder();
    
    StringBuilder orQuery = new StringBuilder();
    
    for(String element : parsedStrings)
    {
      char firstSign = element.charAt(0);
      boolean opsAND = (firstSign == '+');
      boolean opsNOT = (firstSign == '-');
      
      if(opsAND && element.length() > 1)
      {
        if(andIndex > 0)
        {
          andQuery.append(" AND ");
        }
        andQuery.append(prop);
        andQuery.append(":");
        andQuery.append(element.substring(1));
        andIndex++;
        continue;
      }

      if(opsNOT && element.length() > 1)
      {
        notQuery.append(" NOT ");
        notQuery.append(prop);
        notQuery.append(":");
        notQuery.append(element.substring(1));
        continue;
      }

      if(elementIndex > 0)
      {
        orQuery.append(" OR ");
      }
      orQuery.append(prop);
      orQuery.append(":");
      orQuery.append(element);
      elementIndex++;
    }

    query.append(orQuery.toString());
    if(andQuery.length() > 0)
    {
      if(query.length() > 1)
      {
        query.append(" AND ");
      }
      query.append(andQuery.toString());
    }
    query.append(notQuery.toString());
    
    query.append(" )");

    logger.debug("orQuery:"+orQuery.toString());
    logger.debug("andQuery:"+andQuery.toString());
    logger.debug("notQuery:"+notQuery.toString());
    logger.debug("query:"+query.toString());
    
    return query.toString();
    
  }

  /**
   * This was doing it the hard way: find search strings for AND, OR, and NOT
   * then make the search string together
   * @param andStrings
   * @param notStrings
   * @param orStrings
   * @param prop
  
   * @return String
   */
  private String buildPropertyQueryOld3(ArrayList andStrings, ArrayList notStrings, ArrayList orStrings, String prop) {
    StringBuilder query = new StringBuilder();
    logger.debug("andStrings size:" + andStrings.size());
    logger.debug("notStrings size:" + notStrings.size());
    logger.debug("orStrings size:" + orStrings.size());
    query.append("( ");
    int orIndex = 0;

    StringBuilder andQuery = new StringBuilder();
    int andIndex = 0;
    StringBuilder notQuery = new StringBuilder();
    
    StringBuilder orQuery = new StringBuilder();
    
    for(Object obj : andStrings)
    {
      String element = (String)obj;
      if(andIndex > 0)
      {
        andQuery.append(" AND ");
      }
      andQuery.append(prop);
      andQuery.append(":");
      andQuery.append(element);
      andIndex++;
    }
    for(Object obj : notStrings)
    {
      String element = (String)obj;
      notQuery.append(" NOT ");
      notQuery.append(prop);
      notQuery.append(":");
      notQuery.append(element);
    }

    for(Object obj : orStrings)
    {
      String element = (String)obj;
      if(orIndex > 0)
      {
        orQuery.append(" OR ");
      }
      orQuery.append(prop);
      orQuery.append(":");
      orQuery.append(element);
      orIndex++;
    }

    //Approach 1
    //There is a problem:
    //Alfresco server:
    //  +contains +"exact phrase" twwwo
    //  does not require to contain "twwwo"
    // but the syntax here requires so.
    // 
//    if(orQuery.length() > 0)
//    {
//      if(andQuery.length() > 0)
//      {
//        query.append(" (" + orQuery.toString() + ")");
//      }
//      else
//      {
//        query.append(orQuery.toString());
//      }
//    }
//    
//    if(andQuery.length() > 0)
//    {
//      if(orQuery.length() > 0)
//      {
//        query.append(" AND (");
//        query.append(andQuery.toString());
//        query.append(" )");
//      }
//      else 
//      {
//        query.append(andQuery.toString());
//      }
//    }
//
//    query.append(notQuery.toString());

    
    //Approach 2
    //There is a problem:
    //  + not a must anymore
//    if(andQuery.length() > 0)
//    {
//      if(orQuery.length() > 0)
//      {
//        query.append(" (");
//        query.append(andQuery.toString());
//        query.append(" )");
//      }
//      else 
//      {
//        query.append(andQuery.toString());
//      }
//    }
//
//    if(orQuery.length() > 0)
//    {
//      if(andQuery.length() > 0)
//      {
//        query.append(" OR (" + orQuery.toString() + " AND " + andQuery + ")");
//      }
//      else
//      {
//        query.append(orQuery.toString());
//      }
//    }
//    
//    query.append(notQuery.toString());

    //Approach 3
    //It seems work the best, except that relevant score is not so right
    //twwwo +exact
    //things contains twwwo should be higher - but it is not. 
    if(andQuery.length() > 0 && orQuery.length() > 0) 
    {
      query.append(" (" + orQuery.toString() + " AND " + andQuery + ")");
      query.append(" OR (" + andQuery + ")");
    }
    else if(orQuery.length() > 0)
    {
      query.append(orQuery);
    }
    else if(andQuery.length() > 0)
    {
      query.append(andQuery.toString());
    }

    query.append(notQuery.toString());

    query.append(" )");

//    logger.debug("orQuery:"+orQuery.toString());
//    logger.debug("andQuery:"+andQuery.toString());
//    logger.debug("notQuery:"+notQuery.toString());
    logger.debug("query:"+query.toString());
    
    return query.toString();
    
  }

  /**
   * 
   * @param parsedStrings
   * @param prop
  
   * @return String
   */
  private String buildPropertyQueryOld(String [] parsedStrings, String prop)
  {
    StringBuilder propertyString = new StringBuilder();
    propertyString.append("@");
    propertyString.append(SearchContext.escapeQualifiedName(prop));

    return buildPropertyQueryOld2(parsedStrings, propertyString.toString());
  }

  /**
   * Method buildPropertyQuery.
   * @param andStrings ArrayList
   * @param notStrings ArrayList
   * @param orStrings ArrayList
   * @param prop QualifiedName
   * @return String
   */
  private String buildPropertyQuery(ArrayList andStrings, ArrayList notStrings, ArrayList orStrings, String prop)
  {
    StringBuilder propertyString = new StringBuilder();
    propertyString.append("@");
    propertyString.append(SearchContext.escapeQualifiedName(prop));

    return buildPropertyQueryOld3(andStrings, notStrings, orStrings, propertyString.toString());
  }

  /**
   * This method build a Lucene search query from a search string
  
   * @return String
   * @throws Exception
   * @see gov.pnnl.cat.search.basic.query.IBasicSearchQuery#buildSearchQuery()
   */
  public String buildSearchQuery() throws Exception
  {
    String searchString = getSearchString();
    searchString = "(" + searchString + ")";
    
    StringBuilder query = new StringBuilder();
    query.append("ALL:" + searchString );

    query.append(" AND -ASPECT:\"");
    query.append(VeloConstants.ASPECT_IGNORE);
    query.append("\"");
    
    query.append(" AND -ASPECT:\"");
    query.append(VeloConstants.ASPECT_HIDDEN_RENDITION);
    query.append("\"");

    logger.debug("Query: " + query.toString());
    return query.toString();
  }
  
  /**
   * Method buildSearchQueryOld.
   * @return String
   * @throws Exception
   */
  private String buildSearchQueryOld() throws Exception
  {
    String searchString = getSearchString();
    
    //carina found that just wrap the user typed in search string with "()",
    //all is taken care of
    searchString = "(" + searchString + ")";
    
    StringBuilder query = new StringBuilder();

    //On 5/16/2007, found that the PATH specification does not make a difference on search result
    // and is really expensive (search on "china" with about 1000 matches, 
    //  increase time on web service from 3500 ms to 80000 ms
    //query.append("(PATH:\"/app:company_home//*\" AND -PATH:\"/app:company_home/app:*//*\") AND ");
    
    query.append("( "); //beginning of (Text OR Author OR ...) 
    query.append("TEXT:"+searchString);
    //author
    query.append(" OR ");
    query.append(buildPropertyQueryNew(searchString, VeloConstants.PROP_AUTHOR));
    
    //description
    query.append(" OR ");
    query.append(buildPropertyQueryNew(searchString, VeloConstants.PROP_DESCRIPTION));

    //title
    query.append(" OR ");
    query.append(buildPropertyQueryNew(searchString, VeloConstants.PROP_TITLE));

    //name
    query.append(" OR ");
    query.append(buildPropertyQueryNew(searchString, VeloConstants.PROP_NAME));
    

//    ArrayList andStrings = new ArrayList<String>();
//    ArrayList orStrings = new ArrayList<String>();
//    ArrayList notStrings = new ArrayList<String>();    
//    parseSearchString2(searchString, andStrings, notStrings, orStrings);

//    query.append("( "); //beginning of (Text OR Author OR ...) 
//    
//    //TEXT
//    query.append(buildPropertyQuery(andStrings, notStrings, orStrings, "TEXT"));
//    //author
//    query.append(" OR ");
//    query.append(buildPropertyQuery(andStrings, notStrings, orStrings, VeloConstants.PROP_AUTHOR));
//
//    //description
//    query.append(" OR ");
//    query.append(buildPropertyQuery(andStrings, notStrings, orStrings, VeloConstants.PROP_DESCRIPTION));
//
//    //title
//    query.append(" OR ");
//    query.append(buildPropertyQuery(andStrings, notStrings, orStrings, VeloConstants.PROP_TITLE));
//
//    //name
//    query.append(" OR ");
//    query.append(buildPropertyQuery(andStrings, notStrings, orStrings, VeloConstants.PROP_NAME));

    query.append(") AND (TYPE:\""); //end of (Text OR Title OR ...)
    query.append(Constants.TYPE_FOLDER);
    query.append("\" OR TYPE:\"");
    query.append(Constants.TYPE_CONTENT);
    query.append("\" OR TYPE:\"");
    query.append(VeloConstants.TYPE_LINKED_FILE);
    query.append("\")"); 
    query.append(" AND -ASPECT:\"");
    query.append(VeloConstants.ASPECT_IGNORE);
    query.append("\"");
    query.append(" AND -ASPECT:\"");
    query.append(VeloConstants.ASPECT_HIDDEN_RENDITION);
    query.append("\"");

    logger.debug("Query: " + query.toString());
    return query.toString();  
  }  
  
  /**
   * Get the 
   * @param searchString
  
  
  
   * @param stringList ArrayList
   * @param del String
   * @return String
   */
  private String getStrings(String searchString, ArrayList stringList, String del)
  {
    String theSearchStr = searchString;

    String oneString;
    
    StringTokenizer t = new StringTokenizer(theSearchStr, del);
    int tokenCount = t.countTokens();
    logger.debug("token + count:" + tokenCount);
    if(tokenCount > 1 || theSearchStr.indexOf(del) == 0) //there are one or more + signs
    {
      for (int i=0; i<tokenCount; i++)
      {
        if(theSearchStr.length() == 0)
          break;
        int indexBegin = theSearchStr.indexOf(del);
        if(indexBegin < 0 || theSearchStr.length() < indexBegin+1) //no more
          break;
        String block = theSearchStr.substring(indexBegin+1);
        int length = block.length();
        if(block.indexOf(del) > 0)
        {
          length = block.indexOf(del);
        }
        block = block.substring(0, length);
        logger.debug("block:" + block);
        //at this moment, block contains good stuff but need to split for space and ""
        if(block.charAt(0) == '\"') // e.g., +"exact phrase" others -- find the closing " and wrap the whole thing with "
        {
          int indexNextDoubleQuote = block.substring(1).indexOf("\"");
          if(indexNextDoubleQuote > 0)
          {
            length = indexNextDoubleQuote+2;
            oneString = block.substring(0, length);
          }
          else
          {
            //length still the same
            //not yet
            oneString = block + "\""; //the search string does not have the closing ", add that at the end!
          }
          logger.debug("oneString:" + oneString + "END");          
        }
        else // e.g., +word1 others -- find the next space
        {
          oneString = block;
          int indexSpace = block.indexOf(" ");
          if(indexSpace > 0)
          {
            length = indexSpace;
            oneString = block.substring(0, indexSpace);
          }
          logger.debug("oneString:" + oneString);
        }
        
        String leadingRemains = "";
        if(indexBegin > 0)
        {
          leadingRemains = theSearchStr.substring(0, indexBegin);
          logger.debug("leading remains =" + leadingRemains + "END");
        }
        String trailingRemains = theSearchStr.substring(indexBegin+length+1); 
        logger.debug("trailing remains=" + trailingRemains + "END");
        stringList.add(oneString);
        theSearchStr = leadingRemains + trailingRemains;
        logger.debug("theSearchStr is now=" + theSearchStr + "END");        
      }
    }
    else
    {
      logger.debug("searchString does not have " + del + " sign");
    }
    
    return theSearchStr;
  }

  /**
   * Method buildPropertyQueryNew.
   * @param searchString String
   * @param prop QualifiedName
   * @return String
   */
  private String buildPropertyQueryNew(String searchString, String prop)
  {
    StringBuilder propertyString = new StringBuilder();
    propertyString.append("@");
    propertyString.append(SearchContext.escapeQualifiedName(prop));
    propertyString.append(":" + searchString);
    return propertyString.toString();
  }

}
