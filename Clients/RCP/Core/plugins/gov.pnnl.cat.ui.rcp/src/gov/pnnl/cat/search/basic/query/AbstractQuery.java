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

import gov.pnnl.cat.core.internal.resources.search.SearchManager;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.search.basic.results.ClusteredSearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.text.Match;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * TODO: This class should not be named AbstractClusteredQuery, since
 * it's not just for clustered queries.  The name is misleading.
 * @version $Revision: 1.0 $
 */
public abstract class AbstractQuery implements ISearchQuery {

  private static Logger logger = CatLogger.getLogger(AbstractQuery.class);

  protected ISearchResult searchResult;

  private boolean clustered;

  private String querySessionID;

  private Long totalHits;

  /**
   * Method getSearchResult.
   * @return ISearchResult
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery#getSearchResult()
   */
  public ISearchResult getSearchResult() {
    // TODO: check if NOT clustering do this

    // How does this work?
    // Is it possible to have the same SearchQuery clustered at time 
    // and then become non-clustered? Or the SearchQuery is always re-generated
    // so this search type change will never happen?
    if (!clustered) {
      if (searchResult == null) {
        searchResult = createSearchResult();
      }
      return searchResult;
    }else{
      if(searchResult == null)
      {
        searchResult = new ClusteredSearchResult(this);
      }
      return searchResult;
    }
  }

  public Long getTotalHits() {
    return totalHits;
  }

  /**
   * Method createSearchResult.
   * @return ISearchResult
   */
  protected abstract ISearchResult createSearchResult();

  /**
   * Method searchValid.
   * @return boolean
   */
  protected abstract boolean searchValid();

  /**
   * Method buildSearchQuery.
   * @return String
   * @throws Exception
   */
  protected abstract String buildSearchQuery() throws Exception;

  /**
   * Method setClustered.
   * @param isClusteredSearch boolean
   */
  public void setClustered(boolean isClusteredSearch) {
    this.clustered = isClusteredSearch;
  }

  /**
   * TODO: need to test this and decide if we need to modify the JCR search
   * string
   * 
   * @param searchString
  
   * @param monitor IProgressMonitor
   * @throws Exception
   */
  protected void performSearch(String searchString, IProgressMonitor monitor) throws Exception {

    //if the search is not valid, or is canceled, do no more work
    if (!searchValid() || monitor.isCanceled()) {
      return;
    }

    ICatQueryResult results = executeSearch();
    if(results != null)
    {
      final AbstractTextSearchResult textResult = (AbstractTextSearchResult) getSearchResult();
      textResult.removeAll();

      addSearchMatches(results, textResult, monitor);
    }
    else
    {
      ToolErrorHandler.handleError("The search string is not valid. Please check your syntax and try again.", null, true);
    }
  }

  /**
   * @param results
   * @param textResult
   * @param monitor 
   */
  private void addSearchMatches(ICatQueryResult results, final AbstractTextSearchResult textResult, IProgressMonitor monitor) {
    this.totalHits = results.getTotalHits();
    if(results.getHandles() != null) 
    {
      for (IResource resource : results.getHandles()) {
        //we check monitor status and stop if it is canceled, but
        //since this client code executes so fast and most time spent on search is
        //on the server side, doing this does not have any practical effect - cancel only
        //happens between fetchMore()
        if(monitor.isCanceled())
        {
          return;
        }
        
        Match m = new Match(resource, 0, 0);
        textResult.addMatch(m);
      }
    }
  }

  /**
   * Method executeSearch.
   * @return List<IResource>
   * @throws Exception
   */
  public ICatQueryResult executeSearch() throws Exception {
    String searchQuery = buildSearchQuery();
    long begin = System.currentTimeMillis();
    this.querySessionID = null;

    // For now, only bring back first 100 results
    // TODO: add paging
    ICatQueryResult results = ((SearchManager)ResourcesPlugin.getDefault().getSearchManager()).query(searchQuery, 
        false, /*VeloConstants.PROP_NAME*/ null, "ascending", 100, null);
      
    long end = System.currentTimeMillis();
    System.out.println("Took " + (end - begin) + " ms to execute the query");

    return results;
  }

}
