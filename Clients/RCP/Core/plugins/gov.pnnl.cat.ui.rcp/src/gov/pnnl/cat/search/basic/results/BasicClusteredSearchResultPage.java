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

import gov.pnnl.cat.core.resources.search.ICluster;
import gov.pnnl.cat.search.basic.results.ClusteredSearchResult.MatchFilterEvent;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.SearchResultEvent;
import gov.pnnl.cat.search.eclipse.search.ui.text.Match;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * Provides custom behavior for displaying results of a repository search.
 * 
 * @author Eric Marshall
 * 
 * @version $Revision: 1.0 $
 */
public class BasicClusteredSearchResultPage extends AbstractBasicSearchResultsPage implements ISelectionChangedListener {

  private TreeViewer clusterTree;

  private ClusteredSearchResult search; // do we really need this as the super

  private SearchResultsTableContentProvider fContentProvider;

  private ICluster selectedCluster = null;

  // class contains fInput

  public BasicClusteredSearchResultPage() {
    // call our super constructor noting that we only support a flat layout.
    super(FLAG_LAYOUT_FLAT);
  }

  /**
   * Method configureTreeViewer.
   * @param viewer TreeViewer
   */
  protected void configureTreeViewer(TreeViewer viewer) {
    throw new RuntimeException("Tree Viewer is not supported in search results.");
  }

  /**
   * Method configureTableViewer.
   * @param viewer TableViewer
   */
  protected void configureTableViewer(TableViewer viewer) {
    viewer.setLabelProvider(new CatWorkbenchLabelProvider(viewer));
    // viewer.setLabelProvider(new SearchResultsLabelProvider());

    // viewer.setContentProvider(new TableCatWorkbenchProvider(new int[] {}));
    fContentProvider = new SearchResultsTableContentProvider(this);
    viewer.setContentProvider(fContentProvider);
    this.mContentProvider = (ISearchResultsContentProvider) viewer.getContentProvider();

    // also set providers for the cluster tree
    clusterTree.setContentProvider(new ClusteredSearchResultsTreeContentProvider());
    clusterTree.setLabelProvider(new ClusteredSearchTreeLabelProvider());
    createContextMenu(viewer);
  }

  /**
   * Method elementsChanged.
   * @param objects Object[]
   */
  protected void elementsChanged(Object[] objects) {
    super.elementsChanged(objects);

    // TODO: find a better place for this setInput()
    // But this is done only once - so maybe it is OK
    if (search != null && search.getClusteredResultRoot() != null) {
      this.clusterTree.setInput(search);
    }
    
  }
  
  

  /**
   * Creates the Table used to construct our TableViewer.
   * @param parent Composite
   * @return Table
   */
  protected Table createTable(Composite parent) {
    SashForm m_sash = new SashForm(parent, SWT.NONE);
    GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
    gd.horizontalSpan = 1;

    m_sash.setLayoutData(gd);

    // Composite leftSide = new Composite(m_sash, SWT.BORDER);
    // leftSide.setLayoutData(gd);

    clusterTree = createClustersTree(m_sash);

    Table table = createSearchResultsTable(m_sash);

    int[] sashWeight = new int[] { 35, 65 };

    m_sash.setWeights(sashWeight);
    return table;
  }

  /**
   * Method createClustersTree.
   * @param parent Composite
   * @return TreeViewer
   */
  private TreeViewer createClustersTree(Composite parent) {
    TreeViewer fTreeViewer = new TreeViewer(parent, SWT.FULL_SELECTION);
    // Tree fTree = fTreeViewer.getTree();

    // ClusteredSearchResultInput input = new
    // ClusteredSearchResultInput(clusteredResult.getClusteredResultRoot());
    // fTree.setHeaderVisible(true);

    fTreeViewer.addSelectionChangedListener(this);
    return fTreeViewer;

  }

  /**
   * Method setInput.
   * @param search ISearchResult
   * @param viewState Object
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultPage#setInput(ISearchResult, Object)
   */
  public void setInput(ISearchResult search, Object viewState) {
    super.setInput(search, viewState);
    this.selectedCluster = null;//no filter to begin with
    this.search = (ClusteredSearchResult) search;
  }

  //= to SRP setFilters
  /**
   * Method selectionChanged.
   * @param event SelectionChangedEvent
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event) {
    StructuredSelection selection = (StructuredSelection) event.getSelection();
    this.selectedCluster = (ICluster) selection.getFirstElement();
    if(selectedCluster != null){
      search.setActivatedFilters(selectedCluster);
    }
  }
 
  /**
   * Method getDisplayedMatchCount.
   * @param element Object
   * @return int
   */
  @Override
  public int getDisplayedMatchCount(Object element) {
    if (this.selectedCluster == null)
      return super.getDisplayedMatchCount(element);
    Match[] matches= super.getDisplayedMatches(element);
    int count= 0;
    for (int i= 0; i < matches.length; i++) {
      if (!matches[i].isFiltered())
        count++;
    }
    return count;
  }
  
  /**
   * Method getDisplayedMatches.
   * @param element Object
   * @return Match[]
   */
  @Override
  public Match[] getDisplayedMatches(Object element) {
    if (this.selectedCluster == null)
      return super.getDisplayedMatches(element);
    Match[] matches= super.getDisplayedMatches(element);
    int count= 0;
    for (int i= 0; i < matches.length; i++) {
      if (matches[i].isFiltered())
        matches[i]= null;
      else 
        count++;
    }
    Match[] filteredMatches= new Match[count];
    
    int writeIndex= 0;
    for (int i= 0; i < matches.length; i++) {
      if (matches[i] != null)
        filteredMatches[writeIndex++]= matches[i];
    }
    
    return filteredMatches;
  }
  
  
  /* (non-Javadoc)
   * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#handleSearchResultChanged(org.eclipse.search.ui.SearchResultEvent)
   */
  /**
   * Method handleSearchResultsChanged.
   * @param e SearchResultEvent
   */
  @Override
  protected synchronized void handleSearchResultsChanged(final SearchResultEvent e) {
    super.handleSearchResultsChanged(e);
    if (e instanceof MatchFilterEvent) {
      filtersChanged();
    }
  }
  
  
  
  private void filtersChanged() {
    getViewer().refresh();
    
    getViewPart().updateLabel();
  }
  
  protected void clear() {
    if (fContentProvider != null)
      fContentProvider.clear();
  }
  
}
