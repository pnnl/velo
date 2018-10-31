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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.actions.ActionDelegateViewSetup;
import gov.pnnl.cat.ui.rcp.actions.PropertiesActionGroup;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;

/**
 */
public abstract class TreeExplorerView extends AbstractExplorerView implements ICatExplorerView, IPerspectiveListener, INodeChangeListener {

  protected Logger logger = CatLogger.getLogger(getClass());

  public Composite mainComp = null;

  protected StackLayout stackLayout = null;

  protected Composite treeComposite;

  protected Composite messageComposite;

  protected Composite page;

  /**
   * @return true if the root node should be included in the tree view */
  public abstract boolean isRootIncluded();
  
  /**
   * Child classes should override if they want to hide the new wizard from the 
   * popup context menu.  Some users find this confusing.
  
   * @return boolean
   */
  public boolean showNewWizardInPopupMenu() {
    return true;
  }

  /**
   * Method showFiles.
   * @return boolean
   */
  public boolean showFiles() {
    return true;
  }
  
  public TreeExplorerView() {
    treeExplorer = new TreeExplorer(showFiles(), this);
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

    createMessageView(this.mainComp);
    createTreeView(this.mainComp);
 
    // use the PropertiesActionGroup to handle the properties action for us
    PropertiesActionGroup propertiesActionGroup = new PropertiesActionGroup(this.getViewPart(), treeExplorer.getTreeViewer());
    propertiesActionGroup.fillActionBars(this.getViewPart().getViewSite().getActionBars());

    //System.out.println("createPartControl");
    logger.debug("createPartControl");
    mainComp.layout();
    
  }
  
  /**
   * children can override decide if they need to display a message 
   * @param parent
   */
  protected void createMessageView(Composite parent) {
    
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

    getViewSite().getWorkbenchWindow().addPerspectiveListener(this);

    this.getViewSite().setSelectionProvider(treeExplorer.getTreeViewer());

    getViewSite().registerContextMenu(VIEW_CONTEXT_MENU_ID, treeExplorer.getContextMenuManager(), treeExplorer.getTreeViewer());
    treeExplorer.addNodeChangeListener(this);

    treeExplorer.setShowRoot(isRootIncluded());
    setRoot(getDefaultRoot());
    
    treeExplorer.restoreState();
  }
  
  @Override
  public void setRoot(Object objNewRoot) {
    super.setRoot(objNewRoot);
    childrenStateChanged(getRoot() != null);
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    super.dispose();
    getViewSite().getWorkbenchWindow().removePerspectiveListener(this);
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

  /**
   * Method perspectiveChanged.
   * @param page IWorkbenchPage
   * @param perspective IPerspectiveDescriptor
   * @param changeId String
   * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
   */
  public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
    //bringFocusToMyself(page);
//    if (changeId.equalsIgnoreCase(IWorkbenchPage.CHANGE_RESET_COMPLETE) && CatPerspectiveIDs.TAXONOMY_MANAGER.equalsIgnoreCase(perspective.getId())) {
//      uniqueId = 0;
//    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.INodeChangeListener#childrenStateChanged(boolean)
   */
  @Override
  public void childrenStateChanged(final boolean hasChildren) {
    logger.debug("childrenStateChanged: " + hasChildren);
    Control topControl = stackLayout.topControl;
    Control newControl;

    if (hasChildren) {
      newControl = treeComposite;
      treeExplorer.getCollapseAllAction().setEnabled(true);
    } else {
      newControl = messageComposite;
      treeExplorer.getCollapseAllAction().setEnabled(false);
    } 

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
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.AbstractExplorerView#getTableExplorer()
   */
  @Override
  public TableExplorer getTableExplorer() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView#getTreeExplorer()
   */
  public TreeExplorer getTreeExplorer() {
    return treeExplorer;
  }
  
  
}
