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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.IResourceSelection;
import gov.pnnl.cat.ui.rcp.IResourceSelectionListener;
import gov.pnnl.cat.ui.rcp.ISystemUpdateListener;
import gov.pnnl.cat.ui.rcp.ITableSelectedFolderListener;
import gov.pnnl.cat.ui.rcp.SystemManager;
import gov.pnnl.cat.ui.rcp.actions.ExplorerActions;
import gov.pnnl.cat.ui.rcp.actions.OpenFileInSystemEditorAction;
import gov.pnnl.cat.ui.rcp.contextmenus.VeloResourceContextMenu;
import gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;
import gov.pnnl.cat.ui.rcp.views.adapters.TableCatWorkbenchProvider;
import gov.pnnl.cat.ui.rcp.views.dnd.DNDSupport;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.CatViewerContainer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.SelectionProviderIntermediate;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.CreatedDateComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.CreatorComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.ModifiedDateComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.NameComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.PathComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.SizeComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.TableExplorerComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.TypeComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

/**
 */
public class TableExplorer implements IResourceSelectionListener, ISystemUpdateListener, ISelectionChangedListener, CatViewerContainer {

  public static final int NAME_COLUMN = 0;

  public static final int SIZE_COLUMN = 1;

  public static final int TYPE_COLUMN = 2;
  
  public static final int CREATOR_COLUMN = 3;

  public static final int CREATED_COLUMN = 4;

  public static final int MODIFIED_COLUMN = 5;
  
  public static final int PATH_COLUMN = 6;

  //public static final int AUTHOR_COLUMN = 6;

  private Composite mainComp;

  private IResource tableInput = null;

  private Table table;

  protected TableViewer tableViewer;
  
  private TableExplorerComparator nameComparator = new NameComparator();

  private TableExplorerComparator sizeComparator = new SizeComparator();

  private TableExplorerComparator modifiedDateComparator = new ModifiedDateComparator();

  private TableExplorerComparator pathComparator = new PathComparator();

  private TableExplorerComparator typeComparator = new TypeComparator();

  //private TableExplorerComparator authorComparator = new AuthorComparator();
  private TableExplorerComparator createdDateComparator = new CreatedDateComparator();

  private TableExplorerComparator creatorComparator = new CreatorComparator();

  private TableExplorerComparator currentComparator = nameComparator;

  // TODO use DeferredContentProvider and SWT.VIRTUAL table?
  // private DeferredContentProvider contentProvider = new DeferredContentProvider(currentComparator);

  private TableViewerColumn nameColumn;

  private TableViewerColumn sizeColumn;

  private TableViewerColumn modifiedColumn;

  private TableViewerColumn pathColumn;

  private TableViewerColumn typeColumn;

  private TableViewerColumn createdColumn;

  private TableViewerColumn creatorColumn;

  private IResourceManager mgr;

  private Set<ITableSelectedFolderListener> tableSelectedFolderListeners = new HashSet<ITableSelectedFolderListener>();

  private ICatExplorerView catParentView;

  private VeloResourceContextMenu fileFolderContextMenu;

  protected OpenFileInSystemEditorAction openFileAction;

  private static Control control;

  private MenuManager popupMenuManager;
  private boolean showNewWizardInPopupMenu;
  
  private Logger logger = CatLogger.getLogger(this.getClass());

  private ISelection currentSelection;

  /**
   * Constructor for TableExplorer.
   * @param catParentView ICatView
   */
  public TableExplorer(ICatExplorerView catParentView) {
    this.catParentView = catParentView;
    try {
      this.mgr = ResourcesPlugin.getResourceManager();
    } catch (Exception e) {
      logger.error(e);
    }
    SystemManager.getInstance().addDropListener(this);
  }

  /**
   * Method getCatControl.
   * @return Control
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.CatViewerContainer#getCatControl()
   */
  public Control getCatControl() {
    return control;
  }

  /**
   * Method createPartControl.
   * @param parent Composite
   * @param showNewWizardInPopupMenu boolean
   */
  public void createPartControl(Composite parent, boolean showNewWizardInPopupMenu) {
    mainComp = new Composite(parent, SWT.NONE);
    this.showNewWizardInPopupMenu = showNewWizardInPopupMenu;

//    final GridLayout gridLayout = new GridLayout(1, true);
//    gridLayout.marginWidth = 0;
//    gridLayout.marginHeight = 0;
//    mainComp.setLayout(gridLayout);
    mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    // TODO remove lines below if using DeferredContentProvider
    tableViewer = new TableViewer(mainComp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
    tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    tableViewer.setContentProvider(new TableCatWorkbenchProvider(catParentView.getActionBars().getStatusLineManager()));
    tableViewer.setSorter(new TableViewerSorter(nameComparator));

		// TODO use the DeferredContentProvider and SWT.VIRTUAL table?
    // tableViewer = new TableViewer(new TableExplorerTable(mainComp, SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION));
    // tableViewer.setContentProvider(contentProvider);
    // tableViewer.setInput(children);

    control = tableViewer.getControl();
    tableViewer.addDoubleClickListener(new IDoubleClickListener() {
      
      OpenFileInSystemEditorAction fopenFileAction = ExplorerActions.openFileInSystemEditorAction(tableViewer, false);

      public void doubleClick(DoubleClickEvent e) {
        StructuredSelection selectedFile = (StructuredSelection) tableViewer.getSelection();
        if (selectedFile != null && !selectedFile.isEmpty()) {
          IResource selectedResource = (IResource) selectedFile.getFirstElement();
          
          // Check for custom Behavior
          boolean doubleClicked = false;
          
          for(CustomDoubleClickBehavior behavior : TreeExplorer.getCustomDoubleClickBehaviors()) {
            doubleClicked = behavior.doubleClick(selectedResource);
            if(doubleClicked) {
              break;
            } 
          }
          if(!doubleClicked) {
            if (selectedResource instanceof IFile) { // Double click on file
              // Run application on file.
              fopenFileAction.run();
              
            } else if (selectedResource instanceof IFolder) { // Double click on
              defaultDoubleClickFolder(selectedResource);
            }
          }
        }
      }
    });

    table = tableViewer.getTable();
//    table.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    table.addMouseListener(new MouseAdapter() {
      public void mouseDown(MouseEvent e) {
        Point point = new Point(e.x, e.y);

        if (table.getItem(point) == null && (e.stateMask != SWT.CTRL) && (e.stateMask != SWT.SHIFT) && (e.stateMask != (SWT.CTRL | SWT.SHIFT))) {
          tableViewer.setSelection(new StructuredSelection());
        }
      }
    });
    
    final CatWorkbenchLabelProvider catLabelProvider =  new CatWorkbenchLabelProvider(tableViewer);
    CatColumnLabelProvider  colLabelProvider = new CatColumnLabelProvider(catLabelProvider);

    nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
    nameColumn.getColumn().setText("Name");;
    ILabelDecorator decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
    nameColumn.setLabelProvider(new DecoratingStyledCellLabelProvider(catLabelProvider, decorator, null));    
    nameColumn.getColumn().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        setSorter(nameComparator, nameColumn);
      }
    });
    nameColumn.getColumn().setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_UP, SharedImages.CAT_IMG_SIZE_16));

    sizeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
    sizeColumn.getColumn().setText("Size");
    sizeColumn.getColumn().setAlignment(SWT.RIGHT);
    sizeColumn.setLabelProvider(colLabelProvider);
    sizeColumn.getColumn().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        setSorter(sizeComparator, sizeColumn);
      }
    });
    
    typeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
    typeColumn.getColumn().setText("Type");
    typeColumn.setLabelProvider(colLabelProvider);
    typeColumn.getColumn().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        setSorter(typeComparator, typeColumn);
      }
    });

    creatorColumn = new TableViewerColumn(tableViewer, SWT.NONE);
    creatorColumn.getColumn().setText("Creator");
    creatorColumn.setLabelProvider(colLabelProvider);
    creatorColumn.getColumn().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        setSorter(creatorComparator, creatorColumn);
      }
    });
    
    createdColumn = new TableViewerColumn(tableViewer, SWT.NONE);
    createdColumn.getColumn().setText("Created");
    createdColumn.setLabelProvider(colLabelProvider);
    createdColumn.getColumn().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        setSorter(createdDateComparator, createdColumn);
      }
    });
    
    modifiedColumn = new TableViewerColumn(tableViewer, SWT.NONE);
    modifiedColumn.getColumn().setText("Modified");
    modifiedColumn.setLabelProvider(colLabelProvider);
    modifiedColumn.getColumn().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        setSorter(modifiedDateComparator, modifiedColumn);
      }
    });

    pathColumn = new TableViewerColumn(tableViewer, SWT.NONE);
    pathColumn.getColumn().setText("Path");
    pathColumn.setLabelProvider(colLabelProvider);
    pathColumn.getColumn().addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        setSorter(pathComparator, pathColumn);
      }
    });
    
    TableColumnLayout tableColumnLayout = new TableColumnLayout();
    tableColumnLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(3,200));
    tableColumnLayout.setColumnData(sizeColumn.getColumn(), new ColumnWeightData(1,72));
    tableColumnLayout.setColumnData(typeColumn.getColumn(), new ColumnWeightData(2,100));
    tableColumnLayout.setColumnData(creatorColumn.getColumn(), new ColumnWeightData(2,100));
    tableColumnLayout.setColumnData(createdColumn.getColumn(), new ColumnWeightData(2,150));
    tableColumnLayout.setColumnData(modifiedColumn.getColumn(), new ColumnWeightData(2,150));
    tableColumnLayout.setColumnData(pathColumn.getColumn(), new ColumnWeightData(5,400));
    mainComp.setLayout(tableColumnLayout);

    initDND();

    // Add all supported actions (menus and buttons) for this explorer. Note:
    // Order matters... place this call before all calls that might throw a
    // handleSelectionChanged event.
    createActions();

    if (tableInput != null) {
      
      // TODO remove line below if using DeferredContentProvider
      tableViewer.setInput(tableInput);
      
      // TODO use the DeferredContentProvider and SWT.VIRTUAL table?
      // setInput();
      
      fileFolderContextMenu.selectionChanged(null);
    }

    SelectionProviderIntermediate.getInstance().addSelectionChangedListener(this);
  }
  
  /**
   * Method defaultDoubleClickFolder.
   * @param folder IResource
   */
  protected void defaultDoubleClickFolder(IResource folder) {
    if(folder.hasAspect(VeloConstants.ASPECT_REMOTE_LINK)) {
      // Call the browse remote resource action
      try {
        RSEUtils.openInRemoteSystemsExplorer(folder);
      } catch (Exception ex) {
        ToolErrorHandler.handleError("Failed to open Remote Systems Explorer", ex, true);
      } 
    } else {
      // Expand file in tree.
      fireSelectedFolderChanged(folder);
    }
  }
  
//  /**
//   * Clear the contents displayed in the table.
//   * @param event SelectionChangedEvent
//   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
//   */
//  private void clearTable() {
//    children.clear();
//    table.clearAll();
//  }

//  /**
//   * Find the children of the current {@link #tableInput}.
//   */
//  @SuppressWarnings("unused")
//  private void setInput() {
//    clearTable();
//    List<IResource> resources = ResourcesPlugin.getResourceManager().getChildren(tableInput.getPath());
//    children.addAll(resources);
//
//    final StringBuilder status = new StringBuilder();
//    status.append(resources.size());
//    status.append(" resource");
//
//    if (resources.size() > 1) {
//      status.append("s");
//    }
//
//    Display.getDefault().asyncExec(new Runnable() {
//
//      @Override
//      public void run() {
//        catParentView.getActionBars().getStatusLineManager().setMessage(status.toString());
//      }
//    });
//
//  }

  /**
   * Receive {@link SelectionChangedEvent} from other views (i.e., from ImageFlowView).
   * 
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public void selectionChanged(SelectionChangedEvent event) {
    if (!event.getSelection().equals(currentSelection)) {
      this.currentSelection = event.getSelection();

      try {
        tableViewer.setSelection(event.getSelection());
      } catch (IllegalArgumentException e) {
        // Tried to set selection to something that doesn't exist in view
      }
    }
  }

  public void dispose() {
    SystemManager.getInstance().removeDropListener(this);
  }

  private void initDND() {
    DNDSupport.addDragSupport(tableViewer);

    if (catParentView != null) {
      DNDSupport.addDropSupport(this);
    }
  }
  
  /**
   * Create the Actions for this explorer.
   */
  private void createActions() {

    openFileAction = new OpenFileInSystemEditorAction();
    openFileAction.setViewer(tableViewer);

    // Add the context menu for this explorer.
    popupMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$

    if (catParentView != null) {
      fileFolderContextMenu = new VeloResourceContextMenu(catParentView, tableViewer, showNewWizardInPopupMenu);
      popupMenuManager.addMenuListener(fileFolderContextMenu);
      popupMenuManager.setRemoveAllWhenShown(true);
      Menu menu = popupMenuManager.createContextMenu(tableViewer.getControl());
      tableViewer.getControl().setMenu(menu);
      fileFolderContextMenu.listenToViewer(tableViewer);
    }

  } // end createActions()

  /**
   * Method getContextMenuManager.
   * @return MenuManager
   */
  public MenuManager getContextMenuManager() {
    return this.popupMenuManager;
  }

  /**
   * Method setColumnHeaderImage.
   * @param column TableColumn
   * @param sortAscending boolean
   */
  private void setColumnHeaderImage(TableViewerColumn column, boolean sortAscending) {
    this.nameColumn.getColumn().setImage(null);
    this.sizeColumn.getColumn().setImage(null);
    this.modifiedColumn.getColumn().setImage(null);
    this.pathColumn.getColumn().setImage(null);
    this.typeColumn.getColumn().setImage(null);
    if (sortAscending) {
      column.getColumn().setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_UP, SharedImages.CAT_IMG_SIZE_16));
    } else {
      column.getColumn().setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_DOWN, SharedImages.CAT_IMG_SIZE_16));
    }
  }

  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.CatViewerContainer#getViewer()
   */
  @Override
  public StructuredViewer getViewer() {
    return tableViewer;
  }

  /**
   * Method resourceSelectionChanged.
   * @param currentSelection IResourceSelection
   * @see gov.pnnl.cat.ui.rcp.IResourceSelectionListener#resourceSelectionChanged(IResourceSelection)
   */
  public void resourceSelectionChanged(IResourceSelection currentSelection) {
    if (tableViewer != null) {
      Object previousInput = tableViewer.getInput();

      // don't do anything if the input hasn't changed
      if (previousInput != null && previousInput.equals(currentSelection.getIResource())) {
        logger.debug("NOT UPDATING THE TABLE (same input)");
        return;
      }
    }

    // don't do anything if the currentSelection is nothing (i.e., ctrl+click to
    // unselect something)
    if (currentSelection.isEmpty()) {
      
      // TODO remove line below if using DeferredContentProvider
      tableViewer.setInput(null);
     
      // TODO use the DeferredContentProvider and SWT.VIRTUAL table?    
      // clearTable();
    } else {
      final IResource resource = currentSelection.getIResource();

      // only interested in folders, thats all we should get anyways..
      if (resource instanceof IFolder) {
        this.tableInput = resource;

        if (tableViewer != null) {
        	
        	// TODO remove line below if using DeferredContentProvider
          tableViewer.setInput(tableInput);
        
        	// TODO use the DeferredContentProvider and SWT.VIRTUAL table?
          // setInput();
          
          fileFolderContextMenu.selectionChanged(null);
        }
      }
    }
  }

  /**
   * Method refreshResource.
   * @param path CmsPath
   * @param selectResource boolean
   * @see gov.pnnl.cat.ui.rcp.ISystemUpdateListener#refreshResource(CmsPath, boolean)
   */
  public void refreshResource(final CmsPath path, boolean selectResource) {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        if (tableInput != null) {
          if(mgr.resourceExists(tableInput.getPath())) {
            if (path.isPrefixOf(tableInput.getPath())) {
              tableViewer.refresh();              
            }
          } else {
            tableViewer.setInput(null);
          }
        }
      }
    });

  }

  /**
   * Add Listener<br>
   * Add a listener to the the event list.
   * 
   * @param listener
   *          the listener to add to the list
   */
  public void addSelectedFolderChangedListener(ITableSelectedFolderListener listener) {
    synchronized (tableSelectedFolderListeners) {
      tableSelectedFolderListeners.add(listener);
    }
  }

  /**
   * Remove Listener<br>
   * Remove a listener from the the event list.
   * 
   * @param listener
   *          the listener to remove from the list
   */
  public void removeSelectedFolderChangedListener(ITableSelectedFolderListener listener) {
    synchronized (tableSelectedFolderListeners) {
      tableSelectedFolderListeners.remove(listener);
    }
  }

  /**
   * Method fireSelectedFolderChanged.
   * @param node IResource
   */
  protected void fireSelectedFolderChanged(IResource node) {
    for (ITableSelectedFolderListener listener : tableSelectedFolderListeners) {
      listener.selectedFolderUpdated(node);
    }
  }

  /**
   * Method getTableViewer.
   * @return TableViewer
   */
  public TableViewer getTableViewer() {
    return this.tableViewer;
  }
  
  /**
   * Method requestSelection.
   * @param resource IResource
   */
  public void requestSelection(IResource resource) {
    ISelection sel = new StructuredSelection(resource);
    tableViewer.setSelection(sel, true);
    tableViewer.update(resource, null);
    //let table scroll to selected resource to make sure its in view 
  }

  /**
   * Method setSorter.
   * @param comparator TableExplorerComparator
   * @param column TableColumn
   */
  private void setSorter(TableExplorerComparator comparator, TableViewerColumn column) {
    if (currentComparator == comparator) {
      currentComparator.toggleSortOrder();
      
      // TODO remove line below if using DeferredContentProvider
      tableViewer.refresh();
    } else {
      currentComparator = comparator;
    }

    setColumnHeaderImage(column, currentComparator.isAscending());
    
    // TODO remove lines below if using DeferredContentProvider
    tableViewer.getControl().setRedraw(false);
    tableViewer.setSorter(new TableViewerSorter(comparator));
    tableViewer.getControl().setRedraw(true);
    
    // TODO use DeferredContentProvider and SWT.VIRTUAL table?
    // contentProvider.setSortOrder(currentComparator);
  }
  
  public  class CatColumnLabelProvider extends ColumnLabelProvider {
    private CatWorkbenchLabelProvider catLabelProvider;
    
    public CatColumnLabelProvider(CatWorkbenchLabelProvider catLabelProvider) {
      super();
      this.catLabelProvider = catLabelProvider;
    }

    @Override
    public void update(ViewerCell cell) {
      int col = cell.getColumnIndex();
      Object element = cell.getElement();
      String text = catLabelProvider.getColumnText(element, col);
      cell.setText(text);
      Image image = catLabelProvider.getColumnImage(element, col);
      cell.setImage(image);
      cell.setBackground(catLabelProvider.getBackground(element));
      cell.setForeground(catLabelProvider.getForeground(element));
      cell.setFont(catLabelProvider.getFont(element));
    }
    
  };
}
