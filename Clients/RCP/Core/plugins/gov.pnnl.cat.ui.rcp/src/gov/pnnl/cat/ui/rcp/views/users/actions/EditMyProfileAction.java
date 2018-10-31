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
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.cat.ui.rcp.dialogs.properties.user.UserPropertiesDialog;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 */
public class EditMyProfileAction implements IWorkbenchWindowActionDelegate {

  private IWorkbenchWindow window;

  private Logger logger = CatLogger.getLogger(getClass());

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {
    // TODO Auto-generated method stub

  }

  /**
   * Method init.
   * @param window IWorkbenchWindow
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
    this.window = window;

  }

  /**
   * Method run.
   * @param action IAction
   * @see org.eclipse.ui.IActionDelegate#run(IAction)
   */
  public void run(IAction action) {
    try {
      IUser user = ResourcesPlugin.getSecurityManager().getActiveUser();
      // TODO: CHANGE TO USE NEW PROPERTY PAGES SO WE CAN DELETE THIS OLD ONE:
      UserPropertiesDialog dialog = new UserPropertiesDialog(Display.getCurrent().getActiveShell(), user, UserPropertiesDialog.Tab.PERSON_PROPERTIES_TAB);

      dialog.open();
    } catch (Exception e) {
      logger.error("Could not lookup active user", e);
      IStatus status = new Status(IStatus.ERROR, CatRcpPlugin.PLUGIN_ID, 0, "Could not lookup active user", e);
      ErrorDialog.openError(window.getShell(), "Error Opening Profile", "Your user profile could not be opened.", status);
    }
  }

  /**
   * Method selectionChanged.
   * @param action IAction
   * @param selection ISelection
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
  }

}
