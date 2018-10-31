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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.actions.PropertiesActionGroup;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

/**
 * Creates a two panel viewpart with the TreeExplorer on the left and a TableExplorer on the right.
 * 
 * @see TableExplorer and TreeExplorer
 * @version $Revision: 1.0 $
 */
public abstract class TreeTableExplorerView extends AbstractExplorerView implements ICatExplorerView, ISelectionProvider {
  protected boolean showFiles = false;
  

  // Local Variables
  protected TableExplorer tableExplorer;

  // GUI Variables.
  protected SashForm m_sash;
  protected int[] sashWeight = null;
  protected NavigationBar navBar;

  protected SelectionProviderIntermediate selectionProvider;

  protected Logger logger = CatLogger.getLogger(TreeTableExplorerView.class);

  protected PropertiesActionGroup propertiesActionGroup;
  protected RefreshAction refreshAction;

  protected static IResourceManager mgr = ResourcesPlugin.getResourceManager();

  public TreeTableExplorerView() {
    super();

    //setViewId(CatViewIDs.DATA_INSPECTOR);

    treeExplorer = new TreeExplorer(showFiles, this, isTreeSingleSelection());
    tableExplorer = new TableExplorer(this);

    // Add listeners between the table to the tree and between the tree to the table.
    treeExplorer.addFileExplorerChangeListener(tableExplorer);
    tableExplorer.addSelectedFolderChangedListener(treeExplorer);
    
    selectionProvider = SelectionProviderIntermediate.getInstance();
  }
  
  /**
   * Child classes should override if they want to allow multiple selection in the tree or not
   * @return boolean
   */
  public boolean isTreeSingleSelection() {
    return true;
  }
  
  /**
   * Child classes should override if they want to hide the new wizard from the 
   * popup context menu.  Some users find this confusing.
  
   * @return boolean
   */
  public boolean showNewWizardInPopupMenu() {
    return true;
  }

  /**
  
  
   * @return true if the root node should be included in the tree view */
  public abstract boolean isRootIncluded();
  
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent) {
    
    Composite wrapper = new Composite(parent, SWT.NONE);
    final GridLayout gridLayout = new GridLayout(1, true);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    wrapper.setLayout(gridLayout);
    wrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    navBar.createControl(wrapper);
    
    m_sash = new SashForm(wrapper, SWT.NONE);
    GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
    gd.horizontalSpan = 1;
    m_sash.setLayoutData(gd);
    treeExplorer.createPartControl(m_sash, showNewWizardInPopupMenu(), this.showFiles);
    tableExplorer.createPartControl(m_sash, showNewWizardInPopupMenu());

    if (sashWeight == null) {
      sashWeight = new int[] { 30, 70 };
    }
    m_sash.setWeights(sashWeight);

    // JSD Added as a demonstration and placeholder for the context sensitive help.
    IWorkbenchHelpSystem iwhs = PlatformUI.getWorkbench().getHelpSystem();
    iwhs.setHelp(this.treeExplorer.getCatControl(), "gov.pnnl.cat.ui.rcp.DetailedRepositoryExplorer");

    // Initalize selection events on sub views though the intermediate selection provider.
    initSelectionProvider();

    getViewSite().registerContextMenu(VIEW_CONTEXT_MENU_ID, treeExplorer.getContextMenuManager(), selectionProvider);
    getViewSite().registerContextMenu(VIEW_CONTEXT_MENU_ID, tableExplorer.getContextMenuManager(), selectionProvider);

    createActions();
    fillActionBars(getViewSite().getActionBars());
    
    treeExplorer.setShowRoot(isRootIncluded());
    setRoot(getDefaultRoot());
    treeExplorer.restoreState();
    
  }

 
  /**
   * Method expandToPath.
   * @param resource IResource
   */
  public void expandToPath(IResource resource) {
    // expand the parent path in the tree
    this.treeExplorer.getTreeViewer().expandToPath(resource.getParent().getPath());
    
    // now select the resource in the table
    tableExplorer.getTableViewer().setSelection(new StructuredSelection(resource));
  }
  
  /**
   * Method addSelectionChangedListener.
   * @param listener ISelectionChangedListener
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
   */
  public void addSelectionChangedListener(ISelectionChangedListener listener) {
    // currently no-op
  }

  /**
   * Method removeSelectionChangedListener.
   * @param listener ISelectionChangedListener
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
   */
  public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    // currently no-op
  }

  /**
   * Method getSelection.
   * @return ISelection
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection() {
    // currently no-op
    return null;
  }

  /**
   * Method setSelection.
   * @param selection ISelection
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(ISelection)
   */
  public void setSelection(ISelection selection) {
    if (selection.isEmpty())
      return;
    if (!(selection instanceof IStructuredSelection))
      return;
    
    IStructuredSelection s = (IStructuredSelection) selection;
    Object object = s.getFirstElement();
    
    CmsPath path = null;
    
    if (object instanceof String) {
      String string = (String) object;
      path = new CmsPath( string );      
    }
    
    expandToPath(path);
    
    // SDH: Should try to force the selection to fire the apprpriate event.
//      selectionProvider.setSelection(new StructuredSelection( ResourcesPlugin.getResourceManager().getResource(path)));
  }

  private void createActions() {
    this.propertiesActionGroup = new PropertiesActionGroup(this, selectionProvider);
    this.refreshAction = new RefreshAction();
  }
  
  /**
   * Method fillActionBars.
   * @param actionBars IActionBars
   */
  protected void fillActionBars(IActionBars actionBars) {
    IToolBarManager toolBar = actionBars.getToolBarManager();
    toolBar.add(refreshAction);
    actionBars.updateActionBars();
    
    // Now add dynamic menu extensions
    propertiesActionGroup.fillActionBars(actionBars);
  }

  /**
   * Initalizes the intermediate selection provider.
   */
  private void initSelectionProvider() {
    treeExplorer.getTreeViewer().getTree().addFocusListener(new FocusAdapter() {
      public void focusGained(final FocusEvent e) {
        selectionProvider.setSelectionProviderDelegate(treeExplorer.getTreeViewer());
      }
    });

    tableExplorer.getTableViewer().getTable().addFocusListener(new FocusAdapter() {
      public void focusGained(final FocusEvent e) {
        selectionProvider.setSelectionProviderDelegate(tableExplorer.getTableViewer());
      }
    });

    this.getViewSite().setSelectionProvider(selectionProvider);
    selectionProvider.setSelectionProviderDelegate(treeExplorer.getTreeViewer());
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  public void setFocus() {
    this.tableExplorer.getTableViewer().getControl().setFocus();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
   */
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    super.init(site, memento);
    
    // Restore the previous state.
    if (memento != null) {
     if ((memento.getChild("left_weight") != null) && (memento.getChild("left_weight") != null)) {
        sashWeight = new int[2];
        try {
          sashWeight[0] = Integer.parseInt(memento.getChild("left_weight").getID());
          sashWeight[1] = Integer.parseInt(memento.getChild("right_weight").getID());
        } catch (NumberFormatException NFex) {
          logger.error(NFex);
        }
      }
  
    }
    navBar = new NavigationBar();
    navBar.linkExplorers(treeExplorer, tableExplorer);
    navBar.init(memento);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
   */
  public void saveState(IMemento memento) {
    super.saveState(memento);
    
    // save the path history
    navBar.saveState(memento);

    // Save the state of the GUI variables.
    int[] sashWeight = m_sash.getWeights();
    memento.createChild("left_weight", String.valueOf(sashWeight[0]));
    memento.createChild("right_weight", String.valueOf(sashWeight[1]));
  }

  /**
   * Method getCurrentDelegate.
   * @return ISelectionProvider
   */
  public ISelectionProvider getCurrentDelegate() {
    return selectionProvider.getCurrentDelegate();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    super.dispose();
    if(navBar != null) {
      navBar.dispose();
    }
    treeExplorer.dispose();
    tableExplorer.dispose();
  }

  /**
   * Method getTableViewer.
   * @return TableViewer
   */
  @Override
  public TableViewer getTableViewer() {
    return tableExplorer.getTableViewer();
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.IResourceContainer#getTreeViewr()
   */
  /**
   * Method getTreeViewer.
   * @return TreeViewer
   */
  @Override
  public TreeViewer getTreeViewer() {
    return treeExplorer.getTreeViewer();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView#getTableExplorer()
   */
  @Override
  public TableExplorer getTableExplorer() {
    return tableExplorer;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView#getTreeExplorer()
   */
  public TreeExplorer getTreeExplorer() {
    return treeExplorer;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.IResourceContainer#isTableActive()
   */
  @Override
  public boolean isTableActive() {
    
    ISelectionProvider delegate = getCurrentDelegate();
    if (delegate instanceof TableViewer) {
      return true;
    }
    return false;
  }
  
  private class RefreshAction extends Action {
    public RefreshAction() {
      super("Refresh");
      setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_REFESH, SharedImages.CAT_IMG_SIZE_16));
      setToolTipText("Refresh");
    }

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      try {
        ResourcesPlugin.getResourceManager().clearCache();
      } catch (Exception e) {
        ToolErrorHandler.handleError("Failed to refresh view.", e, true);
      }
    }
  }
 
}
