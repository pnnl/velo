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
package gov.pnnl.cat.ui.rcp.views.databrowser;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.actions.ActionDelegateViewSetup;
import gov.pnnl.cat.ui.rcp.actions.PropertiesActionGroup;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.DataBrowserRoot;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.AbstractExplorerView;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.INodeChangeListener;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.SelectionProviderIntermediate;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

/**
 */
public class DataBrowserView extends AbstractExplorerView implements ICatExplorerView, INodeChangeListener {
  public static final String ID = DataBrowserView.class.getName();
  
  protected Logger logger = CatLogger.getLogger(getClass());

  protected Composite mainComp = null;
  protected StackLayout stackLayout = null;
  protected Composite treeComposite;
  protected Composite page;

  /**
   * Child classes should override if they want to show the new wizard from the 
   * popup context menu.  Some users find this confusing.
  
   * @return boolean
   */
  public boolean showNewWizardInPopupMenu() {
    return false;
  }

  /**
   * Method showFiles.
   * @return boolean
   */
  public boolean showFiles() {
    return false;
  }
  
  public DataBrowserView() {
    treeExplorer = new TreeExplorer(showFiles(), this);
  }

  @Override
  public Object getDefaultRoot() {
    IResource[] repositoryRoots = (IResource[])RCPUtil.getTreeRoot().getChildren();
    Object defaultRoot = new DataBrowserRoot(repositoryRoots);
    return defaultRoot;
  }

  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  public void createPartControl(Composite parent) {
    this.mainComp = new Composite(parent, 0);
    this.mainComp.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
    this.stackLayout = new StackLayout();
    this.mainComp.setLayout(stackLayout);

    createTreeView(this.mainComp);
    createMessageView(this.mainComp);

    fillActionBars(getViewSite().getActionBars());

    //System.out.println("createPartControl");
    logger.debug("createPartControl");
    childrenStateChanged(getRoot() != null);

    mainComp.layout();
    
  }
  
  /**
   * Method fillActionBars.
   * @param actionBars IActionBars
   */
  protected void fillActionBars(IActionBars actionBars) {
    IToolBarManager toolBar = actionBars.getToolBarManager();
    
    RefreshAction refreshAction = new RefreshAction();
    toolBar.add(refreshAction);
    
    // use the PropertiesActionGroup to handle the properties action for us
    PropertiesActionGroup propertiesActionGroup = new PropertiesActionGroup(this.getViewPart(), treeExplorer.getTreeViewer());
    propertiesActionGroup.fillActionBars(actionBars);
    
    actionBars.updateActionBars();
  }
  
  /**
   * children can override decide if they need to display a message 
   * @param parent
   */
  protected void createMessageView(Composite parent) {
    
  }

  /**
   * Method getTreeExplorer.
   * @return TreeExplorer
   */
  public TreeExplorer getTreeExplorer() {
    return treeExplorer;
  }


  /**
   * Method createTreeView.
   * @param parent Composite
   */
  protected void createTreeView(Composite parent) {
    this.treeComposite = new Composite(parent, 0);
    this.treeComposite.setLayout(new FillLayout());

    new ActionDelegateViewSetup(getViewSite().getActionBars());
    treeExplorer.createPartControl(this.treeComposite, showNewWizardInPopupMenu(), showFiles());

    this.getViewSite().setSelectionProvider(treeExplorer.getTreeViewer());

    getViewSite().registerContextMenu(VIEW_CONTEXT_MENU_ID, treeExplorer.getContextMenuManager(), treeExplorer.getTreeViewer());
    treeExplorer.addNodeChangeListener(this);

    setRoot(getDefaultRoot());
    
    treeExplorer.restoreState();
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    super.dispose();
    if (treeExplorer != null) {
      treeExplorer.dispose();
    }
  }


  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus() {
    if (treeExplorer != null) {
      // give focus to the tree
      treeExplorer.setFocus();

      // make tree be selection provider
      SelectionProviderIntermediate.getInstance().setSelectionProviderDelegate(treeExplorer.getTreeViewer());
    }
  }

  /**
   * Method perspectiveActivated.
   * @param page IWorkbenchPage
   * @param perspective IPerspectiveDescriptor
   * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
   */
  public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
    //bringFocusToMyself(page);
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.INodeChangeListener#childrenStateChanged(boolean)
   */
  @Override
  public void childrenStateChanged(final boolean hasChildren) {
    logger.debug("childrenStateChanged: " + hasChildren);
    Control topControl = stackLayout.topControl;
    Control newControl;

    newControl = treeComposite;
    treeExplorer.getCollapseAllAction().setEnabled(true);

    if (newControl != topControl) {
      stackLayout.topControl = newControl;
      mainComp.layout();
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.IResourceContainer#getTableViewer()
   */
  @Override
  public TableViewer getTableViewer() {
    return null;
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
     if(treeExplorer != null) {
      return treeExplorer.getTreeViewer();
    }
    return null;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.IResourceContainer#isTableActive()
   */
  @Override
  public boolean isTableActive() {
    return false;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.IResourceContainer#getTableExplorer()
   */
  @Override
  public TableExplorer getTableExplorer() {
    // TODO Auto-generated method stub
    return null;
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
