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
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.search.ICluster;
import gov.pnnl.cat.search.basic.query.BasicSearchQuery;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPluginImages;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.SearchResultEvent;
import gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.text.IEditorMatchAdapter;
import gov.pnnl.cat.search.eclipse.search.ui.text.IFileMatchAdapter;
import gov.pnnl.cat.search.eclipse.search.ui.text.Match;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;

/**
 */
public class ClusteredSearchResult extends AbstractResourceSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {

  private final Match[] EMPTY_ARR = new Match[0];

  private ICluster clusteredResultRoot;

  private ISearchQuery mQuery;

  /**
   * Constructor for ClusteredSearchResult.
   * @param job ISearchQuery
   */
  public ClusteredSearchResult(ISearchQuery job) {
    mQuery = job;
  }

  /**
   * Method getImageDescriptor.
   * @return ImageDescriptor
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResult#getImageDescriptor()
   */
  public ImageDescriptor getImageDescriptor() {
    return SearchPluginImages.DESC_OBJ_TSEARCH_DPDN;
  }

  /**
   * Method getLabel.
   * @return String
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResult#getLabel()
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

  /**
   * Method getQuery.
   * @return ISearchQuery
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResult#getQuery()
   */
  public ISearchQuery getQuery() {
    return mQuery;
  }

  /**
   * Method getTooltip.
   * @return String
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResult#getTooltip()
   */
  public String getTooltip() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getClusteredResultRoot.
   * @return ICluster
   */
  public ICluster getClusteredResultRoot() {
    return clusteredResultRoot;
  }

  /**
   * Method setClusteredResultRoot.
   * @param clusteredResultRoot ICluster
   */
  public void setClusteredResultRoot(ICluster clusteredResultRoot) {
    this.clusteredResultRoot = clusteredResultRoot;
  }

  /**
   * Method getEditorMatchAdapter.
   * @return IEditorMatchAdapter
   */
  @Override
  public IEditorMatchAdapter getEditorMatchAdapter() {
    return this;
  }

  /**
   * Method getFileMatchAdapter.
   * @return IFileMatchAdapter
   */
  @Override
  public IFileMatchAdapter getFileMatchAdapter() {
    return this;
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

  // Is this method needed so we can re-setInput() for the search result
  // when the cluster tree selection got changed?
  /**
   * Method getChild.
   * @param child ICluster
   * @return ClusteredSearchResult
   */
  public ClusteredSearchResult getChild(ICluster child) {
    ClusteredSearchResult childSearchResult = null;

    // need to make sure that the child is a real one
    if (clusteredResultRoot != null && clusteredResultRoot.hasChild(child)) {
      childSearchResult = new ClusteredSearchResult(mQuery);
      childSearchResult.setClusteredResultRoot(child);
    }
    return childSearchResult;
  }

  // for testing only
  /**
   * Method getFirstChild.
   * @return ClusteredSearchResult
   */
  public ClusteredSearchResult getFirstChild() {
    ClusteredSearchResult childSearchResult = null;

    // need to make sure that the child is a real one
    if (clusteredResultRoot != null) {
      List<ICluster> childs = clusteredResultRoot.getSubclusters();
      if (childs.size() > 0) {
        childSearchResult = new ClusteredSearchResult(mQuery);

        childSearchResult.setClusteredResultRoot(childs.get(0));
      }
    } else
      System.out.println("root is null");

    return childSearchResult;
  }

  /**
   * Method setActivatedFilters.
   * @param selectedCluster ICluster
   */
  public void setActivatedFilters(ICluster selectedCluster) {
    filterMatches(this.clusteredResultRoot, selectedCluster);//=JSRupdateFilterState
    fireChange(new MatchFilterEvent(this));
  }
  
  
  /**
   * Method filterMatches.
   * @param currentCluster ICluster
   * @param selectedCluster ICluster
   */
  private void filterMatches(ICluster currentCluster, ICluster selectedCluster) {
    List<IResource> resources = currentCluster.getReourceMatches();
    // assuming that a resource is only in one cluster of the tree
    // if we're currently at the selected cluster, don't filter any of its
    // matches:
    boolean filter = true;
    if (selectedCluster.getPath().isPrefixOf(currentCluster.getPath())) {
      filter = false;
    }
    for (IResource resource : resources) {
      Match[] matches = this.getMatches(resource);
      for (int i = 0; i < matches.length; i++) {
        matches[i].setFiltered(filter);
      }
    }

    for (ICluster child : currentCluster.getSubclusters()) {
      filterMatches(child, selectedCluster);
    }
  }
  

  /**
   */
  public static class MatchFilterEvent extends SearchResultEvent {
    /**
     * Constructor for MatchFilterEvent.
     * @param searchResult ISearchResult
     */
    protected MatchFilterEvent(ISearchResult searchResult) {
      super(searchResult);
    }

    private static final long serialVersionUID = 1234L;
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
