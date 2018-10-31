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


import gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchResult;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 */
public class SearchResultsTableContentProvider implements IStructuredContentProvider, ISearchResultsContentProvider {
  // these would be better stored in a preferences class
  private static final int TABLE_LIMIT = 1000;
  private static final boolean TABLE_IS_LIMITED = true;

  // an optimization to avoid the overhead of creating new Object[0] multiple times.
  private static final Object[] EMPTY_ARR = new Object[0];
  
  private AbstractBasicSearchResultsPage mPage;
  private AbstractTextSearchResult mResult;

  /**
   * Constructor for SearchResultsTableContentProvider.
   * @param page AbstractBasicSearchResultsPage
   */
  public SearchResultsTableContentProvider(AbstractBasicSearchResultsPage page) {
    this.mPage= page;
  }
  
  /**
   * Method dispose.
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose() {
    // nothing to do
  }
  
  /**
   * Method getElements.
   * @param inputElement Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
   */
  public Object[] getElements(Object inputElement) {
    if (inputElement instanceof AbstractTextSearchResult) {
      Set<Object> filteredElements= new HashSet<Object>();
      Object[] rawElements= ((AbstractTextSearchResult)inputElement).getElements();

      for (int i= 0; i < rawElements.length; i++) {
        if (this.mPage.getDisplayedMatchCount(rawElements[i]) > 0){
          filteredElements.add(rawElements[i]);
        }
      }
      return filteredElements.toArray();
    }
    return EMPTY_ARR;
  }
  
  /**
   * Method inputChanged.
   * @param viewer Viewer
   * @param oldInput Object
   * @param newInput Object
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    if (newInput instanceof AbstractTextSearchResult) {
      this.mResult= (AbstractTextSearchResult) newInput;
    }
  }
  
  /**
   * Method elementsChanged.
   * @param updatedElements Object[]
   * @see gov.pnnl.cat.search.basic.results.ISearchResultsContentProvider#elementsChanged(Object[])
   */
  public void elementsChanged(Object[] updatedElements) {
    if (mResult == null)
      return;
    int addCount= 0;
    int removeCount= 0;
    TableViewer viewer= (TableViewer) mPage.getViewer();
    Set updated= new HashSet();
    Set added= new HashSet();
    Set removed= new HashSet();

    for (int i= 0; i < updatedElements.length; i++) {
      if (mPage.getDisplayedMatchCount(updatedElements[i]) > 0) {
        if (viewer.testFindItem(updatedElements[i]) != null)
          updated.add(updatedElements[i]);
        else
          added.add(updatedElements[i]);
        addCount++;
      } else {
        removed.add(updatedElements[i]);
        removeCount++;
      }
    }

    viewer.add(added.toArray());
    //viewer.update(updated.toArray(), new String[] { SearchLabelProvider.PROPERTY_MATCH_COUNT });
    viewer.remove(removed.toArray());
  }

  /**
   * Method getViewer.
   * @return TableViewer
   */
  private TableViewer getViewer() {
    return (TableViewer) this.mPage.getViewer();
  }
  
  /**
   * Method clear.
   * @see gov.pnnl.cat.search.basic.results.ISearchResultsContentProvider#clear()
   */
  public void clear() {
    getViewer().refresh();
  }
  
  
}
