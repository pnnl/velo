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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 */
public class ChangeMyPasswordAction implements IWorkbenchWindowActionDelegate {
  private IWorkbenchWindow window;

  private String username;

  protected static final String EXTENSION_POINT = "gov.pnnl.cat.ui.rcp.customChangePasswordBehavior";
  protected static final String ATTRIBUTE = "class";
  protected static CustomChangePasswordBehavior customChangePasswordBehavior;
  static {
    loadCustomBehavior();
  }

  private static void loadCustomBehavior() {
    customChangePasswordBehavior = null;
    
    try {
      // look up all the extensions for the LaunchSimulation extension point (should only be 1 per deployment)
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

      for (IConfigurationElement configurationElement : elements) {
        Object obj = configurationElement.createExecutableExtension(ATTRIBUTE);
        if(obj instanceof CustomChangePasswordBehavior) {
          CustomChangePasswordBehavior behavior = (CustomChangePasswordBehavior)obj;
          customChangePasswordBehavior = behavior;
          break;
        }
      }

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load custom delete behavior extension points.", e);
    }
  }
  
  
  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {
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
    if(customChangePasswordBehavior != null) {
      customChangePasswordBehavior.changePassword();
      
    } else {
      defaultChangePassword();
    }
    
  }
  
  private void defaultChangePassword() {
    //use the currently logged in username if a username wasn't passed in first.  This action is
    //used from both the Account menu via "Change my password" as well as from the User perspective
    //where the admin user can change others' password.  In the later case, the username of whos password
    //to change will be passed in.
    
    String currentUserLoggedIn = ResourcesPlugin.getSecurityManager().getUsername();
    if(this.username == null){
      this.username = currentUserLoggedIn;
    }
    
    Shell shell = null;
    if(window == null) {
      shell = Display.getCurrent().getActiveShell();
    } else {
      shell = window.getShell();
    }
    boolean confirmCurrentPassword = !ResourcesPlugin.getSecurityManager().getActiveUser().isAdmin();
    ChangePasswordDialog dialog = new ChangePasswordDialog(shell, confirmCurrentPassword, this.username);
    //reset username back to null for the next use of this action (its a singleton from the 'Account' menu, 
    //but created new for each use from the users perspective.
    this.username = null;
    dialog.open();   
  }

  /**
   * Method selectionChanged.
   * @param action IAction
   * @param selection ISelection
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
  }


  public void setUsername(String username) {
    this.username = username;
  }

}
