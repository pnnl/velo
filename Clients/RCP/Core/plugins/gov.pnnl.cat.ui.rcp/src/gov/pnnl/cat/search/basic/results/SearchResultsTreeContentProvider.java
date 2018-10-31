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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider for displaying a flat tree of people search results.
 * Although technically a tree, all of the content is provided at the same depth.
 * 
 * @author Eric Marshall
 *
 * @version $Revision: 1.0 $
 */
public class SearchResultsTreeContentProvider implements ITreeContentProvider, ISearchResultsContentProvider {
  // an optimization to avoid the overhead of creating new Object[0] multiple times.
  private static final Object[] EMPTY_ARR = new Object[0];

  // the "invisible" root node of this tree.
  private Object invisibleRoot = new Object();

  // a flat list of children of the root node.
  // this will store all of the search results
  private List mChildren = new LinkedList();

  // the search result we are providing content for
  private AbstractTextSearchResult mResult;
  private AbstractTreeViewer mTreeViewer;

  /**
   * Constructor for SearchResultsTreeContentProvider.
   * @param viewer AbstractTreeViewer
   */
  SearchResultsTreeContentProvider(AbstractTreeViewer viewer) {
    this.mTreeViewer = viewer;
  }

  /**
   * Method getElements.
   * @param inputElement Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(Object)
   */
  public Object[] getElements(Object inputElement) {
    return getChildren(this.invisibleRoot);
  }
  
  /**
   * Method dispose.
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose() {
    // nothing to do
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
      initialize((AbstractTextSearchResult) newInput);
    }
  }

  /**
   * Initializes the content provider with a new search result.
   * This will remove any previous results and add those that are already known.
   * @param result AbstractTextSearchResult
   */
  protected synchronized void initialize(AbstractTextSearchResult result) {
    this.mResult= result;
    this.mChildren.clear();
    if (result != null) {
      Object[] elements= result.getElements();

      // add any hits that the search result may already have.
      for (int i= 0; i < elements.length; i++) {
        insert(elements[i], false);
      }
    }
  }

  /**
   * Inserts a child that has not already been added.
   * @param child
   * @param refreshViewer
   */
  protected void insert(Object child, boolean refreshViewer) {
    if (!this.mChildren.contains(child)) {
      this.mChildren.add(child);
      this.mTreeViewer.add(this.invisibleRoot, child);
      if (refreshViewer) {
        this.mTreeViewer.refresh();
      }
    }
  }

  /**
   * Removes the specified element from the list of children, if it was present.
   * @param element the element to remove
   * @param refreshViewer true if the viewer should refresh to reflect the new change
   */
  protected void remove(Object element, boolean refreshViewer) {
    if (this.mChildren.remove(element) && refreshViewer) {
      this.mTreeViewer.refresh();
    }
  }

  /**
   * Method getChildren.
   * @param parentElement Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
   */
  public Object[] getChildren(Object parentElement) {
    if (parentElement == this.invisibleRoot) {
      return this.mChildren.toArray();
    }
    return EMPTY_ARR;
  }

  /**
   * Method hasChildren.
   * @param element Object
   * @return boolean
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
   */
  public boolean hasChildren(Object element) {
    return false;
  }

  /**
   * Method elementsChanged.
   * @param updatedElements Object[]
   * @see gov.pnnl.cat.search.basic.results.ISearchResultsContentProvider#elementsChanged(Object[])
   */
  public synchronized void elementsChanged(Object[] updatedElements) {
    for (int i= 0; i < updatedElements.length; i++) {
      if (this.mResult.getMatchCount(updatedElements[i]) > 0)
        insert(updatedElements[i], true);
      else
        remove(updatedElements[i], true);
    }
  }

  /**
   * Method clear.
   * @see gov.pnnl.cat.search.basic.results.ISearchResultsContentProvider#clear()
   */
  public void clear() {
    initialize(this.mResult);
    this.mTreeViewer.refresh();
  }

  /**
   * Method getParent.
   * @param element Object
   * @return Object
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
   */
  public Object getParent(Object element) {
    if (element == this.invisibleRoot) {
      return null;
    }
    return this.invisibleRoot;
  }

}
