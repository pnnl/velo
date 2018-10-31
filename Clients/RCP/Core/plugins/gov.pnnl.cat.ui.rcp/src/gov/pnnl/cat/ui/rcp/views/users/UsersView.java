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
package gov.pnnl.cat.ui.rcp.views.users;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IUserEventListener;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.CatActionIDs;
import gov.pnnl.cat.ui.rcp.actions.PropertiesActionGroup;
import gov.pnnl.cat.ui.rcp.views.users.actions.DeleteUserAction;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class UsersView extends ViewPart implements IDoubleClickListener, IUserEventListener {
  private Logger logger = CatLogger.getLogger(this.getClass());
  private ISecurityManager securityMgr = ResourcesPlugin.getSecurityManager();
  private UserFilteredTree userFilteredTree;
  private UpdateUsersViewAction updateUsersViewAction;

  private IWorkbenchAction propertiesAction;
  private PropertiesActionGroup propertiesActionGroup;
  
  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  public void createPartControl(Composite parent) {
    userFilteredTree = new UserFilteredTree(parent);

    TreeViewer treeViewer = userFilteredTree.getViewer();
    treeViewer.addDoubleClickListener(this);
    getSite().setSelectionProvider(treeViewer);

    // set the focus so the user can start typing immediately
    this.userFilteredTree.getFilterControl().setFocus();

    createActions();
    fillActionBars(getViewSite().getActionBars());
    
    // so I can be apprised of user change events
    this.securityMgr.addUserEventListener(this);
  }


  private void createActions() {
    ContentViewer viewer = userFilteredTree.getViewer();

    // Add the context menu for this explorer.
    MenuManager popupMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
    Menu menu = popupMenuManager.createContextMenu(viewer.getControl());
//    popupMenuManager.addMenuListener(this);

    viewer.getControl().setMenu(menu);

    this.updateUsersViewAction = new UpdateUsersViewAction();

    DeleteUserAction deleteAction = new DeleteUserAction(updateUsersViewAction);
    this.getViewSite().getActionBars().setGlobalActionHandler(CatActionIDs.DELETE_ACTION, deleteAction);
    popupMenuManager.add(deleteAction);

    // Add the New User Section
    popupMenuManager.add(new Separator("new"));

    IContributionItem mWizardMenu = ContributionItemFactory.NEW_WIZARD_SHORTLIST.create(getSite().getWorkbenchWindow());
    MenuManager wizardMenu = new MenuManager("&New", "new");
    wizardMenu.add(mWizardMenu);
    popupMenuManager.add(wizardMenu);

    popupMenuManager.add(new Separator("properties"));
    propertiesAction = ActionFactory.PROPERTIES.create(getSite().getWorkbenchWindow());
    popupMenuManager.add(propertiesAction);
    propertiesActionGroup = new PropertiesActionGroup(this, userFilteredTree.getViewer());


    //listen to selection events to determine who to delete & when to enable/disable:
    viewer.addSelectionChangedListener(deleteAction);
  }

  /**
   * Method fillActionBars.
   * @param actionBars IActionBars
   */
  private void fillActionBars(IActionBars actionBars) {
    IToolBarManager toolBar = actionBars.getToolBarManager();
    fillToolBar(toolBar);
    actionBars.updateActionBars();
    propertiesActionGroup.fillActionBars(actionBars);
  }

  /**
   * Method fillToolBar.
   * @param tbm IToolBarManager
   */
  private void fillToolBar(IToolBarManager tbm) {
    tbm.add(updateUsersViewAction);
  }

  /**
   * Method doubleClick.
   * @param event DoubleClickEvent
   * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(DoubleClickEvent)
   */
  public void doubleClick(DoubleClickEvent event) {
    StructuredSelection selectedFile = (StructuredSelection) userFilteredTree.getViewer().getSelection();
    if (selectedFile != null) {
      IUser selectedUser = ((IUser) selectedFile.getFirstElement());
      if (selectedUser instanceof IUser) { // Double click on folder
        propertiesAction.run();
      } else {
        //EZLogger.logWarning("double click a NON user?", null);
        logger.warn("double click a NON user?");
      }
    }
  }

  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
  }

  /**
   */
  private class UpdateUsersViewAction extends Action {
    public UpdateUsersViewAction() {
      super("Refresh");
      setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_REFESH, SharedImages.CAT_IMG_SIZE_16));
      setToolTipText("Refresh View");
    }

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      try {
        securityMgr.resetUserCache();
        userFilteredTree.getViewer().refresh();
      } catch (Exception e) {
        logger.error("Unable to refresh users view", e);
      }
    }
  }

  /**
   * User events get filtered by the security manager, so we know that 
   * if this method is called, at least one user has been changed.
   * @param events
   * @see gov.pnnl.cat.core.resources.events.IUserEventListener#onEvent(List<IResourceEvent>)
   */
  public void onEvent(List<IResourceEvent> events) {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        userFilteredTree.refreshWithSelection();
      }
    });
  }

}
