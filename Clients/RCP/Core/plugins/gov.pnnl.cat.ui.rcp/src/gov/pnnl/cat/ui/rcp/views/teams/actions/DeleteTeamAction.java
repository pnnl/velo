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
package gov.pnnl.cat.ui.rcp.views.teams.actions;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.CatActionIDs;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;

/**
 */
public class DeleteTeamAction extends Action implements ISelectionChangedListener {
  private final static ITeam[] EMPTY_ARRAY = new ITeam[0];

  private ISecurityManager securityMgr = ResourcesPlugin.getSecurityManager();

  private ITeam[] teams = EMPTY_ARRAY;

  private Action updateViewAction;

  private Logger logger = CatLogger.getLogger(this.getClass());

  /**
   * Constructor for DeleteTeamAction.
   * @param updateTeamAction Action
   */
  public DeleteTeamAction(Action updateTeamAction) {
    super("Delete");
    setToolTipText("Delete");
    setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_DEL, SharedImages.CAT_IMG_SIZE_16));
    setActionDefinitionId(CatActionIDs.DELETE_COMMAND);
    // by default is not enabled until something is selected:
    setEnabled(false);
    this.updateViewAction = updateTeamAction;
  }

  /**
   * Method run.
   * @see org.eclipse.jface.action.IAction#run()
   */
  public void run() {
    String title = "Confirm Team Delete";
    String msg = "Are you sure you want to delete the team '" + teams[0].getPath().getName() + "'?";

    if (teams.length > 1) {
      title = "Confirm Multiple Team Delete";
      msg = "Are you sure you want to delete these " + teams.length + " teams?";
    }

    MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), title, null, msg, MessageDialog.WARNING, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
    boolean shouldDelete = (dialog.open() == 0);

    if (shouldDelete) {
      CmsPath[] teamPaths = new CmsPath[teams.length];
      for (int i = 0; i < teams.length; i++) {
        teamPaths[i] = teams[i].getPath();
      }
      try {
        securityMgr.deleteTeam(teamPaths);
        
      } catch (Throwable e) {
        if(e.getMessage().contains("Access denied")) {
          ToolErrorHandler.handleError("You do not have sufficient permissions to delete this team.", e, true);
        } else {
          ToolErrorHandler.handleError("An error occurred deleting the team.", e, true);      
        }
     }

      this.updateViewAction.run();
    }
  }

  /**
   * Method selectionChanged.
   * @param event SelectionChangedEvent
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event) {
    // IUser user = null;
    // try {
    // user = ResourcesPlugin.getSecurityManager().getActiveUser();
    // } catch (Exception e) {
    // logger.error(e);
    // }

    StructuredSelection newSelection = (StructuredSelection) event.getSelection();
    // if (!newSelection.isEmpty() && user != null && user.isAdmin()) {
    if (!newSelection.isEmpty()) {
      newSelection.getFirstElement();
      Object[] selectedElements = newSelection.toArray();
      teams = new ITeam[selectedElements.length];

      for (int i = 0; i < selectedElements.length; i++) {
        if (selectedElements[i] instanceof ITeam) {
          teams[i] = (ITeam) selectedElements[i];
        }
      }

      setEnabled(true);
    } else {
      teams = EMPTY_ARRAY;
      setEnabled(false);
    }
  }

}
