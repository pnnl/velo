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

import gov.pnnl.cat.search.basic.results.contextmenu.SearchResultsContextMenu;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.SearchResultEvent;
import gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchViewPage;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.actions.PropertiesActionGroup;
import gov.pnnl.cat.ui.rcp.views.dnd.DNDSupport;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableViewerSorter;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.CreatedDateComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.CreatorComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.ModifiedDateComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.NameComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.PathComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.SizeComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.TypeComparator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;


/**
 */
public abstract class AbstractBasicSearchResultsPage extends AbstractTextSearchViewPage {
  public static final String FILE_NAME    = "Name";
  public static final String SIZE         = "Size";
  public static final String TYPE         = "Type";
  public static final String CREATOR      = "Creator";
  public static final String CREATED      = "Created";
  public static final String MODIFIED     = "Modified";
  public static final String PATH         = "Path";

  /**
   * Defines the column names used in this search result page.
   */
  public static final String[] COLUMN_NAMES = {FILE_NAME, SIZE, TYPE, CREATOR, CREATED, MODIFIED, PATH};
  // the default width of each column in the TableViewer
  private static final int[] COLUMN_WIDTHS = {166, 72, 90, 90, 90, 83, 158};
  // the default style of each column in the TableViewer
  private static final int[] COLUMN_STYLES = {SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT};

  // our sorters for the different columns
  private TableViewerSorter[] sorters = new TableViewerSorter[] {
    new TableViewerSorter(new NameComparator()),
    new TableViewerSorter(new SizeComparator()),
    new TableViewerSorter(new TypeComparator()),
    new TableViewerSorter(new CreatorComparator()),
    new TableViewerSorter(new CreatedDateComparator()),
    new TableViewerSorter(new ModifiedDateComparator()),
    new TableViewerSorter(new PathComparator())
  };

  // the index default sorter located in the above array
  private static final int DEFAULT_SORTER = 0;

  protected ISearchResultsContentProvider mContentProvider;

  private TableViewerSorter currentSorter;

  private TableColumn currentColumn;

  private SearchResultsContextMenu contextMenu;


  /**
   * Constructor for AbstractBasicSearchResultsPage.
   * @param supportedLayouts int
   */
  protected AbstractBasicSearchResultsPage(int supportedLayouts) {
    super(supportedLayouts);
  }

  /**
   * Method setInput.
   * @param search ISearchResult
   * @param uiState Object
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultPage#setInput(ISearchResult, Object)
   */
  public void setInput(ISearchResult search, Object uiState) {
    super.setInput(search, uiState);
    // use the PropertiesActionGroup to handle the properties action for us
    PropertiesActionGroup propertiesActionGroup = new PropertiesActionGroup(this.getViewPart(), getViewer());
    propertiesActionGroup.fillActionBars(this.getViewPart().getViewSite().getActionBars());
  }

  /**
   * This method is called when the elements of the search result are changed.
   * For example, this method is called as results become known for the search.
   * @param objects Object[]
   */
  protected void elementsChanged(Object[] objects) {
    // notify our content provider that some elements have changed (added, removed, or simply changed).
    if (this.mContentProvider != null) {
      this.mContentProvider.elementsChanged(objects);

      // we've had a strange problem where when the search results come in
      // one of them is selected, even though the user didn't select it.
      // this causes a problem if things are listening to selections, because
      // then we'd have a result selected, but there wouldn't be a notification
      // about it. to work around this problem, we'll explicitly set the selection
      // here to whatever is currently selected. this will fire the appropriate
      // actions so that the selection listeners are notified of the selection.
      getViewer().setSelection(getViewer().getSelection());
    }
  }

  protected void clear() {
    if (this.mContentProvider != null) {
      this.mContentProvider.clear();
    }
  }

  /**
   * Method fillContextMenu.
   * @param manager IMenuManager
   */
  protected void fillContextMenu(IMenuManager manager) {
    this.contextMenu.menuAboutToShow(manager);
  }
  
  /**
   * Method createContextMenu.
   * @param viewer ContentViewer
   */
  protected void createContextMenu(ContentViewer viewer) {
    this.contextMenu = new SearchResultsContextMenu(this.getViewPart().getSite().getWorkbenchWindow(), viewer);
    viewer.getControl().addFocusListener(contextMenu);
    viewer.addSelectionChangedListener(contextMenu);   
  }
  

  /*
   * This method will be called by our parent class to create the TreeViewer.
   *  (non-Javadoc)
   * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#createTreeViewer(org.eclipse.swt.widgets.Composite)
   */
  protected TreeViewer createTreeViewer(Composite parent) {
    Tree tree = createTree(parent);
    TreeViewer viewer = new TreeViewer(tree);
    int dragOperations = DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_LINK;
    DNDSupport.addDragSupport(viewer, dragOperations);
    
    return viewer;
  }
  /**
   * Creates the Tree used to construct our TreeViewer.
   * @param parent Composite
   * @return Tree
   */
  protected Tree createTree(Composite parent) {
    System.out.println("BasicSearchResultPage::createTree()");
    int style = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
                SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

    Tree tree = new Tree(parent, style);
    TreeColumn column;
    GridData gridData;

    if (COLUMN_NAMES.length != COLUMN_WIDTHS.length ||
        COLUMN_NAMES.length != COLUMN_STYLES.length) {
      // TODO Log a message that explains the error, then use column names only and ignore column widths.
      throw new RuntimeException();
    }

    gridData = new GridData(GridData.FILL_BOTH);
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalSpan = 3;
    tree.setLayoutData(gridData);    

//    tree.setLinesVisible(true);
    tree.setHeaderVisible(true);

    // create each column
    for (int i = 0; i < COLUMN_NAMES.length; i++) {
      column = new TreeColumn(tree, COLUMN_STYLES[i], i);
      column.setText(COLUMN_NAMES[i]);
      column.setWidth(COLUMN_WIDTHS[i]);
    }

    return tree;
  }


  /**
   * Method assignSortersToColumns.
   * @param table Table
   * @param sorters TableViewerSorter[]
   * @param viewer TableViewer
   */
  private void assignSortersToColumns(Table table, final TableViewerSorter[] sorters, final TableViewer viewer) {
    for (int i = 0; i < table.getColumnCount(); i++) {
      final int index = i;
      final TableColumn column = table.getColumn(i);

      column.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent event) {
          handleWidgetSelected(sorters[index], column, viewer);
        }
      });
    }
  }

  /*
   * This method will be called by our parent class to create the TableViewer.
   *  (non-Javadoc)
   * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#createTableViewer(org.eclipse.swt.widgets.Composite)
   */
  protected TableViewer createTableViewer(Composite parent) {
    Table table = createTable(parent);
    
    
    TableViewer viewer = new TableViewer(table);

    currentSorter = sorters[DEFAULT_SORTER];
    currentColumn = table.getColumn(DEFAULT_SORTER);
    setColumnHeaderImage(currentColumn, true);
    viewer.setSorter(currentSorter);

    assignSortersToColumns(table, this.sorters, viewer);

    int dragOperations = DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_LINK;
    DNDSupport.addDragSupport(viewer, dragOperations);

    return viewer;
  }
  
  
  
  /**
   * Method createSearchResultsTable.
   * @param parent Composite
   * @return Table
   */
  protected Table createSearchResultsTable(Composite parent) {
    int style = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
    SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

    Table table = new Table(parent, style);
    TableColumn column;
    GridData gridData;

    if (COLUMN_NAMES.length != COLUMN_WIDTHS.length ||
        COLUMN_NAMES.length != COLUMN_STYLES.length) {
      // TODO Log a message that explains the error, then use column names only and ignore column widths.
      throw new RuntimeException();
    }

    gridData = new GridData(GridData.FILL_BOTH);
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalSpan = 3;
    table.setLayoutData(gridData);    

//    table.setLinesVisible(true);
    table.setHeaderVisible(true);

    // create each column
    for (int i = 0; i < COLUMN_NAMES.length; i++) {
      column = new TableColumn(table, COLUMN_STYLES[i], i);
      column.setText(COLUMN_NAMES[i]);
      column.setWidth(COLUMN_WIDTHS[i]);
    }

    
    return table;
  }
  
  /* Open the selection.
   * (non-Javadoc)
   * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#handleOpen(org.eclipse.jface.viewers.OpenEvent)
   */
  protected void handleOpen(OpenEvent event) {
    contextMenu.doubleClick();
  }
  
  
  /**
   * Method getViewer.
   * @return ContentViewer
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultPage#getViewer()
   */
  public ContentViewer getViewer() {
    return super.getViewer();
  }
  

  /**
   * Method handleWidgetSelected.
   * @param sorter TableViewerSorter
   * @param column TableColumn
   * @param viewer TableViewer
   */
  private void handleWidgetSelected(TableViewerSorter sorter, TableColumn column, TableViewer viewer) {
    if (currentSorter == sorter) {
      sorter.toggleSortOrder();
    } else {
      currentSorter = sorter;
    }
    setColumnHeaderImage(column, sorter.isAscending());
    viewer.getControl().setRedraw(false);
    viewer.setSorter(sorter);
    viewer.getControl().setRedraw(true);
    viewer.refresh();
    this.currentColumn = column;
  }

  /**
   * Method setColumnHeaderImage.
   * @param column TableColumn
   * @param sortAscending boolean
   */
  private void setColumnHeaderImage(TableColumn column, boolean sortAscending) {
    this.currentColumn.setImage(null);
    if (sortAscending) {
      column.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_UP, SharedImages.CAT_IMG_SIZE_16));
    } else {
      column.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_DOWN, SharedImages.CAT_IMG_SIZE_16));
    }
  }
  
  /**
   * Creates the Table used to construct our TableViewer.
   * @param parent Composite
   * @return Table
   */
  protected Table createTable(Composite parent) {
    return createSearchResultsTable(parent);
  }
  
  /**
   * Method handleSearchResultsChanged.
   * @param e SearchResultEvent
   */
  @Override
  protected synchronized void handleSearchResultsChanged(final SearchResultEvent e) {
    super.handleSearchResultsChanged(e);
  }


}
