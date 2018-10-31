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
package gov.pnnl.cat.ui.rcp.views.users.actions;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.CatSecurityException;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.cat.ui.rcp.CatActionIDs;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;

/**
 */
public class DeleteUserAction extends Action implements ISelectionChangedListener {
  private final static IUser[] EMPTY_ARRAY = new IUser[0];

  private ISecurityManager securityMgr = ResourcesPlugin.getSecurityManager();

  private IUser[] users = EMPTY_ARRAY;

  private Action updateViewAction;

  private Logger logger = CatLogger.getLogger(DeleteUserAction.class);

  /**
   * Constructor for DeleteUserAction.
   * @param updateViewAction Action
   */
  public DeleteUserAction(Action updateViewAction) {
    super("Delete");
    setToolTipText("Delete");
    setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_DEL, SharedImages.CAT_IMG_SIZE_16));
    setActionDefinitionId(CatActionIDs.DELETE_COMMAND);
    // by default is not enabled until something is selected:
    setEnabled(false);
    this.updateViewAction = updateViewAction;
  }

  /**
   * Method run.
   * @see org.eclipse.jface.action.IAction#run()
   */
  public void run() {
    String title = "Confirm User Delete";
    String msg = "Are you sure you want to delete the user '" + users[0].getUsername() + "'?";

    if (users.length > 1) {
      title = "Confirm Multiple User Delete";
      msg = "Are you sure you want to delete these " + users.length + " users?";
    }

    boolean shouldDelete = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), title, msg);

    if (shouldDelete) {
      String[] usernames = new String[users.length];
      for (int i = 0; i < users.length; i++) {
        usernames[i] = users[i].getUsername();
      }

      try {
        securityMgr.deleteUser(usernames);
      } catch (CatSecurityException e) {
        String message = "You do not have sufficient permissions to delete a user.";
        IStatus status = new Status(Status.ERROR, CatRcpPlugin.PLUGIN_ID, Status.ERROR, message, e);
        ErrorDialog.openError(Display.getDefault().getActiveShell(), "Error Deleting User", "An error occurred deleting the user.", status);
      } catch (Exception e) {
        String message = "A " + e.getClass().getSimpleName() + " was thrown.";
        IStatus status = new Status(Status.ERROR, CatRcpPlugin.PLUGIN_ID, Status.ERROR, message, e);
        ErrorDialog.openError(Display.getDefault().getActiveShell(), "Error Deleting User", "An error occurred deleting the user.", status);
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

    IUser user = null;
    boolean enabled = false;
    try {
      user = ResourcesPlugin.getSecurityManager().getActiveUser();
    } catch (Exception e) {
      logger.error(e);
    }

    StructuredSelection newSelection = (StructuredSelection) event.getSelection();
    if (!newSelection.isEmpty() && user != null && user.isAdmin()) {
      newSelection.getFirstElement();
      Object[] selectedElements = newSelection.toArray();
      users = new IUser[selectedElements.length];
      enabled = true;

      for (int i = 0; i < selectedElements.length; i++) {
        if (selectedElements[i] instanceof IUser) {
          users[i] = (IUser) selectedElements[i];
          if( users[i].getUsername().equals("guest") ) {
            enabled = false;
          }
        }
      }
      
    } else {
      users = EMPTY_ARRAY;
    }
    setEnabled(enabled);
  }
}
