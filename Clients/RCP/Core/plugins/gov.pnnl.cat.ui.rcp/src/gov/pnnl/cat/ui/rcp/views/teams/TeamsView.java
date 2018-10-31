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
package gov.pnnl.cat.ui.rcp.views.teams;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IGroupEventListener;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.CatActionIDs;
import gov.pnnl.cat.ui.rcp.actions.PropertiesActionGroup;
import gov.pnnl.cat.ui.rcp.views.teams.actions.DeleteTeamAction;

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
public class TeamsView extends ViewPart implements IDoubleClickListener, IGroupEventListener
{
  private Logger logger = CatLogger.getLogger(this.getClass());
  private ISecurityManager securityMgr = ResourcesPlugin.getSecurityManager();

  private TeamFilteredTree filteredTree;

  private UpdateTeamsViewAction updateTeamsViewAction;
  private IWorkbenchAction propertiesAction;
  private PropertiesActionGroup propertiesActionGroup;

  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  public void createPartControl(Composite parent) {
    filteredTree = new TeamFilteredTree(parent);
    
    getSite().setSelectionProvider(this.filteredTree.getViewer());
    
    // set the focus so the user can start typing immediately
    this.filteredTree.getFilterControl().setFocus();

    createActions();
    fillActionBars(getViewSite().getActionBars());

    TreeViewer treeViewer = filteredTree.getViewer();
    treeViewer.addDoubleClickListener(this);

    // so I can be apprised of team change events
    this.securityMgr.addGroupEventListener(this);
  }

  private void createActions() {
    ContentViewer viewer = filteredTree.getViewer();
    // Add the context menu for this explorer.
    MenuManager popupMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
    Menu menu = popupMenuManager.createContextMenu(viewer.getControl());

    viewer.getControl().setMenu(menu);

    this.updateTeamsViewAction = new UpdateTeamsViewAction();

    DeleteTeamAction deleteAction = new DeleteTeamAction(updateTeamsViewAction);
    this.getViewSite().getActionBars().setGlobalActionHandler(CatActionIDs.DELETE_ACTION, deleteAction);
    popupMenuManager.add(deleteAction);

    // Add the New Team Section
    popupMenuManager.add(new Separator("new"));

    IContributionItem mWizardMenu = ContributionItemFactory.NEW_WIZARD_SHORTLIST.create(getSite().getWorkbenchWindow());
    MenuManager wizardMenu = new MenuManager("&New", "new");
    wizardMenu.add(mWizardMenu);
    popupMenuManager.add(wizardMenu);

    popupMenuManager.add(new Separator("properties"));
    propertiesAction = ActionFactory.PROPERTIES.create(getSite().getWorkbenchWindow());
    popupMenuManager.add(propertiesAction);
    
    //listen to selection events to determine who to delete & when to enable/disable:
    viewer.addSelectionChangedListener(deleteAction);
//    viewer.addSelectionChangedListener(propertiesAction);

    //connect Properties provider to the action
    propertiesActionGroup = new PropertiesActionGroup(this, filteredTree.getViewer());

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
    tbm.add(updateTeamsViewAction);
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
  private class UpdateTeamsViewAction extends Action {
    public UpdateTeamsViewAction() {
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
        securityMgr.resetGroupCache();
        filteredTree.getViewer().refresh();
      } catch (Exception e) {
        logger.error("Unable to refresh teams view", e);
      }
    }
  }

  /**
   * Method doubleClick.
   * @param event DoubleClickEvent
   * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(DoubleClickEvent)
   */
  public void doubleClick(DoubleClickEvent event) {
    StructuredSelection selectedFile = (StructuredSelection) filteredTree.getViewer().getSelection();
    if (selectedFile != null) {
      ITeam selectedTeam = ((ITeam) selectedFile.getFirstElement());
      if (selectedTeam instanceof ITeam) { // Double click on folder
        propertiesAction.run();
      } else {
        logger.warn("double click a NON team?");
      }
    }
  }

  /**
   * Team events get filtered by the security manager, so we know that 
   * if this method is called, at least one team has been changed.
   * @param events
   * @see gov.pnnl.cat.core.resources.events.IGroupEventListener#onEvent(List<IResourceEvent>)
   */
  public void onEvent(List<IResourceEvent> events) {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        filteredTree.refreshWithSelection();
      }
    });
  }

}
