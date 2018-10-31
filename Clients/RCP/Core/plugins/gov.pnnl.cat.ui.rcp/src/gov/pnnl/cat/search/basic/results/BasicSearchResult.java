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
package gov.pnnl.cat.search.basic.results;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.search.basic.query.BasicSearchQuery;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPluginImages;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.text.IEditorMatchAdapter;
import gov.pnnl.cat.search.eclipse.search.ui.text.IFileMatchAdapter;
import gov.pnnl.cat.search.eclipse.search.ui.text.Match;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;

/**
 * @author d3l028
 * 
 * @version $Revision: 1.0 $
 */
public class BasicSearchResult extends AbstractResourceSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {
  private final Match[] EMPTY_ARR = new Match[0];

  private ISearchQuery mQuery;

  /**
   * Constructor for BasicSearchResult.
   * @param job ISearchQuery
   */
  public BasicSearchResult(ISearchQuery job) {
    mQuery = job;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getEditorMatchAdapter()
   */
  public IEditorMatchAdapter getEditorMatchAdapter() {
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getFileMatchAdapter()
   */
  public IFileMatchAdapter getFileMatchAdapter() {
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor()
   */
  public ImageDescriptor getImageDescriptor() {
   // return null;
    return SearchPluginImages.DESC_OBJ_TSEARCH_DPDN;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchResult#getLabel()
   */
  public String getLabel() {
    if (getQuery() instanceof BasicSearchQuery) {
      BasicSearchQuery query = (BasicSearchQuery) getQuery();
      String searchDescription = query.getSearchString();
      String matches = "matches";

      if (getMatchCount() == 1) {
        matches = "match";
      }
      
      if(query.getTotalHits() != null && query.getTotalHits() > getMatchCount()){
        // i.e 'cat' - top 100 (out of 1000) matches 
        return "'" + searchDescription + "' - showing " + getMatchCount() + " (out of " + query.getTotalHits() + ") " + matches;
      }else{
        return "'" + searchDescription + "' - " + getMatchCount() + " " + matches;
      }
    }

    return "Search";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchResult#getTooltip()
   */
  public String getTooltip() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchResult#getQuery()
   */
  public ISearchQuery getQuery() {
    return mQuery;
  }

  /**
   * Method isShownInEditor.
   * @param match Match
   * @param editor IEditorPart
   * @return boolean
   * @see gov.pnnl.cat.search.eclipse.search.ui.text.IEditorMatchAdapter#isShownInEditor(Match, IEditorPart)
   */
  public boolean isShownInEditor(Match match, IEditorPart editor) {
    // we arent going to be opening editors inside the RCP at this time
    return false;
  }

  
  /**
   * Method computeContainedMatches.
   * @param result AbstractTextSearchResult
   * @param editor IEditorPart
   * @return Match[]
   * @see gov.pnnl.cat.search.eclipse.search.ui.text.IEditorMatchAdapter#computeContainedMatches(AbstractTextSearchResult, IEditorPart)
   */
  public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
    // we aren't using editors with search results yet
    return EMPTY_ARR;
  }

  /**
   * Method computeContainedMatches.
   * @param result AbstractTextSearchResult
   * @param file IFile
   * @return Match[]
   */
  public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
    return getMatches(file);
  }

  /**
   * Method getFile.
   * @param element Object
   * @return IFile
   */
  public IFile getFile(Object element) {
    if (element instanceof IFile)
      return (IFile) element;
    return null;
  }

  /**
   * Method cacheCleared.
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // TODO Auto-generated method stub
    
  }
}
